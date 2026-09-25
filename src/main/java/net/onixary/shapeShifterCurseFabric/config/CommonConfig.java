package net.onixary.shapeShifterCurseFabric.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

// 双端配置
@Config(name = "shape-shifter-curse-common")
public class CommonConfig implements ConfigData {
    public CommonConfig() {}

    // Cloth Config 没有浮点的边界检查 只有整数的
    @ConfigEntry.Category("General")
    @Comment("Transformative Bat Spawn Chance, 0 For Disable Spawn. Default: 0.5f [0.0f ~ 1.0f]")
    public float transformativeBatSpawnChance = 0.5f;

    @ConfigEntry.Category("General")
    @Comment("Transformative Axolotl Spawn Chance, 0 For Disable Spawn. Default: 0.5f [0.0f ~ 1.0f]")
    public float transformativeAxolotlSpawnChance = 0.5f;

    @ConfigEntry.Category("General")
    @Comment("Transformative Ocelot Spawn Chance, 0 For Disable Spawn. Default: 0.5f [0.0f ~ 1.0f]")
    public float transformativeOcelotSpawnChance = 0.5f;

    @ConfigEntry.Category("General")
    @Comment("Transformative Wolf Spawn Chance, 0 For Disable Spawn. Default: 0.5f [0.0f ~ 1.0f]")
    public float transformativeWolfSpawnChance = 0.5f;

    @ConfigEntry.Category("General")
    @Comment("Transformative Spider Spawn Chance, 0 For Disable Spawn. Default: 0.5f [0.0f ~ 1.0f]")
    public float transformativeSpiderSpawnChance = 0.5f;

    @ConfigEntry.Category("General")
    @Comment("Curse Moon Phase (0 ~ 7). Default: [1, 5]")
    public int[] curseMoonPhase = {1, 5};

    @ConfigEntry.Category("General")
    @Comment("Allow players to sleep during Cursed Moon. Default: false")
    public boolean allowSleepInCursedMoon = false;

    @ConfigEntry.Category("General")
    @Comment("Enable Cursed Moon Transform. Default: true")
    public boolean enableCursedMoonTransform = true;

    @ConfigEntry.Category("General")
    @Comment("Enable Debug Command (PermissionLevel = 0). Default: false")
    public boolean enableDebugCommand = false;

    @ConfigEntry.Category("General")
    @Comment("Enable Upgrade Recipe Can Upgrade Full Stack. Default: true")
    public boolean enableFullStackUpgrade = true;  // 防止一些Mod冲突导致刷物品Bug

    @ConfigEntry.Category("General")
    @Comment("Enable Food Habit System Default: true")
    public boolean enableFoodHabitSystem = true;

    @ConfigEntry.Category("General")
    @Comment("Immediately Transform Default: false")
    public boolean immediatelyTransform = false;

    @ConfigEntry.Category("General")
    @Comment("Enable Initial Form Default: true")
    public boolean enableInitialForm = false;

    @ConfigEntry.Category("General")
    @Comment("Initial form IDs. Supports datapack forms. One ID will be picked randomly. Default: [shape-shifter-curse:original_before_enable:1]")
    public String[] initialFormIds = {"shape-shifter-curse:original_before_enable:1"};

    @ConfigEntry.Category("General")
    @Comment("Transformative Potion Can Make Pre-Book Transforms Default: false")
    public boolean statusPotionWithCurse = false;

    @ConfigEntry.Category("General")
    @Comment("Witch Can Throw Potion To Pre-Book Player Default: false")
    public boolean witchPotionForPreBook = false;

    // 原 "Patron" 配置分类（enablePatronFormSystem / 各 [Obsolete] 的 data|resource pack 下载 URL /
    // PatronDataUrl / CheckUpdateInterval）已随 Patreon 验证与赞助者内容分发系统一并移除。

    // 开发用
    // @ConfigEntry.Category("InDevelopment")
}
