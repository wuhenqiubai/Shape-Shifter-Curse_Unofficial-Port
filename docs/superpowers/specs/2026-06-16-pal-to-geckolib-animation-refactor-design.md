# PAL → GeckoLib 动画架构重构设计

**日期**: 2026-06-16
**状态**: 已确认

## Context

当前项目使用 PAL (Player Animation Library) + GeckoLib 混合动画架构：
- PAL 负责 FSM 状态机、状态控制器、动画切换、淡入淡出
- GeckoLib 仅负责模型加载（GeoModel）和渲染管线
- 中间桥接层 `DefaultModelAnimationSystem` 手动从 PAL 读取骨骼变换拷贝到 GeckoLib GeoBone

此重构的目标：**完全移除 PAL 依赖，统一使用 GeckoLib 作为唯一的动画和渲染方案**。

## 核心决策

- **保留 FSM + 状态控制器架构**，适配输出 GeckoLib `RawAnimation`
- **转换** 80 个 PAL JSON 动画文件为 GeckoLib `.animation.json` 格式（骨骼名从 Minecraft 玩家模型命名改为 Geo Model 命名）
- **保留**程序化动画代码（尾巴物理/翅膀扇动），适配到 GeckoLib post-process 阶段
- **渐进迁移**，每步可独立测试

## 目标架构

```
Tick:   AnimSystem → FSM → StateController → AnimationState(RawAnimation引用)
            ↓
Render: GeckoLib AnimationController.predicate → 读取 AnimationState → setAnimation(RawAnimation)
            ↓
        GeckoLib 自动将关键帧应用到 GeoBone（无需手动骨骼拷贝）
            ↓
        Post-process: 程序化动画（尾巴/翅膀物理）
            ↓
        GeckoLib GeoObjectRenderer → 渲染到屏幕
```

### 关键变化

| 当前（PAL）                                                 | 目标（纯 GeckoLib）                          |
|---------------------------------------------------------|-----------------------------------------|
| `AnimationHolder(PAL Animation)`                        | `RawAnimation` + 元数据（speed/fade/easing） |
| PAL `PlayerAnimationController` 驱动骨骼                    | GeckoLib `AnimationController` 自动应用关键帧  |
| `DefaultModelAnimationSystem.processAnimation()` 手动拷贝骨骼 | GeckoLib 内置处理，移除手动拷贝                    |
| `FormAnimatable.registerControllers()` 空实现              | 注册真正的 AnimationController               |
| `AnimSystem.getPlayerBone3DTransform()`                 | 不再需要，删除                                 |

## 动画数据迁移

### 骨骼名映射

PAL JSON 骨骼名 → Geo Model 骨骼名，按体型分组：

| PAL 名      | FERAL Geo 名     | BIPED Geo 名     |
|------------|-----------------|-----------------|
| `head`     | `bipedHead`     | `bipedHead`     |
| `body`     | `bipedBody`     | `bipedBody`     |
| `rightArm` | `bipedRightArm` | `bipedRightArm` |
| `leftArm`  | `bipedLeftArm`  | `bipedLeftArm`  |
| `rightLeg` | `bipedRightLeg` | `bipedRightLeg` |
| `leftLeg`  | `bipedLeftLeg`  | `bipedLeftLeg`  |
| `torso`    | 合并到 bipedBody   | 视模型而定           |

### 转换方式

1. 编写自动化转换脚本（遍历 PAL JSON → 骨骼名映射 → 输出 GeckoLib `.animation.json`）
2. 共享动画（`form_feral_common_*`）转换为一份，所有 feral 形态引用同一文件
3. 人工复核关键动画（idle、walk、attack 等各形态代表）
4. 输出到 `assets/.../animations/form/` 目录

## FSM → GeckoLib 桥接

### AnimationState（tick 写、render 读）

AnimSystem tick 阶段写入，GeckoLib AnimationController render 阶段读取：

- `currentBodyAnim: RawAnimation` — 主体动画
- `currentUpperAnim: RawAnimation` — 上半身覆写（使用物品/攻击）
- `currentPowerAnim: RawAnimation` — Power 动画
- `speed: float` — 播放速度
- `transitionTicks: int` — 过渡帧数（原 fade）
- `onAnimComplete: Runnable` — Power 动画完成回调

### Tick 端：AnimSystem 改造

FSM 和状态控制器逻辑完全不变，`getAnimation()` 改为返回 `RawAnimation` + 元数据，写入 `FormAnimatable.animationState`。

### Render 端：FormAnimatable.registerControllers()

注册 3 个 AnimationController：
- `"main"` — 主体动画
- `"upper"` — 上半身覆写
- `"power"` — Power 动画

每个 predicate 从 `AnimationState` 读取对应 `RawAnimation` 并 `setAnimation()`。

### Easing/Speed/Fade 映射

- `speed` → `AnimationController.setAnimationSpeed()`
- `fade` (ticks) → `AnimationController.transitionLength(fade / 20.0)` (秒)
- `easingType` (35 种) → GeckoLib 内置 + 自定义 `EasingType` 实现
- `skipFade` → `transitionLength(0)`

## 保留系统（仅适配）

| 系统                                            | 改动                                                      |
|-----------------------------------------------|---------------------------------------------------------|
| FSM 三层（OnGround/InAir/UseItem）                | 输出 stateID 不变                                           |
| 状态控制器（10 种 DP + ConditionAnim + Transforming） | `getAnimation()` 返回 `RawAnimation` 替代 `AnimationHolder` |
| IPlayerAnimController（Power 动画接口）             | 写入 `AnimationState.currentPowerAnim`                    |
| 网络同步包                                         | 逻辑不变，同步内容改为 GeckoLib 标识符                                |
| 程序化动画（尾巴/翅膀物理）                                | 移至 FormRenderFeature post-animation 阶段                  |
| PlayerFormDynamic（数据包形态）                      | JSON `"animID"` 改为 GeckoLib animation 路径                |
| FormModel 资源加载                                | `getAnimationResource()` 返回实际路径                         |

## 移除系统

- PAL 依赖（build.gradle）
- `AnimationHolder` 类
- `PlayerEntityAnimOverrideMixin`（PAL AnimationController 创建/tick 逻辑）
- `DefaultModelAnimationSystem.processAnimation()` 手动骨骼拷贝
- `AnimSystem.getPlayerBone3DTransform()`
- `FormRenderUtils.getPartRotation()/getPartPosition()`
- PAL 动画 JSON（80 个，`player_animations/` 目录）
- PAL 反编译源码（`_dev/PlayerAnimationLibFabric/`）

## 迁移顺序

| #  | 步骤                                                 | 验证                |
|----|----------------------------------------------------|-------------------|
| 1  | 编写骨骼名映射表 + 转换脚本                                    | 输出文件人工抽查          |
| 2  | 运行转换脚本，生成 GeckoLib `.animation.json`               | 文件数量/内容对比         |
| 3  | 创建 `AnimationState`，改造 `AnimSystem`                | 编译通过              |
| 4  | 实现 `FormAnimatable.registerControllers()`          | runClient 单形态验证   |
| 5  | 改造 `AnimationHolderData` → RawAnimation 元数据，逐个形态适配 | 每形态 runClient     |
| 6  | 适配程序化动画到 post-process 阶段                           | 尾巴/翅膀效果           |
| 7  | 适配 Power 动画 + 网络同步                                 | 触发 Power 动画       |
| 8  | 适配 `PlayerFormDynamic` JSON 格式                     | 数据包形态加载           |
| 9  | 移除 PAL 依赖和遗留代码                                     | 编译 + 全量 runClient |
| 10 | 删除旧 `player_animations/` 目录                        | 最终清理              |

## 验证方法

- 每步完成后 `./gradlew build` 确保编译通过
- 关键步骤后 `./gradlew runClient` 进入游戏验证：
  - 切换形态 → 动画是否正确播放
  - 移动/潜行/冲刺 → FSM 状态切换正常
  - 使用物品/攻击 → 上半身动画覆写
  - 变形动画 → TransformingController
  - Power 动画触发 → 网络同步正常
  - 数据包形态 → 自定义动画加载
  - 尾巴/翅膀物理 → 程序化动画效果
- 对照原始行为：切换形态前后动画效果一致