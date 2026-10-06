package net.onixary.shapeShifterCurseFabric.minion.mobs;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.mana.ManaUtils;
import net.onixary.shapeShifterCurseFabric.minion.MinionBase;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;
import org.jetbrains.annotations.NotNull;

public class ManaReservoirEntity extends MinionBase implements ItemSupplier {
    public ManaReservoirEntity(EntityType<? extends ManaReservoirEntity> type, Level world) {
        super(type, world);
        minionTypeID = ShapeShifterCurseFabric.identifier("mana_reservoir");
        setNoGravity(true);
    }
    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10)
                .add(Attributes.MOVEMENT_SPEED, 0);
    }
    @Override protected void registerGoals() {}
    @Override public AgeableMob getBreedOffspring(ServerLevel world, AgeableMob mate) { return null; }
    @Override public boolean isFood(ItemStack stack) { return false; }
    @Override public @NotNull ItemStack getItem() { return new ItemStack(Items.AMETHYST_SHARD); }
    @Override
    public void tick() {
        setNoGravity(true);
        super.tick();
        // ⚠ 上游 Yarn 写的是 `age`，那里的 `Entity.age` 是 **tick 计数器**，对应 Mojmap `Entity.tickCount`。
        //   不要误映射成同名的 `AgeableMob.age`（那是幼年成长龄，`aiStep` 每 tick 自减，用来做周期判定会错）。
        if (!(level() instanceof ServerLevel world) || !isAlive() || tickCount % 20 != 0) return;
        for (var player : world.players()) {
            var form = FormUtils.getPlayerForm(player).getFormID();
            if (player.isAlive() && !player.isSpectator() && distanceToSqr(player) <= 16
                    && (form.equals(ShapeShifterCurseFabric.identifier("familiar_fox_2"))
                    || form.equals(ShapeShifterCurseFabric.identifier("familiar_fox_3")))) {
                ManaUtils.gainPlayerMana(player, 5);
            }
        }
        // Lifetime cost bypasses invulnerability frames and still runs without recipients.
        if (getHealth() <= 2) kill(); else setHealth(getHealth() - 2);
    }
}
