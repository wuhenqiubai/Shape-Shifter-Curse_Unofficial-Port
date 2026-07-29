package net.onixary.shapeShifterCurseFabric.status_effects;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.status_effects.transformative_effects.TransformativeStatusPotion;

public class RegTStatusPotionEffect {
    private RegTStatusPotionEffect(){}

    public static final MobEffect TO_BAT_0_POTION = register("to_bat_0_potion",new TransformativeStatusPotion(RegTStatusEffect.TO_BAT_0_EFFECT));
    public static final MobEffect TO_AXOLOTL_0_POTION = register("to_axolotl_0_potion", new TransformativeStatusPotion(RegTStatusEffect.TO_AXOLOTL_0_EFFECT));
    public static final MobEffect TO_OCELOT_0_POTION = register("to_ocelot_0_potion", new TransformativeStatusPotion(RegTStatusEffect.TO_OCELOT_0_EFFECT));
    public static final MobEffect TO_FAMILIAR_FOX_0_POTION = register("to_familiar_fox_0_potion", new TransformativeStatusPotion(RegTStatusEffect.TO_FAMILIAR_FOX_0_EFFECT));
    public static final MobEffect TO_SNOW_FOX_0_POTION = register("to_snow_fox_0_potion", new TransformativeStatusPotion(RegTStatusEffect.TO_SNOW_FOX_0_EFFECT));
    public static final MobEffect TO_ANUBIS_WOLF_0_POTION = register("to_anubis_wolf_0_potion", new TransformativeStatusPotion(RegTStatusEffect.TO_ANUBIS_WOLF_0_EFFECT));
    public static final MobEffect TO_SPIDER_0_POTION = register("to_spider_0_potion", new TransformativeStatusPotion(RegTStatusEffect.TO_SPIDER_0_EFFECT));
    public static final MobEffect TO_ALLAY_SP_POTION = register("to_allay_sp_potion", new TransformativeStatusPotion(RegTStatusEffect.TO_ALLAY_SP_EFFECT));
    public static final MobEffect TO_FERAL_CAT_SP_POTION = register("to_feral_cat_sp_potion", new TransformativeStatusPotion(RegTStatusEffect.TO_FERAL_CAT_SP_EFFECT));

    public static final MobEffect TO_CUSTOM_STATUE_POTION = register("to_custom_statue_potion", new TransformativeStatusPotion(RegTStatusEffect.TO_CUSTOM_STATUE_EFFECT));

    /* 未支持数据包时代的占位形态 现在可以使用数据添加形态了
    // empty custom forms
    public static final StatusEffect TO_ALPHA_0_POTION = register("to_alpha_0_potion", new TransformativeStatusPotion(RegTStatusEffect.TO_ALPHA_0_EFFECT));
    public static final StatusEffect TO_BETA_0_POTION = register("to_beta_0_potion", new TransformativeStatusPotion(RegTStatusEffect.TO_BETA_0_EFFECT));
    public static final StatusEffect TO_GAMMA_0_POTION = register("to_gamma_0_potion", new TransformativeStatusPotion(RegTStatusEffect.TO_GAMMA_0_EFFECT));
    public static final StatusEffect TO_OMEGA_SP_POTION = register("to_omega_sp_potion", new TransformativeStatusPotion(RegTStatusEffect.TO_OMEGA_SP_EFFECT));
    public static final StatusEffect TO_PSI_SP_POTION = register("to_psi_sp_potion", new TransformativeStatusPotion(RegTStatusEffect.TO_PSI_SP_EFFECT));
    public static final StatusEffect TO_CHI_SP_POTION = register("to_chi_sp_potion", new TransformativeStatusPotion(RegTStatusEffect.TO_CHI_SP_EFFECT));
    public static final StatusEffect TO_PHI_SP_POTION = register("to_phi_sp_potion", new TransformativeStatusPotion(RegTStatusEffect.TO_PHI_SP_EFFECT));
     */

    public static <T extends MobEffect> T register(String path, T effect) {
        return Registry.register(BuiltInRegistries.MOB_EFFECT, ResourceKey.create(Registries.MOB_EFFECT, Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, path)), effect);
    }

    public static void initialize() {}
}