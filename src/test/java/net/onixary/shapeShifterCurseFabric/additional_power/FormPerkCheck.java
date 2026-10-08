package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.EntityGlowPower;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.apoli.power.Active;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.LongTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.perk.NormalPerk;
import net.onixary.shapeShifterCurseFabric.perk.PerkUtils;
import net.onixary.shapeShifterCurseFabric.perk.RegPerks;
import net.onixary.shapeShifterCurseFabric.player_form.NormalForm;
import net.onixary.shapeShifterCurseFabric.player_form.NormalSubForm;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;
import net.onixary.shapeShifterCurseFabric.util.util.cost.BaseCost;
import net.onixary.shapeShifterCurseFabric.util.util.cost.ItemCost;
import net.onixary.shapeShifterCurseFabric.util.util.cost.RegCostType;

public class FormPerkCheck {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void collectWaterReleaseAndLandPropulsion(GameTestHelper c) {
        var p=c.makeMockPlayer(GameType.SURVIVAL);
        p.setPos(c.absoluteVec(new Vec3(.5,1,.5)));p.setYRot(0);
        var holder=PowerHolderComponent.KEY.get(p);
        var chargeType=PowerTypeRegistry.get(id("perks/axolotl_collect_water_charge"));
        var glowType=PowerTypeRegistry.get(id("perks/axolotl_collect_water_glow"));
        holder.addPower(chargeType,SOURCE);holder.addPower(glowType,SOURCE);
        var charge=(ChargePower)holder.getPower(chargeType);
        var glow=(EntityGlowPower)holder.getPower(glowType);
        var a=c.spawn(EntityType.ZOMBIE,new BlockPos(8,1,0));
        var b=c.spawn(EntityType.ZOMBIE,new BlockPos(-2,1,0));
        var far=c.spawn(EntityType.ZOMBIE,new BlockPos(11,1,0));
        var friend=c.spawn(EntityType.COW,new BlockPos(0,1,2));
        // Isolate the enlarged area from entities and walls in neighbouring GameTests.
        for (var entity : new Entity[]{p,a,b,far,friend}) entity.setPos(entity.position().add(0,100,0));
        p.setAirSupply(100);charge.onUse();charge.onUse();
        c.assertTrue(glow.isActive() && glow.doesApply(a) && glow.doesApply(b),"Hold previews enemies on both sides");
        c.assertTrue(!glow.doesApply(far) && !glow.doesApply(friend),"Preview excludes distant and friendly targets");
        c.assertTrue(a.getHealth()==20 && p.getAirSupply()==100,"Holding neither attacks nor refunds");
        charge.fire(false);
        c.assertTrue(a.getHealth()<20 && b.getHealth()<20 && far.getHealth()==20 && friend.getHealth()==10,"Release matches preview targets");
        c.assertTrue(a.getDeltaMovement().x>0 && b.getDeltaMovement().x<0 && a.getDeltaMovement().y>0,"Launches outward and upward");
        c.assertTrue(p.getAirSupply()==160 && charge.nowCooldown==600 && !glow.isActive(),"Thirty moisture per hit and thirty second cooldown");
        charge.onUse();c.assertTrue(!charge.isCharging(),"Cooldown blocks restart");
        var jumpType=PowerTypeRegistry.get(id("perks/axolotl_propulsion_efficiency"));holder.addPower(jumpType,SOURCE);
        var jump=(ActionOnJumpPower)holder.getPower(jumpType);
        p.setPos(c.absoluteVec(new Vec3(.5,1,.5)));
        c.setBlock(new BlockPos(0,0,0),Blocks.STONE);
        p.setOnGround(true);p.setSprinting(true);p.setDeltaMovement(0,0,0);jump.executeAction();
        c.assertTrue(p.getAirSupply()==159 && p.getDeltaMovement().z>.29,"Land sprint jump costs one moisture and pushes forward");
        p.setSprinting(false);jump.executeAction();c.assertTrue(p.getAirSupply()==159,"Ordinary jump does not spend moisture");
        var perk=(NormalPerk)RegPerks.getPerk(id("axolotl_propulsion_efficiency"));
        c.assertTrue(perk.powerRemove.contains(id("form_axolotl_3_sprinting_jump")) && !perk.powerRemove.contains(id("form_axolotl_2_water_spurt")),"Replaces land propulsion only");
        a.discard();b.discard();far.discard();friend.discard();c.succeed();
    }
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void independentSubformPerkTree(GameTestHelper c) {
        var parent = new NormalForm(id("test_parent"))
                .perkTree(id("parent_tree"));
        var child = new NormalSubForm(id("test_child"), parent);
        c.assertTrue(child.getPerkTreeID().equals(RegPerks.EMPTY_PERK_TREE), "Unconfigured subform does not inherit parent tree");
        child.perkTree(id("child_tree"));
        c.assertTrue(child.getPerkTreeID().equals(id("child_tree")) && parent.getPerkTreeID().equals(id("parent_tree")), "Subform owns its configured tree");

        var player = c.makeMockServerPlayerInLevel();
        var tree = RegPlayerForms.SNOW_FOX_3.getPerkTreeID();
        PerkUtils.__addPerk(player, tree, id("snow_fox_revenge"));
        FormUtils.setForm(player, RegPlayerForms.SNOW_FOX_3);
        var power = PowerTypeRegistry.get(id("perks/snow_fox_revenge_melee"));
        c.assertTrue(PowerHolderComponent.KEY.get(player).hasPower(power), "Master perk applies on master form");
        FormUtils.setForm(player, RegPlayerForms.SNOW_FOX_3_SUB_MARBLED_POLECAT);
        c.assertTrue(PerkUtils.getPlayerNowPerkTreeID(player).equals(id("marbled_polecat_perk_tree")), "Subform selects its own tree");
        c.assertTrue(!PowerHolderComponent.KEY.get(player).hasPower(power), "Master perk power is removed on subform transition");
        FormUtils.setForm(player, RegPlayerForms.SNOW_FOX_3);
        c.assertTrue(PowerHolderComponent.KEY.get(player).hasPower(power), "Master unlocks are preserved when returning");
        player.discard(); c.succeed();
    }
    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("shape-shifter-curse", path); }
    private static final ResourceLocation SOURCE = id("perk_check");

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void pounceMovementAndHit(GameTestHelper context) {
        var player = context.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(context.absoluteVec(new Vec3(0.5, 1, 0.5)));
        var target = context.spawn(EntityType.ZOMBIE, new BlockPos(0, 1, 5));
        var holder = PowerHolderComponent.KEY.get(player);
        var type = PowerTypeRegistry.get(id("perks/ocelot_long_pounce_flight"));
        holder.addPower(type, SOURCE);
        var pounce = (TargetPouncePower) holder.getPower(type);
        player.getFoodData().setFoodLevel(10);
        pounce.start(target);
        for (int i = 0; i < 15; i++) {
            pounce.tick();
            player.move(MoverType.SELF, player.getDeltaMovement());
        }
        context.assertTrue(target.getHealth() < 11, "Pounce reaches target and deals damage");
        context.assertTrue(player.getFoodData().getFoodLevel() == 16, "Pounce rewards food once");
        player.setPos(context.absoluteVec(new Vec3(0.5, 1, 0.5)));
        context.setBlock(new BlockPos(0, 1, 2), Blocks.STONE);
        context.setBlock(new BlockPos(0, 2, 2), Blocks.STONE);
        pounce.start(target); pounce.tick(); pounce.tick();
        context.assertTrue(player.getDeltaMovement().lengthSqr() == 0, "Wall cancels pounce movement");
        context.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void raycastPreviewAndRelease(GameTestHelper context) {
        var player = context.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(context.absoluteVec(new Vec3(0.5, 1, 0.5)));
        player.setYRot(0); player.setXRot(0);
        var near = context.spawn(EntityType.ZOMBIE, new BlockPos(0, 1, 3));
        var far = context.spawn(EntityType.ZOMBIE, new BlockPos(0, 1, 6));
        var holder = PowerHolderComponent.KEY.get(player);
        var chargeType = PowerTypeRegistry.get(id("perks/axolotl_tidal_pull_charge"));
        var glowType = PowerTypeRegistry.get(id("perks/axolotl_tidal_pull_glow"));
        holder.addPower(chargeType, SOURCE); holder.addPower(glowType, SOURCE);
        ChargePower charge = (ChargePower) holder.getPower(chargeType);
        var glow = (io.github.apace100.apoli.power.EntityGlowPower) holder.getPower(glowType);
        player.setAirSupply(100); charge.onUse();
        context.assertTrue(glow.isActive() && glow.doesApply(near), "Preview chooses nearest living target");
        context.assertTrue(!glow.doesApply(far), "Near target occludes far target");
        charge.fire(false);
        context.assertTrue(Math.abs(near.getDeltaMovement().z + 1.5) < 0.001 && Math.abs(near.getDeltaMovement().y - 0.9) < 0.001, "Apoli release uses configured pull and upward velocity");
        context.assertTrue(near.hasEffect(MobEffects.SLOW_FALLING) && near.getEffect(MobEffects.SLOW_FALLING).getDuration() == 100, "Pulled target receives five seconds of slow falling");
        context.assertTrue(!far.hasEffect(MobEffects.SLOW_FALLING) && !player.hasEffect(MobEffects.SLOW_FALLING), "Slow falling only applies to the selected target");
        context.assertTrue(far.getDeltaMovement().lengthSqr() == 0, "Release only affects first target");
        context.assertTrue(!glow.isActive(), "Release clears glow condition");
        context.setBlock(new BlockPos(0, 2, 2), Blocks.STONE);
        context.assertTrue(!glow.doesApply(near), "Walls block target selection");
        context.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void arrowUseAndPrimaryAttack(GameTestHelper context) {
        var player = context.makeMockPlayer(GameType.SURVIVAL);
        var holder = PowerHolderComponent.KEY.get(player);
        holder.addPower(PowerTypeRegistry.get(id("perks/bat_arrow_throw")), SOURCE);
        var arrows = new ItemStack(Items.ARROW, 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, arrows);
        arrows.use(context.getLevel(), player, InteractionHand.MAIN_HAND);
        context.assertTrue(arrows.getCount() == 1, "Right click fires and consumes one arrow");
        var spectral = new ItemStack(Items.SPECTRAL_ARROW, 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, spectral);
        spectral.use(context.getLevel(), player, InteractionHand.MAIN_HAND);
        context.assertTrue(spectral.getCount() == 2, "Changing arrow type cannot bypass cooldown");
        holder.addPower(PowerTypeRegistry.get(id("perks/ocelot_ambush")), SOURCE);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setShiftKeyDown(true);
        player.getFoodData().setFoodLevel(10);
        var target = EntityType.ZOMBIE.create(context.getLevel());
        player.attack(target);
        context.assertTrue(target.getHealth() < 15, "Primary attack applies ambush bonus through mixin");
        context.assertTrue(player.getFoodData().getFoodLevel() == 9, "Successful primary attack pays once");
        context.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void registrationsAndCosts(GameTestHelper context) {
        int nodes = 0, prisms = 0;
        for (String form : new String[]{"bat_3", "axolotl_3", "ocelot_3"}) {
            var tree = RegPerks.getPerkTree(id(form + "_perk_tree"));
            context.assertTrue(tree != null, "Registered tree: " + form);
            context.assertTrue(RegPlayerForms.getPlayerFormOrThrow(id(form)).getPerkTreeID().equals(tree.getID()), "Bound form: " + form);
            for (var node : tree.getAllNodes()) {
                nodes++;
                var perk = (NormalPerk) RegPerks.getPerk(node.perkID);
                context.assertTrue(perk != null, "Registered perk: " + node.perkID);
                if (perk.getCost() instanceof ItemCost item) {
                    prisms++;
                    context.assertTrue(item.getAmount() >= 1 && item.getAmount() <= 2, "Prism amount");
                }
                for (var power : perk.powerAdd) context.assertTrue(PowerTypeRegistry.get(power) != null, "Parsed power: " + power);
                for (var power : perk.powerRemove) context.assertTrue(PowerTypeRegistry.get(power) != null, "Removed power exists: " + power);
                for (var parent : node.dependentPerkIDs) context.assertTrue(tree.getNode(parent) != null, "Parent exists");
            }
        }
        context.assertTrue(nodes == 26 && prisms == 8, "26 perks and 8 prism purchases");
        context.assertTrue(RegPlayerForms.BAT_3_SUB_AVALI.getPerkTreeID().equals(id("avali_perk_tree")), "Avali has its independent tree");
        var buyer = context.makeMockPlayer(GameType.SURVIVAL);
        buyer.giveExperiencePoints(200);
        var cost = new BaseCost(
                RegCostType.COST_XP, 100);
        cost.getType().pay(cost, buyer);
        context.assertTrue(buyer.totalExperience == 100, "Experience cost deducts experience");
        context.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void combatThresholdsAndFood(GameTestHelper context) {
        var player = context.makeMockPlayer(GameType.SURVIVAL);
        var target = EntityType.ZOMBIE.create(context.getLevel());
        var holder = PowerHolderComponent.KEY.get(player);
        holder.addPower(PowerTypeRegistry.get(id("perks/axolotl_moisture_return_2")), SOURCE);
        player.setAirSupply(100);
        ActionOnCombatHitPower.fire(player, target, "critical", 7);
        context.assertTrue(player.getAirSupply() == 100, "Exactly 7 must not refund");
        ActionOnCombatHitPower.fire(player, target, "melee", 9);
        context.assertTrue(player.getAirSupply() == 100, "Non-critical hit must not refund");
        ActionOnCombatHitPower.fire(player, target, "critical", 8);
        context.assertTrue(player.getAirSupply() == 130, "Below 150 refunds 30");
        player.setAirSupply(150);
        ActionOnCombatHitPower.fire(player, target, "critical", 8);
        context.assertTrue(player.getAirSupply() == 165, "At 150 refunds 15");
        holder.addPower(PowerTypeRegistry.get(id("perks/ocelot_ambush")), SOURCE);
        player.getFoodData().setFoodLevel(10);
        player.setShiftKeyDown(true);
        context.assertTrue(ActionOnCombatHitPower.meleeBonus(player, target) == 6, "Ambush adds six");
        ActionOnCombatHitPower.fire(player, target, "melee", 6);
        context.assertTrue(player.getFoodData().getFoodLevel() == 9, "Ambush charges one food, not exhaustion");
        player.setShiftKeyDown(false);
        context.assertTrue(ActionOnCombatHitPower.meleeBonus(player, target) == 0, "Standing must not ambush");
        holder.addPower(PowerTypeRegistry.get(id("perks/ocelot_hungry_pounce")), SOURCE);
        ActionOnCombatHitPower.fire(player, target, "pounce", 0);
        context.assertTrue(player.getFoodData().getFoodLevel() == 9, "No damage must not refund");
        ActionOnCombatHitPower.fire(player, target, "pounce", 7);
        context.assertTrue(player.getFoodData().getFoodLevel() == 11, "Pounce refunds two");
        context.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void overloadAndCharging(GameTestHelper context) {
        var player = context.makeMockPlayer(GameType.SURVIVAL);
        var holder = PowerHolderComponent.KEY.get(player);
        var overload = PowerTypeRegistry.get(id("perks/ocelot_metabolic_overload"));
        holder.addPower(overload, SOURCE);
        holder.getPower(overload).fromTag(LongTag.valueOf(-1000), context.getLevel().registryAccess());
        player.getFoodData().setFoodLevel(12);
        ((Active) holder.getPower(overload)).onUse();
        context.assertTrue(player.getFoodData().getFoodLevel() == 4, "Overload exact food cost");
        context.assertTrue(player.hasEffect(MobEffects.ABSORPTION) && player.getEffect(MobEffects.ABSORPTION).getAmplifier() == 1, "Absorption II");
        ((Active) holder.getPower(overload)).onUse();
        context.assertTrue(player.getFoodData().getFoodLevel() == 4, "Cooldown prevents duplicate cost");
        var chargeType = PowerTypeRegistry.get(id("perks/axolotl_tidal_pull_charge"));
        holder.addPower(chargeType, SOURCE);
        ChargePower charge = (ChargePower) holder.getPower(chargeType);
        player.setAirSupply(100);
        charge.onUse(); charge.onUse();
        context.assertTrue(player.getAirSupply() == 80, "Holding pays once");
        context.assertTrue(charge.isCharging(), "Charging state for preview");
        charge.fire(false);
        context.assertTrue(!charge.isCharging(), "Release clears preview");
        charge.onUse();
        context.assertTrue(player.getAirSupply() == 80, "Cooldown prevents recharging");
        context.succeed();
    }
}
