package net.onixary.shapeShifterCurseFabric.status_effects;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.status_effects.other_effects.EntangledEffect;
import net.onixary.shapeShifterCurseFabric.status_effects.other_effects.FeedEffect;
import net.onixary.shapeShifterCurseFabric.status_effects.other_effects.ImmobilityEffect;
import net.onixary.shapeShifterCurseFabric.status_effects.other_effects.SimpleStatusEffect;

public class RegOtherStatusEffects {
    private RegOtherStatusEffects(){}

    //public static final BaseTransformativeStatusEffect EMPTY_EFFECT = register("empty_effect",new BaseTransformativeStatusEffect(null, StatusEffectCategory.NEUTRAL, 0xFFFFFF, false) );
    public static final ImmobilityEffect IMMOBILITY_EFFECT = register("immobility_effect",new ImmobilityEffect());
    public static final FeedEffect FEED_EFFECT = register("feed_effect", new FeedEffect());

    // 裹茧1级效果不手动减速，使用减速效果
    public static final MobEffect ENTANGLED_EFFECT = register("entangled_effect", new EntangledEffect(MobEffectCategory.HARMFUL, 0x9F9F9F));
    public static final MobEffect ENTANGLED_FULL_EFFECT  = register("entangled_full_effect", new SimpleStatusEffect(MobEffectCategory.HARMFUL, 0xFFFFFF)
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "entangled_full_speed"), -1.0F, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            .addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "entangled_full_knockback"), 100.0F, AttributeModifier.Operation.ADD_VALUE)
            .addAttributeModifier(Attributes.ATTACK_SPEED, Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "entangled_full_attack_speed"), -0.8F, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
    );

    public static <T extends MobEffect> T register(String path, T effect) {
        return Registry.register(BuiltInRegistries.MOB_EFFECT, ResourceKey.create(Registries.MOB_EFFECT, Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, path)), effect);
    }

    public static void initialize() {}
}