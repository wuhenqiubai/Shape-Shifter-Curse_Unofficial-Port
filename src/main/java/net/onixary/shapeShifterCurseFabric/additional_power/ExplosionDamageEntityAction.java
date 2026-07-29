package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

import java.util.List;

public class ExplosionDamageEntityAction {
    public static void action(SerializableData.Instance data, Entity entity) {
        int Power = data.getInt("power");
        @SuppressWarnings("unchecked")
        ConditionFactory<Tuple<Entity, Entity>>.Instance entityCondition = data.get("entity_condition");
        @SuppressWarnings("unchecked")
        ActionFactory<Entity>.Instance entityAction = data.get("entity_action");
        boolean explosion_damage_entity = data.get("explosion_damage_entity");
        explosion(entity, Power, entityCondition, entityAction, explosion_damage_entity);
    }

    private static void explosion(Entity entity,
                                  int power,
                                  ConditionFactory<Tuple<Entity, Entity>>.Instance entityCondition,
                                  ActionFactory<Entity>.Instance entityAction,
                                  boolean explosion_damage_entity
    ) {
        Vec3 ExplosionPos = entity.position();
        DamageSource source = entity.level().damageSources().explosion(entity, entity);
        entity.level().gameEvent(entity, GameEvent.EXPLODE, new Vec3(ExplosionPos.x(), ExplosionPos.y(), ExplosionPos.z()));

        float q = power * 2.0F;
        int k = Mth.floor(ExplosionPos.x() - (double)q - 1.0);
        int l = Mth.floor(ExplosionPos.x() + (double)q + 1.0);
        int r = Mth.floor(ExplosionPos.y() - (double)q - 1.0);
        int s = Mth.floor(ExplosionPos.y() + (double)q + 1.0);
        int t = Mth.floor(ExplosionPos.z() - (double)q - 1.0);
        int u = Mth.floor(ExplosionPos.z() + (double)q + 1.0);
        List<Entity> list = entity.level().getEntities(entity, new AABB(k, r, t, l, s, u));
        for (Entity target_entity : list) {
            if (entityCondition == null || entityCondition.test(new Tuple<>(entity, target_entity))) {
			    double w = Math.sqrt(target_entity.distanceToSqr(ExplosionPos)) / (double)q;
			    if (w <= 1.0) {
				    double x = target_entity.getX() - ExplosionPos.x();
				    double y = (target_entity instanceof PrimedTnt ? target_entity.getY() : target_entity.getEyeY()) - ExplosionPos.y();
				    double z = target_entity.getZ() - ExplosionPos.z();
				    double aa = Math.sqrt(x * x + y * y + z * z);
				    if (aa != 0.0) {
					    x /= aa;
					    y /= aa;
					    z /= aa;
                        double ab = ServerExplosion.getSeenPercent(ExplosionPos, target_entity);
					    double ac = (1.0 - w) * ab;
                        if(explosion_damage_entity){
                            target_entity.hurt(source, (float)((int)((ac * ac + ac) / 2.0 * 7.0 * (double)q + 1.0)));
					    }
					    double ad;
					    if (target_entity instanceof LivingEntity livingEntity) {
							Holder<Enchantment> blastProt = entity.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.BLAST_PROTECTION);
							int blastProtLevel = EnchantmentHelper.getEnchantmentLevel(blastProt, livingEntity);
							ad = blastProtLevel > 0 ? ac * Mth.clamp(1.0 - (double)blastProtLevel * 0.15, 0.0, 1.0) : ac;
					    } else {
						    ad = ac;
					    }
					    x *= ad;
					    y *= ad;
					    z *= ad;
					    Vec3 vec3d2 = new Vec3(x, y, z);
					    target_entity.setDeltaMovement(target_entity.getDeltaMovement().add(vec3d2));
					    // 加入额外可选的EntityAction
					    if (entityAction != null) {
						    entityAction.accept(target_entity);
					    }
				    }
			    }
		    }
	    }
    }

    public static ActionFactory<Entity> createFactory() {
        return new ActionFactory<>(
                ShapeShifterCurseFabric.identifier("explosion_damage_entity"),
                new SerializableData()
                        .add("power", SerializableDataTypes.INT, 0)
                        .add("entity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                        .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                        .add("explosion_damage_entity", SerializableDataTypes.BOOLEAN, true),

                ExplosionDamageEntityAction::action
        );
    }
}