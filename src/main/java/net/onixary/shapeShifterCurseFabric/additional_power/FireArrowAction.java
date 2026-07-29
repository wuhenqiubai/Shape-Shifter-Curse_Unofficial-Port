package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

import java.util.function.Consumer;

public class FireArrowAction {
    public static void spawnFireArrow(LivingEntity owner, float Damage, float Speed, float Spread, int FireTime, boolean NoGravity, boolean Critical, boolean hasOwner, Consumer<Entity> projectileAction) {
        ArrowItem arrowItem = (ArrowItem)(Items.ARROW);
        ItemStack itemStack = new ItemStack(arrowItem);
        AbstractArrow persistentProjectileEntity = arrowItem.createArrow(owner.level(), itemStack, owner, ItemStack.EMPTY);
        if (FireTime > 0) {
            persistentProjectileEntity.igniteForSeconds(FireTime);
        }
        if (NoGravity) {
            persistentProjectileEntity.setNoGravity(true);  // 危险设计 容易制作卡服机 见烈焰弹卡服务器方法
        }
        persistentProjectileEntity.shootFromRotation(owner, owner.getXRot(), owner.getYRot(), 0.0F, Speed, Spread);
        persistentProjectileEntity.setBaseDamage(Damage);
        if (Critical) {
            persistentProjectileEntity.setCritArrow(true);
        }
        persistentProjectileEntity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        boolean success = owner.level().addFreshEntity(persistentProjectileEntity);
        if (success) {
            if (projectileAction != null) {
                projectileAction.accept(persistentProjectileEntity);
            }
        }
    }

    public static void registerAction(Consumer<ActionFactory<Entity>> ActionRegister, Consumer<ActionFactory<Tuple<Entity, Entity>>> BIActionRegister) {
        ActionRegister.accept(new ActionFactory<Entity>(
			    ShapeShifterCurseFabric.identifier("fire_arrow"),
			    new SerializableData()
					    .add("damage", SerializableDataTypes.FLOAT, 2.0f)
					    .add("speed", SerializableDataTypes.FLOAT, 3.0f)
					    .add("spread", SerializableDataTypes.FLOAT, 0.0f)
					    .add("fire_time", SerializableDataTypes.INT, 0)
					    .add("no_gravity", SerializableDataTypes.BOOLEAN, false)
					    .add("critical", SerializableDataTypes.BOOLEAN, false)
					    .add("has_owner", SerializableDataTypes.BOOLEAN, true)
					    .add("projectile_action", ApoliDataTypes.ENTITY_ACTION, null)
					    .add("count", SerializableDataTypes.INT, 1),
			    (data, e) -> {
				    if (e instanceof LivingEntity livingEntity) {
					    float damage = data.get("damage");
					    float speed = data.get("speed");
					    float spread = data.get("spread");
					    int fireTime = data.get("fire_time");
					    boolean noGravity = data.get("no_gravity");
					    boolean critical = data.get("critical");
					    boolean hasOwner = data.get("has_owner");
					    Consumer<Entity> projectileAction = data.get("projectile_action");
					    int count = data.get("count");
					    for (int i = 0; i < count; i++) {
						    spawnFireArrow(livingEntity, damage, speed, spread, fireTime, noGravity, critical, hasOwner, projectileAction);
					    }
                }}));
    }
}