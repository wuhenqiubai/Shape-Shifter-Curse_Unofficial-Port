package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ItemOnItemPower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.apoli.power.Active;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.inventory.Slot;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.nbt.LongTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.items.FamiliarFoxContent;
import net.onixary.shapeShifterCurseFabric.mana.ManaUtils;
import net.onixary.shapeShifterCurseFabric.perk.NormalPerk;
import net.onixary.shapeShifterCurseFabric.perk.RegPerks;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;

public class FamiliarFoxPerkCheck {
    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("shape-shifter-curse", path); }
    private static ServerPlayer player(GameTestHelper context) {
        var player = context.makeMockServerPlayerInLevel();
        for (int i = 0; i < 61; i++) player.tick();
        player.getAbilities().invulnerable = false;
        player.setInvulnerable(false);
        player.setPos(context.absoluteVec(new Vec3(0.5, 1, 0.5)));
        ManaUtils.gainManaTypeID(player, id("familiar_fox_mana"), id("fox_test"));
        ManaUtils.setPlayerMana(player, 50);
        return player;
    }
    private static Power add(ServerPlayer player, String perk) {
        var type = PowerTypeRegistry.get(id("perks/familiar_fox_" + perk));
        var holder = PowerHolderComponent.KEY.get(player);
        holder.addPower(type, id("fox_test"));
        return holder.getPower(type);
    }
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void registrations(GameTestHelper context) {
        var tree = RegPerks.getPerkTree(id("familiar_fox_3_perk_tree"));
        context.assertTrue(tree.getAllNodes().size() == 11, "Eleven fox perks");
        context.assertTrue(RegPlayerForms.FAMILIAR_FOX_3.getPerkTreeID().equals(tree.getID()), "Fox tree binding");
        for (var node : tree.getAllNodes()) {
            var perk = (NormalPerk) RegPerks.getPerk(node.perkID);
            for (var power : perk.powerAdd) context.assertTrue(PowerTypeRegistry.get(power) != null, "Power parsed: " + power);
        }
        context.succeed();
    }
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void capacityAndProjectileSiphon(GameTestHelper context) {
        var player = player(context);
        var capacity = add(player, "mana_capacity_1");
        context.assertTrue(ManaUtils.getPlayerMaxMana(player) == 125, "Capacity I");
        PowerHolderComponent.KEY.get(player).removePower(capacity.getType(), id("fox_test"));
        add(player, "mana_capacity_2");
        context.assertTrue(ManaUtils.getPlayerMaxMana(player) == 150, "Capacity II replaces I");
        add(player, "siphon_2");
        var zombie = context.spawn(EntityType.ZOMBIE, new BlockPos(0, 1, 3));
        var arrow = new Arrow(context.getLevel(), player, new ItemStack(Items.ARROW), null);
        zombie.hurt(player.damageSources().arrow(arrow, player), 3);
        context.assertTrue(ManaUtils.getPlayerMana(player) == 54, "Projectile hits restore mana");
        zombie.hurt(player.damageSources().arrow(arrow, player), 3);
        context.assertTrue(ManaUtils.getPlayerMana(player) == 54, "Rejected damage cannot farm mana");
        player.discard();
        context.succeed();
    }
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void deflectionPayment(GameTestHelper context) {
        var player = player(context);
        add(player, "deflection_reduction"); add(player, "deflection_payment");
        var skeleton = context.spawn(EntityType.SKELETON, new BlockPos(0, 1, 3));
        var arrow = new Arrow(context.getLevel(), skeleton, new ItemStack(Items.ARROW), null);
        var source = player.damageSources().arrow(arrow, skeleton);
        player.hurt(source, 8);
        context.assertTrue(player.getHealth() == 16, "Projectile damage halved: health=" + player.getHealth() + ", mana=" + ManaUtils.getPlayerMana(player));
        context.assertTrue(ManaUtils.getPlayerMana(player) == 45, "Successful hit costs five");
        player.hurt(source, 8);
        context.assertTrue(ManaUtils.getPlayerMana(player) == 45, "Invulnerability frame does not cost mana");
        player.invulnerableTime = 0; ManaUtils.setPlayerMana(player, 5);
        player.hurt(source, 8);
        context.assertTrue(player.getHealth() == 8 && ManaUtils.getPlayerMana(player) == 5, "Exactly five mana does not activate protection");
        player.discard();
        context.succeed();
    }
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void shieldRetaliation(GameTestHelper context) {
        var player = player(context);
        add(player, "return_shield");
        player.setYRot(0); player.setXRot(0); player.setShiftKeyDown(true);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHIELD));
        player.startUsingItem(InteractionHand.MAIN_HAND);
        for (int i = 0; i < 6; i++) player.doTick();
        context.assertTrue(player.isBlocking(), "Shield raised");
        var skeleton = context.spawn(EntityType.SKELETON, new BlockPos(0, 1, 3));
        var arrow = new Arrow(context.getLevel(), skeleton, new ItemStack(Items.ARROW), null); arrow.setPos(skeleton.position());
        float before = player.getHealth();
        player.hurt(player.damageSources().arrow(arrow, skeleton), 6);
        context.assertTrue(player.getHealth() == before, "Shield blocks damage");
        context.assertTrue(ManaUtils.getPlayerMana(player) == 45, "Blocked projectile costs five mana");
        var fireballs = context.getLevel().getEntities(EntityType.SMALL_FIREBALL, player.getBoundingBox().inflate(3), e -> e.getOwner() == player);
        context.assertTrue(fireballs.size() == 1 && fireballs.get(0).getDeltaMovement().z > 0, "One fireball aimed at attacker");
        player.discard();
        context.succeed();
    }
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void surfaceRingRewards(GameTestHelper context) {
        var player = player(context);
        player.setYRot(0); player.setXRot(0);
        player.getFoodData().setFoodLevel(8); player.getFoodData().setSaturation(0);
        add(player, "siphon_2");
        var charge = (ChargePower) add(player, "siphoning_ring");
        context.setBlock(new BlockPos(0, 2, 5), Blocks.STONE);
        var target = context.spawn(EntityType.ZOMBIE, new BlockPos(0, 1, 4));
        charge.onUse(); charge.onUse();
        context.assertTrue(ManaUtils.getPlayerMana(player) == 30, "Holding costs twenty once");
        charge.fire(false);
        context.assertTrue(target.getHealth() < 13, "Surface ring damages enemy");
        context.assertTrue(ManaUtils.getPlayerMana(player) == 34, "Ring also triggers siphon");
        context.assertTrue(player.getFoodData().getFoodLevel() == 14, "Hit restores six food");
        context.assertTrue(Math.abs(player.getFoodData().getSaturationLevel() - 4.8f) < 0.001f, "Hit restores 4.8 saturation");
        charge.onUse();
        context.assertTrue(ManaUtils.getPlayerMana(player) == 34, "Cooldown prevents new charge");
        player.discard();
        context.succeed();
    }
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void potionCharmCraftingAndUse(GameTestHelper context) {
        var player = player(context);
        var craft = (ItemOnItemPower) add(player, "potion_charms_healing");
        var paper = new ItemStack(Items.PAPER, 2);
        var ingredient = new ItemStack(Items.GLISTERING_MELON_SLICE, 2);
        player.getInventory().setItem(0, ingredient);
        context.assertTrue(craft.doesApply(paper, ingredient), "Paper accepts healing ingredient");
        var result = craft.execute(paper, ingredient, new Slot(player.getInventory(), 0, 0, 0));
        context.assertTrue(player.getInventory().countItem(FamiliarFoxContent.HEALING_CHARM) == 1 && paper.getCount() == 1 && ingredient.getCount() == 1, "Craft consumes one of each ingredient");
        context.assertTrue(ManaUtils.getPlayerMana(player) == 45, "Craft costs five mana");
        result = new ItemStack(FamiliarFoxContent.HEALING_CHARM);
        player.setItemInHand(InteractionHand.MAIN_HAND, result);
        context.assertTrue(!result.use(context.getLevel(), player, InteractionHand.MAIN_HAND).getResult().consumesAction(), "Other forms cannot use charm");
        FormUtils.setForm(player, RegPlayerForms.FAMILIAR_FOX_3);
        result.use(context.getLevel(), player, InteractionHand.MAIN_HAND);
        var arrows = context.getLevel().getEntities(FamiliarFoxContent.POTION_CHARM_ARROW, player.getBoundingBox().inflate(3), e -> e.getOwner() == player);
        context.assertTrue(arrows.size() == 1, "Fox launches one charm arrow");
        context.assertTrue(arrows.get(0).pickup == AbstractArrow.Pickup.DISALLOWED, "Charm cannot be recovered");
        // ⚠ 1.21.1 起 AbstractArrow 把拾取栈存为 "item" 键（ItemStack 的组件格式），
        // 不再是 1.20 时代的 "Potion" 字符串键。断言含义不变，只是换读取方式。
        var nbt = new CompoundTag(); arrows.get(0).addAdditionalSaveData(nbt);
        ItemStack stored = ItemStack.parseOptional(context.getLevel().registryAccess(), nbt.getCompound("item"));
        PotionContents charmPotion = stored.get(DataComponents.POTION_CONTENTS);
        context.assertTrue(charmPotion != null && charmPotion.is(Potions.STRONG_HEALING), "Strong potion payload retained");
        player.discard();
        context.succeed();
    }
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void reservoirLifetime(GameTestHelper context) {
        var player = player(context);
        FormUtils.setForm(player, RegPlayerForms.FAMILIAR_FOX_3);
        ManaUtils.setPlayerMana(player, 50);
        player.setYRot(0); player.setXRot(0); player.getFoodData().setFoodLevel(10);
        var summon = add(player, "reservoir"); summon.fromTag(LongTag.valueOf(-1000), context.getLevel().registryAccess());
        ((Active) summon).onUse();
        var entities = context.getLevel().getEntities(FamiliarFoxContent.MANA_RESERVOIR, player.getBoundingBox().inflate(4), e -> true);
        context.assertTrue(entities.size() == 1, "One reservoir spawned");
        context.assertTrue(player.getFoodData().getFoodLevel() == 4, "Summon costs six food");
        var reservoir = entities.get(0);
        reservoir.setAge(20); reservoir.tick();
        context.assertTrue(reservoir.getHealth() == 8 && ManaUtils.getPlayerMana(player) == 55, "Pulse gives mana and loses health: age=" + reservoir.getAge() + ", hp=" + reservoir.getHealth() + ", mana=" + ManaUtils.getPlayerMana(player));
        player.setPos(player.position().add(10, 0, 0));
        for (int i = 0; i < 4; i++) { reservoir.setAge(20); reservoir.tick(); }
        context.assertTrue(!reservoir.isAlive(), "Empty pulses still expire after five ticks of service");
        context.assertTrue(ManaUtils.getPlayerMana(player) == 55, "Out of range players receive no mana");
        player.discard();
        context.succeed();
    }
}
