#!/usr/bin/env bash
#
# 重建 SSC 的依赖链（Calio-Legacy → Apoli-Legacy → SSC），并把前两者发布到本地 maven。
#
# 为什么需要这个脚本：
#   SSC 的 build.gradle 用 mavenLocal() 解析 io.github.apace100:Apoli-Legacy，
#   而该 artifact **只存在于本地 maven**（两个 fork 的 publishing 仓库被改成空，只走
#   publishToMavenLocal）。所以每次改了 libs/apoli 或 libs/calio 的源码后，都必须先
#   发布，再构建 SSC —— 否则 SSC 会静默用上旧 jar（它们被 JiJ 内嵌进 SSC jar，
#   在测试服务器的 mods/ 里看不到，出问题极难排查）。
#
# ⚠ 顺序不能反：Calio 必须先于 Apoli。
#   Apoli 的构建要从 mavenLocal 解析 io.github.apace100:Calio-Legacy:${calio_version}，
#   先构建 Apoli 会让它链到旧的 Calio。
#
# 用法：
#   bash scripts/rebuild-deps.sh                  # 发布两个依赖 + 构建 SSC
#   bash scripts/rebuild-deps.sh --with-addon     # 再构建 _dev/shape-shifter-curse-addon
#   bash scripts/rebuild-deps.sh --to-testserver  # 再把 jar 拷进测试服务器 mods/
#
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

WITH_ADDON=0
TO_TESTSERVER=0
CHECK_ONLY=0
for arg in "$@"; do
    case "$arg" in
        --with-addon)    WITH_ADDON=1 ;;
        --to-testserver) TO_TESTSERVER=1 ;;
        --check-only)    CHECK_ONLY=1 ;;
        -h|--help)       sed -n '2,26p' "$0"; exit 0 ;;
        *) echo "未知参数: $arg（用 --help 看用法）" >&2; exit 2 ;;
    esac
done

# 从 properties 文件读一个键：容忍前导空白、`=` 两侧空格、CRLF 行尾
prop() {
    tr -d '\r' < "$1" | sed -n "s/^[[:space:]]*$2[[:space:]]*=[[:space:]]*\(.*[^[:space:]]\)[[:space:]]*$/\1/p" | head -1
}

step() { printf '\n\033[1m==> %s\033[0m\n' "$1"; }
ok()   { printf '  \033[32m✓\033[0m %s\n' "$1"; }
bad()  { printf '  \033[31m✗\033[0m %s\n' "$1"; }

# ---------------------------------------------------------------------------
# 前置：版本一致性校验
#   真实的约束链有两条，缺一不可：
#     calio.mod_version + "+" + calio.minecraft_version  ==  apoli.calio_version
#     apoli.mod_version + "+" + apoli.minecraft_version  ==  SSC.apoli_version
#   任何一个对不上，都会让下游解析到与源码不符的 artifact，且**不会报错**。
#   放在构建前做是为了 fail fast —— 别白跑几分钟再失败。
# ---------------------------------------------------------------------------
step "校验版本一致性"
FAIL=0

check() { # <说明> <实际值> <期望值>
    if [ -z "$2" ] || [ -z "$3" ]; then
        bad "$1：读不到值（实际='$2' 期望='$3'）—— 检查 gradle.properties 的键名"; FAIL=1
    elif [ "$2" != "$3" ]; then
        bad "$1：$2  ≠  $3"; FAIL=1
    else
        ok "$1 = $2"
    fi
}

CALIO_PUB="$(prop libs/calio/gradle.properties mod_version)+$(prop libs/calio/gradle.properties minecraft_version)"
APOLI_PUB="$(prop libs/apoli/gradle.properties mod_version)+$(prop libs/apoli/gradle.properties minecraft_version)"
APOLI_EXPECTS_CALIO="$(prop libs/apoli/gradle.properties calio_version)"
SSC_EXPECTS_APOLI="$(prop gradle.properties apoli_version)"

check "calio 发布版本 → apoli 期望的 calio_version" "$CALIO_PUB" "$APOLI_EXPECTS_CALIO"
check "apoli 发布版本 → SSC 期望的 apoli_version"   "$APOLI_PUB" "$SSC_EXPECTS_APOLI"

if [ "$FAIL" -ne 0 ]; then
    echo
    echo "中止：先把版本对齐再构建。"
    echo "  改 libs/<lib>/gradle.properties 的 mod_version，或改上游引用处的版本键。"
    echo "  注意：SSC gradle.properties 里的 calio_version 是**死变量**（build.gradle 从不引用它，"
    echo "        Calio 是经 Apoli 的 POM 传递进来的），别去改它来"修"这里。"
    exit 1
fi

if [ "$CHECK_ONLY" -eq 1 ]; then
    echo
    ok "仅校验（--check-only），未执行构建。"
    exit 0
fi

# ---------------------------------------------------------------------------
# 1/3、2/3：发布两个依赖（各自用**自己的** gradlew —— 两库是 Gradle 9.4.0 wrapper，
#               与 SSC 的 9.5.1 独立，用同一个 Gradle 跑会踩版本坑）
# ---------------------------------------------------------------------------
step "1/3 发布 Calio-Legacy → mavenLocal"
(cd libs/calio && ./gradlew publishToMavenLocal)

step "2/3 发布 Apoli-Legacy → mavenLocal"
(cd libs/apoli && ./gradlew publishToMavenLocal)

# ---------------------------------------------------------------------------
# 3/3：构建 SSC
# ---------------------------------------------------------------------------
step "3/3 构建 SSC"
./gradlew build

echo
ok "依赖已发布到 mavenLocal，SSC 已构建。"
echo "  JiJ 内嵌验证（可选）：build/libs/*.jar 内 META-INF/jars/Apoli-Legacy-*.jar 的字节大小"
echo "  应与 ~/.m2/repository/io/github/apace100/Apoli-Legacy/${SSC_EXPECTS_APOLI}/ 下的 jar 一致。"

# ---------------------------------------------------------------------------
# 可选后续
# ---------------------------------------------------------------------------
if [ "$WITH_ADDON" -eq 1 ]; then
    step "附加：构建 addon"
    if [ -d _dev/shape-shifter-curse-addon ]; then
        (cd _dev/shape-shifter-curse-addon && ./gradlew build)
    else
        echo "  ⚠ 未找到 _dev/shape-shifter-curse-addon，跳过"
    fi
fi

if [ "$TO_TESTSERVER" -eq 1 ]; then
    step "附加：拷贝 jar 到测试服务器"
    TS="_dev/TestServer-1.21.1/mods"
    if [ -d "$TS" ]; then
        cp -v build/libs/*.jar "$TS/"
        if [ "$WITH_ADDON" -eq 1 ]; then
            cp -v _dev/shape-shifter-curse-addon/build/libs/*.jar "$TS/" 2>/dev/null || true
        fi
    else
        echo "  ⚠ 未找到 $TS，跳过"
    fi
fi
