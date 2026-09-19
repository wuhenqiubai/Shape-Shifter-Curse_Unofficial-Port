package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.registry.ApoliRegistries;
import net.minecraft.core.Holder;
import net.minecraft.util.Tuple;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.material.FluidState;

public class ConditionTypes {

    public static ConditionType<Entity> ENTITY = new ConditionType<>("EntityCondition", ApoliRegistries.ENTITY_CONDITION);
    public static ConditionType<Tuple<Entity, Entity>> BIENTITY = new ConditionType<>("BiEntityCondition", ApoliRegistries.BIENTITY_CONDITION);
    public static ConditionType<ItemStack> ITEM = new ConditionType<>("ItemCondition", ApoliRegistries.ITEM_CONDITION);
    public static ConditionType<BlockInWorld> BLOCK = new ConditionType<>("BlockCondition", ApoliRegistries.BLOCK_CONDITION);
    public static ConditionType<Tuple<DamageSource, Float>> DAMAGE = new ConditionType<>("DamageCondition", ApoliRegistries.DAMAGE_CONDITION);
    public static ConditionType<FluidState> FLUID = new ConditionType<>("FluidCondition", ApoliRegistries.FLUID_CONDITION);
    public static ConditionType<Holder<Biome>> BIOME = new ConditionType<>("BiomeCondition", ApoliRegistries.BIOME_CONDITION);

}
