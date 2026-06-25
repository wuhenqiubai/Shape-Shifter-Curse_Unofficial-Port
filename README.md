
# 感谢 [onixary](https://github.com/onixary) 与 [xu233333](https://github.com/xu233333) 开发了这个优秀的模组！本项目仅为适配与修复维护，所有核心功能版权归原作者所有。
# Thank you [onixary](https://github.com/onixary) with [xu233333](https://github.com/xu233333) Developed this excellent mod! This project is only for adaptation and repair maintenance, and all core functions are copyrighted by the original author.

## **幻形者诅咒（Shape Shifter Curse Mod）** 的 **独立维护/非官方移植版本**
- 原项目：[Shape Shifter Curse](https://github.com/onixary/shape-shifter-curse-fabric)
- 原作者：[onixary](https://github.com/onixary)
- 许可证：MIT License（遵循原项目协议，保留所有版权声明）
> 本项目独立于原项目，仅用于适配新版 Minecraft 与修复问题，所有功能版权归原作者所有。
> 在发布稳定版本后会偶尔发布我的小巧思。

## Minecraft 1.21.1 移植进度 / Porting Progress

> **分支 / Branch**: `ver/1.21.1_Apoli2.12.0-alpha7`
>
> **基于主线commit
**: [2ea1585](https://github.com/onixary/shape-shifter-curse-fabric/commit/2ea1585476ae2b457c35a3cbb95f708dcaeafe89)

| 状态 | 模块     | 说明                |
|----|--------|-------------------|
| ✅  | Java代码 | 完全移植              |
| ✅  | 数据包    | 标签/配方/物品均已通过数据包加载 |

> **运行时状态** : ✅ 核心问题已修复，正常游戏流程完整，可用于生存游玩

---

## 已知问题 / Known Issues

* 锻造台升级配方和盔甲穿戴限制依赖 MorphScaleTagLoader 在运行时解析标签 JSON（而非原版标签系统），数据包覆盖标签文件即可扩展
* 部分 mixin 在 Apoli/Calio 内嵌版本更新后需要重新验证
* 穿戴装备/饰品的按键行为异常,有无法穿戴装备的bug

---

## 非官方维护声明 / Unofficial Maintenance Notice

**本分支是社区维护的 1.21.1 移植版本，非官方发布。**

- 此移植由社区贡献者完成，**未经官方开发者 (onixary) 审核或认可**
- 核心游戏流程已趋于稳定
- 欢迎提交 Issue 或 PR 协助改进移植质量

**This branch is a community-maintained 1.21.1 port and is NOT an official release.**

- This port was completed by community contributors, **not reviewed or endorsed by the original author (onixary)**
- Core gameplay is stable; critical bugs (P0/P1/P2) have been resolved
- Issues and PRs are welcome to help improve port quality

---

## 移植贡献者 / Port Contributors

| 贡献者                                           | 角色                                    |
|-----------------------------------------------|---------------------------------------|
| [wuhenqiubai](https://github.com/wuhenqiubai) | 移植维护 / Port Maintainer                |
| [Claude Code (Anthropic)](https://claude.com/product/claude-code)                       | 代码审查与重构辅助 / Code Review & Refactoring |
| [onixary](https://github.com/onixary)         | 原作者 / Original Author                 |
| [xu233333](https://github.com/xu233333)       | 原作者 / Original Author                 |

> **移植版下载**: 请从 [GitHub Releases](https://github.com/wuhenqiubai/Shape-Shifter-Curse_Unofficial-Port/releases)
> 页面查找
