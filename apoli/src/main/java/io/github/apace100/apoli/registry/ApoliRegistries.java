package io.github.apace100.apoli.registry;

import com.mojang.datafixers.util.Pair;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.modifier.IModifierOperation;
import io.github.apace100.calio.ClassUtil;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.material.FluidState;
import org.apache.commons.lang3.tuple.Triple;

public class ApoliRegistries {

    public static final Registry<PowerFactory> POWER_FACTORY;
    public static final Registry<ConditionFactory<Entity>> ENTITY_CONDITION;
    public static final Registry<ConditionFactory<Pair<Entity, Entity>>> BIENTITY_CONDITION;
    public static final Registry<ConditionFactory<ItemStack>> ITEM_CONDITION;
    public static final Registry<ConditionFactory<BlockInWorld>> BLOCK_CONDITION;
    public static final Registry<ConditionFactory<Pair<DamageSource, Float>>> DAMAGE_CONDITION;
    public static final Registry<ConditionFactory<FluidState>> FLUID_CONDITION;
    public static final Registry<ConditionFactory<Holder<Biome>>> BIOME_CONDITION;
    public static final Registry<ActionFactory<Entity>> ENTITY_ACTION;
    public static final Registry<ActionFactory<Pair<Level, ItemStack>>> ITEM_ACTION;
    public static final Registry<ActionFactory<Triple<Level, BlockPos, Direction>>> BLOCK_ACTION;
    public static final Registry<ActionFactory<Pair<Entity, Entity>>> BIENTITY_ACTION;
    public static final Registry<IModifierOperation> MODIFIER_OPERATION;

    static {
        POWER_FACTORY = FabricRegistryBuilder.create(PowerFactory.class, Apoli.identifier("power_factory")).buildAndRegister();
        ENTITY_CONDITION = FabricRegistryBuilder.create(ClassUtil.<ConditionFactory<Entity>>castClass(ConditionFactory.class), Apoli.identifier("entity_condition")).buildAndRegister();
        BIENTITY_CONDITION = FabricRegistryBuilder.create(ClassUtil.<ConditionFactory<Pair<Entity, Entity>>>castClass(ConditionFactory.class), Apoli.identifier("bientity_condition")).buildAndRegister();
        ITEM_CONDITION = FabricRegistryBuilder.create(ClassUtil.<ConditionFactory<ItemStack>>castClass(ConditionFactory.class), Apoli.identifier("item_condition")).buildAndRegister();
        BLOCK_CONDITION = FabricRegistryBuilder.create(ClassUtil.<ConditionFactory<BlockInWorld>>castClass(ConditionFactory.class), Apoli.identifier("block_condition")).buildAndRegister();
        DAMAGE_CONDITION = FabricRegistryBuilder.create(ClassUtil.<ConditionFactory<Pair<DamageSource, Float>>>castClass(ConditionFactory.class), Apoli.identifier("damage_condition")).buildAndRegister();
        FLUID_CONDITION = FabricRegistryBuilder.create(ClassUtil.<ConditionFactory<FluidState>>castClass(ConditionFactory.class), Apoli.identifier("fluid_condition")).buildAndRegister();
        BIOME_CONDITION = FabricRegistryBuilder.create(ClassUtil.<ConditionFactory<Holder<Biome>>>castClass(ConditionFactory.class), Apoli.identifier("biome_condition")).buildAndRegister();
        ENTITY_ACTION = FabricRegistryBuilder.create(ClassUtil.<ActionFactory<Entity>>castClass(ActionFactory.class), Apoli.identifier("entity_action")).buildAndRegister();
        ITEM_ACTION = FabricRegistryBuilder.create(ClassUtil.<ActionFactory<Pair<Level, ItemStack>>>castClass(ActionFactory.class), Apoli.identifier("item_action")).buildAndRegister();
        BLOCK_ACTION = FabricRegistryBuilder.create(ClassUtil.<ActionFactory<Triple<Level, BlockPos, Direction>>>castClass(ActionFactory.class), Apoli.identifier("block_action")).buildAndRegister();
        BIENTITY_ACTION = FabricRegistryBuilder.create(ClassUtil.<ActionFactory<Pair<Entity, Entity>>>castClass(ActionFactory.class), Apoli.identifier("bientity_action")).buildAndRegister();
        MODIFIER_OPERATION = FabricRegistryBuilder.create(IModifierOperation.class, Apoli.identifier("modifier_operation")).buildAndRegister();
    }
}
