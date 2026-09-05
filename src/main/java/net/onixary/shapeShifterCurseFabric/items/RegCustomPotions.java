package net.onixary.shapeShifterCurseFabric.items;

import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;
import static net.onixary.shapeShifterCurseFabric.status_effects.RegOtherStatusEffects.FEED_EFFECT;
import static net.onixary.shapeShifterCurseFabric.status_effects.RegTStatusPotionEffect.*;

public class RegCustomPotions {
    public static final Potion MOONDUST_POTION =
            Registry.register(BuiltInRegistries.POTION, ResourceKey.create(Registries.POTION, Identifier.fromNamespaceAndPath(MOD_ID, "moondust_potion")), new Potion());
    public static final Potion BAT_FORM_POTION =
            Registry.register(BuiltInRegistries.POTION, ResourceKey.create(Registries.POTION, Identifier.fromNamespaceAndPath(MOD_ID, "to_bat_0_potion")), new Potion(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(TO_BAT_0_POTION), 3600)));
    public static final Potion AXOLOTL_FORM_POTION =
            Registry.register(BuiltInRegistries.POTION, ResourceKey.create(Registries.POTION, Identifier.fromNamespaceAndPath(MOD_ID, "to_axolotl_0_potion")), new Potion(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(TO_AXOLOTL_0_POTION), 3600)));
    public static final Potion OCELOT_FORM_POTION =
            Registry.register(BuiltInRegistries.POTION, ResourceKey.create(Registries.POTION, Identifier.fromNamespaceAndPath(MOD_ID, "to_ocelot_0_potion")), new Potion(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(TO_OCELOT_0_POTION), 3600)));
    public static final Potion FAMILIAR_FOX_FORM_POTION =
            Registry.register(BuiltInRegistries.POTION, ResourceKey.create(Registries.POTION, Identifier.fromNamespaceAndPath(MOD_ID, "to_familiar_fox_0_potion")), new Potion(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(TO_FAMILIAR_FOX_0_POTION), 3600)));
    public static final Potion SNOW_FOX_FORM_POTION =
            Registry.register(BuiltInRegistries.POTION, ResourceKey.create(Registries.POTION, Identifier.fromNamespaceAndPath(MOD_ID, "to_snow_fox_0_potion")), new Potion(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(TO_SNOW_FOX_0_POTION), 3600)));
    public static final Potion ANUBIS_WOLF_FORM_POTION =
            Registry.register(BuiltInRegistries.POTION, ResourceKey.create(Registries.POTION, Identifier.fromNamespaceAndPath(MOD_ID, "to_anubis_wolf_0_potion")), new Potion(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(TO_ANUBIS_WOLF_0_POTION), 3600)));
    public static final Potion SPIDER_FORM_POTION =
            Registry.register(BuiltInRegistries.POTION, ResourceKey.create(Registries.POTION, Identifier.fromNamespaceAndPath(MOD_ID, "to_spider_0_potion")), new Potion(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(TO_SPIDER_0_POTION), 3600)));

    public static final Potion ALLEY_FORM_POTION =
            Registry.register(BuiltInRegistries.POTION, ResourceKey.create(Registries.POTION, Identifier.fromNamespaceAndPath(MOD_ID, "to_allay_sp_potion")), new Potion(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(TO_ALLAY_SP_POTION), 3600)));
    public static final Potion FERAL_CAT_FORM_POTION =
            Registry.register(BuiltInRegistries.POTION, ResourceKey.create(Registries.POTION, Identifier.fromNamespaceAndPath(MOD_ID, "to_feral_cat_sp_potion")), new Potion(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(TO_FERAL_CAT_SP_POTION), 3600)));
    public static final Potion CUSTOM_STATUE_FORM_POTION =
            Registry.register(BuiltInRegistries.POTION, ResourceKey.create(Registries.POTION, Identifier.fromNamespaceAndPath(MOD_ID, "to_custom_statue_potion")), new Potion(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(TO_CUSTOM_STATUE_POTION), 3600)));

    /* 未支持数据包时代的占位形态 现在可以使用数据添加形态了
    // custom empty forms
    public static final Potion ALPHA_FORM_POTION =
            Registry.register(Registries.POTION, RegistryKey.of(RegistryKeys.POTION, Identifier.of(MOD_ID, "to_alpha_0_potion")), new Potion(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(TO_ALPHA_0_POTION), 3600)));
    public static final Potion BETA_FORM_POTION =
            Registry.register(Registries.POTION, RegistryKey.of(RegistryKeys.POTION, Identifier.of(MOD_ID, "to_beta_0_potion")), new Potion(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(TO_BETA_0_POTION), 3600)));
    public static final Potion GAMMA_FORM_POTION =
            Registry.register(Registries.POTION, RegistryKey.of(RegistryKeys.POTION, Identifier.of(MOD_ID, "to_gamma_0_potion")), new Potion(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(TO_GAMMA_0_POTION), 3600)));
    public static final Potion OMEGA_FORM_POTION =
            Registry.register(Registries.POTION, RegistryKey.of(RegistryKeys.POTION, Identifier.of(MOD_ID, "to_omega_sp_potion")), new Potion(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(TO_OMEGA_SP_POTION), 3600)));
    public static final Potion PSI_FORM_POTION =
            Registry.register(Registries.POTION, RegistryKey.of(RegistryKeys.POTION, Identifier.of(MOD_ID, "to_psi_sp_potion")), new Potion(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(TO_PSI_SP_POTION), 3600)));
    public static final Potion CHI_FORM_POTION =
            Registry.register(Registries.POTION, RegistryKey.of(RegistryKeys.POTION, Identifier.of(MOD_ID, "to_chi_sp_potion")), new Potion(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(TO_CHI_SP_POTION), 3600)));
    public static final Potion PHI_FORM_POTION =
            Registry.register(Registries.POTION, RegistryKey.of(RegistryKeys.POTION, Identifier.of(MOD_ID, "to_phi_sp_potion")), new Potion(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(TO_PHI_SP_POTION), 3600)));
     */
    // other custom potions
    // feed potion can only be obtained via familiar_fox_2 and familiar_fox_3, no recipe
    public static final Potion FEED_POTION =
            Registry.register(BuiltInRegistries.POTION, ResourceKey.create(Registries.POTION, Identifier.fromNamespaceAndPath(MOD_ID, "feed_potion")), new Potion(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(FEED_EFFECT), 3600)));

    public static void registerPotions(){

    }

    public static void registerPotionsRecipes(){
        // 代码内定义配方：走 Fabric 官方酿造注册钩子（1.21 的 PotionBrewing 是不可变 Builder 模型，
        // 原 BrewingRecipeUtils 私有列表只供数据包配方使用，官方 BUILD 事件同时解决材料格校验 + 酿造结果）。
        // BUILD 回调在 MinecraftServer 构造时才执行，此时下方自定义 potion 均已注册，无时序问题。
        // awkward + moondust_matrix = moondust_potion
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
            builder.addMix(Potions.AWKWARD, RegCustomItem.MOONDUST_MATRIX, holder(MOONDUST_POTION));
            builder.addMix(holder(MOONDUST_POTION), Items.POINTED_DRIPSTONE, holder(BAT_FORM_POTION));
            builder.addMix(holder(MOONDUST_POTION), Items.BIG_DRIPLEAF, holder(AXOLOTL_FORM_POTION));
            builder.addMix(holder(MOONDUST_POTION), Items.CHICKEN, holder(OCELOT_FORM_POTION));
            // familiar fox只能通过女巫发射或掉落的溅射药水给与，没有配方
            // The familiar fox can only be obtained via splash potions thrown or drop by witches, no recipe available
            builder.addMix(holder(MOONDUST_POTION), RegCustomItem.ECTOPLASM_RAG, holder(ANUBIS_WOLF_FORM_POTION));
            builder.addMix(holder(MOONDUST_POTION), RegCustomItem.SILK_DEW, holder(SPIDER_FORM_POTION));
            // snow fox 需要通过净化familiar fox药水来得到
            // snow fox can be obtained by purifying familiar fox potion
            builder.addMix(holder(FAMILIAR_FOX_FORM_POTION), Items.GOLD_NUGGET, holder(SNOW_FOX_FORM_POTION));
            builder.addMix(holder(MOONDUST_POTION), Items.AMETHYST_SHARD, holder(ALLEY_FORM_POTION));
            builder.addMix(holder(MOONDUST_POTION), Items.COD_BUCKET, holder(FERAL_CAT_FORM_POTION));
        });
    }

    /** 把 Potion 转 Holder<Potion>（mod 自定义 potion 用 Registry.register 注册为 Potion，addMix 需要 Holder）。 */
    private static Holder<Potion> holder(Potion potion) {
        return BuiltInRegistries.POTION.wrapAsHolder(potion);
    }
}
