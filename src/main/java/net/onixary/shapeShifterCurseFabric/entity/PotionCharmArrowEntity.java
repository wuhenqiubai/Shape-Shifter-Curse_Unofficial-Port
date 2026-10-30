package net.onixary.shapeShifterCurseFabric.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.onixary.shapeShifterCurseFabric.items.RegCustomItem;
import org.jetbrains.annotations.NotNull;

/** Renders the thrown charm; healing charms apply their potion without arrow impact damage. */
public class PotionCharmArrowEntity extends Arrow implements ItemSupplier {
    private static final EntityDataAccessor<ItemStack> CHARM_STACK = SynchedEntityData.defineId(PotionCharmArrowEntity.class, EntityDataSerializers.ITEM_STACK);

    public PotionCharmArrowEntity(EntityType<? extends PotionCharmArrowEntity> type, Level world) {
        super(type, world);
        pickup = Pickup.DISALLOWED;
    }
    @Override public void setOwner(Entity owner) {
        super.setOwner(owner);
        pickup = Pickup.DISALLOWED;
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CHARM_STACK, new ItemStack(Items.PAPER));
    }

    public void setCharmStack(ItemStack stack) {
        entityData.set(CHARM_STACK, stack.copyWithCount(1));
    }

    @Override public @NotNull ItemStack getItem() { return entityData.get(CHARM_STACK); }

    @Override protected void onHitEntity(EntityHitResult hit) {
        if (!getItem().is(RegCustomItem.HEALING_CHARM)) {
            super.onHitEntity(hit);
            return;
        }
        if (!level().isClientSide()) {
            if (hit.getEntity() instanceof LivingEntity target) {
                doPostHurtEffects(target);
            }
            discard();
        }
    }

    public void initFromStack(ItemStack stack) {
        this.setPickupItemStack(stack);
    }

    @Override public void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("CharmStack", ItemStack.CODEC, getItem());
    }

    @Override public void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
        super.readAdditionalSaveData(input);
        input.read("CharmStack", ItemStack.CODEC).ifPresent(this::setCharmStack);
    }
}
