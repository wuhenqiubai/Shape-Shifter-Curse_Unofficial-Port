package net.onixary.shapeShifterCurseFabric.items;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/** Vanilla tipped-arrow behavior with a replaceable item-shaped placeholder renderer. */
public class PotionCharmArrowEntity extends Arrow implements ItemSupplier {
    public PotionCharmArrowEntity(EntityType<? extends PotionCharmArrowEntity> type, Level world) {
        super(type, world);
        pickup = Pickup.DISALLOWED;
    }
    @Override public void setOwner(Entity owner) {
        super.setOwner(owner);
        // AbstractArrow enables pickup when a player owner is assigned.
        pickup = Pickup.DISALLOWED;
    }
    @Override public @NotNull ItemStack getItem() { return new ItemStack(Items.PAPER); }

    /**
     * 对应上游（Yarn {@code PersistentProjectileEntity.initFromStack}）的公开入口。
     *
     * <p>Mojmap 1.21.1 里对应的是 {@code AbstractArrow.setPickupItemStack(ItemStack)}，
     * 但它是 <b>protected</b>，外部（{@code PotionCharmItem}）够不着，故在此转发一次。
     * 传入的栈会作为本实体的 pickupItemStack 参与 NBT 存取（渲染仍走上面被覆盖的 {@code getItem()}）。</p>
     */
    public void initFromStack(ItemStack stack) {
        this.setPickupItemStack(stack);
    }
}
