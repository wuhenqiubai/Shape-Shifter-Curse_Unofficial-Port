package io.github.apace100.apoli.power.factory.action.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.material.FluidState;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Optional;
import java.util.function.Predicate;

public class ExplodeAction {

    public static void action(SerializableData.Instance data, Triple<Level, BlockPos, Direction> block) {
        if(block.getLeft().isClientSide) {
            return;
        }

        Predicate<BlockInWorld> indestructible = null;
        if(data.isPresent("indestructible")) {
            indestructible = MiscUtil.combineOr(indestructible, data.get("indestructible"));
        }
        if(data.isPresent("destructible")) {
            Predicate<BlockInWorld> destructibleCondition = data.get("destructible");
            indestructible = MiscUtil.combineOr(indestructible, destructibleCondition.negate());
        }

        if(indestructible != null) {
            ExplosionDamageCalculator eb = getExplosionBehaviour(block.getLeft(), indestructible);
            explode(block.getLeft(), null,
                block.getLeft().damageSources().explosion(null),
                eb, block.getMiddle().getX() + 0.5, block.getMiddle().getY() + 0.5, block.getMiddle().getZ() + 0.5,
                data.getFloat("power"), data.getBoolean("create_fire"),
                data.get("destruction_type"));
        } else {
            explode(block.getLeft(),null, block.getLeft().damageSources().explosion(null), null,
                block.getMiddle().getX() + 0.5, block.getMiddle().getY() + 0.5, block.getMiddle().getZ() + 0.5,
                data.getFloat("power"), data.getBoolean("create_fire"),
                data.get("destruction_type"));
        }
    }

    private static void explode(Level world, Entity entity, DamageSource damageSource, ExplosionDamageCalculator behavior, double x, double y, double z, float power, boolean createFire, Explosion.BlockInteraction destructionType) {
        Explosion explosion = new Explosion(world, entity, damageSource, behavior, x, y, z, power, createFire, destructionType, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, SoundEvents.GENERIC_EXPLODE);
        explosion.explode();
        explosion.finalizeExplosion(true);
    }

    private static ExplosionDamageCalculator getExplosionBehaviour(Level world, Predicate<BlockInWorld> indestructiblePredicate) {
        return new ExplosionDamageCalculator() {
            @Override
            public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter blockView, BlockPos pos, BlockState blockState, FluidState fluidState) {
                BlockInWorld cbp = new BlockInWorld(world, pos, true);
                Optional<Float> def = super.getBlockExplosionResistance(explosion, world, pos, blockState, fluidState);
                Optional<Float> ovr = indestructiblePredicate.test(cbp) ?
                    Optional.of(Blocks.WATER.getExplosionResistance()) : Optional.empty();
                return ovr.isPresent() ? def.isPresent() ? def.get() > ovr.get() ? def : ovr : ovr : def;
            }
        };
    }

    public static ActionFactory<Triple<Level, BlockPos, Direction>> getFactory() {
        return new ActionFactory<>(Apoli.identifier("explode"),
            new SerializableData()
                .add("power", SerializableDataTypes.FLOAT)
                .add("destruction_type", ApoliDataTypes.BACKWARDS_COMPATIBLE_DESTRUCTION_TYPE, Explosion.BlockInteraction.DESTROY)
                .add("indestructible", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("destructible", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("create_fire", SerializableDataTypes.BOOLEAN, false),
            ExplodeAction::action
        );
    }
}