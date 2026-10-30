package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ConditionedAttributePower;
import io.github.apace100.apoli.power.MultiplePowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.onixary.shapeShifterCurseFabric.perk.RegPerks;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.OriginRegistry;
import net.onixary.shapeShifterCurseFabric.perk.PerkUtils;
import net.onixary.shapeShifterCurseFabric.player_form.utils.PlayerFormComponent;
import java.util.ArrayList;
import java.util.List;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.minecraft.world.phys.Vec3;

public class AxolotlVegetationCheck {
    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath("shape-shifter-curse", path); }
    private static final TagKey<Block> VEGETATION = TagKey.create(Registries.BLOCK, id("perk_vegetation"));

    @GameTest
    public void vegetationTagLoads(GameTestHelper context) {
        context.assertTrue(Blocks.GRASS_BLOCK.defaultBlockState().is(VEGETATION), "grass block in tag");
        context.assertTrue(Blocks.MOSS_BLOCK.defaultBlockState().is(VEGETATION), "moss block in tag");
        context.assertFalse(Blocks.STONE.defaultBlockState().is(VEGETATION), "stone not in tag");
        var tag = context.getLevel().getServer().registryAccess().lookupOrThrow(Registries.BLOCK).get(VEGETATION);
        System.out.println("DEBUG tag present=" + tag.isPresent() + " size=" + tag.map(t -> t.size()).orElse(-1));
        context.succeed();
    }

    @GameTest
    public void vegetationGaitApplies(GameTestHelper context) {
        context.setBlock(new BlockPos(0, 1, 0), Blocks.GRASS_BLOCK);
        var stand = context.absolutePos(new BlockPos(0, 2, 0));
        var player = context.makeMockServerPlayerInLevel();
        player.snapTo(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5, 0.0F, 0.0F);
        player.setOnGround(true);
        var type = PowerTypeRegistry.get(id("perks/axolotl_vegetation"));
        context.assertTrue(type instanceof MultiplePowerType, "multiple power parsed");
        context.assertTrue(((MultiplePowerType<?>) type).getSubPowers().size() == 2, "two sub powers");
        var holder = PowerHolderComponent.KEY.get(player);
        holder.addPower(type, id("axolotl_vegetation_test"));
        var powers = holder.getPowers(ConditionedAttributePower.class, true);
        context.assertTrue(powers.size() == 2, "two conditioned attributes: " + powers.size());
        ConditionedAttributePower normal = null;
        ConditionedAttributePower vegetation = null;
        for (var power : powers) {
            String path = power.getType().getIdentifier().getPath();
            if (path.endsWith("_normal")) {
                normal = power;
            } else {
                vegetation = power;
            }
            System.out.println("DEBUG " + path + " active=" + power.isActive() + " pos=" + player.position() + " onGround=" + player.onGround());
        }
        context.assertTrue(normal != null && vegetation != null, "both sub powers granted");
        context.assertTrue(vegetation.isActive(), "vegetation sub power active on grass");
        context.assertFalse(normal.isActive(), "normal sub power inactive on grass");
        vegetation.addMods();
        double grass = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
        System.out.println("DEBUG grassSpeed=" + grass);
        context.assertTrue(Math.abs(grass - 0.085) < 1.0E-6, "vegetation speed 0.085, got " + grass);
        vegetation.removeMods();
        context.setBlock(new BlockPos(0, 1, 0), Blocks.STONE);
        player.setOnGround(true);
        context.assertTrue(normal.isActive(), "normal sub power active on stone");
        context.assertFalse(vegetation.isActive(), "vegetation sub power inactive on stone");
        normal.addMods();
        double stone = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
        System.out.println("DEBUG stoneSpeed=" + stone);
        context.assertTrue(Math.abs(stone - 0.07) < 1.0E-6, "barren speed 0.07, got " + stone);
        normal.removeMods();
        player.discard();
        context.succeed();
    }
    @GameTest
    public void perkReplacesGroundPenalty(GameTestHelper context) {
        context.setBlock(new BlockPos(0, 1, 0), Blocks.GRASS_BLOCK);
        var stand = context.absolutePos(new BlockPos(0, 2, 0));
        var player = context.makeMockServerPlayerInLevel();
        player.snapTo(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5, 0.0F, 0.0F);
        player.setOnGround(true);
        var form = RegPlayerForms.AXOLOTL_3;
        var source = form.getFormLayer().getB();
        var base = PowerTypeRegistry.get(id("form_axolotl_3_ground_speed_down"));
        var holder = PowerHolderComponent.KEY.get(player);
        holder.addPower(base, source);
        context.assertTrue(holder.hasPower(base, source), "base penalty granted with origin source");
        RegPerks.getPerk(id("axolotl_vegetation")).onLoad(player, form);
        context.assertFalse(holder.hasPower(base, source), "perk removed base penalty, source=" + source + " powers=" + holder.getPowersFromSource(source));
        var caps = holder.getPowers(ConditionedAttributePower.class, true);
        context.assertTrue(caps.size() == 2, "two conditioned attributes: " + caps.size());
        for (var power : caps) {
            System.out.println("DEBUG perk " + power.getType().getIdentifier() + " active=" + power.isActive());
            if (power.isActive()) {
                power.addMods();
            }
        }
        double speed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
        System.out.println("DEBUG perk grassSpeed=" + speed);
        context.assertTrue(Math.abs(speed - 0.085) < 1.0E-6, "grass speed with perk 0.085, got " + speed);
        player.discard();
        context.succeed();
    }
    @GameTest
    public void realUnlockPathAppliesPerk(GameTestHelper context) {
        context.setBlock(new BlockPos(0, 1, 0), Blocks.GRASS_BLOCK);
        var stand = context.absolutePos(new BlockPos(0, 2, 0));
        var player = context.makeMockServerPlayerInLevel();
        player.snapTo(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5, 0.0F, 0.0F);
        player.setOnGround(true);
        var origin = OriginRegistry.get(id("form_axolotl_3"));
        context.assertTrue(origin != null, "form_axolotl_3 origin loaded");
        var holder = PowerHolderComponent.KEY.get(player);
        var source = origin.getIdentifier();
        int granted = 0;
        for (var powerType : origin.getPowerTypes()) {
            holder.addPower(powerType, source);
            granted++;
        }
        var base = PowerTypeRegistry.get(id("form_axolotl_3_ground_speed_down"));
        context.assertTrue(holder.hasPower(base, source), "base penalty granted from origin, granted=" + granted);
        var component = PlayerFormComponent.COMPONENT.get(player);
        component.nowForm = RegPlayerForms.AXOLOTL_3;
        component.nowFormID = RegPlayerForms.AXOLOTL_3.getFormID();
        PerkUtils.addPerk(player, RegPerks.T_Axolotl3PerkTree, id("axolotl_vegetation"));
        var recorded = PerkUtils.getPlayerPerks(player, RegPerks.T_Axolotl3PerkTree);
        context.assertTrue(recorded != null && recorded.contains(id("axolotl_vegetation")), "perk recorded by real unlock path: " + recorded);
        context.assertFalse(holder.hasPower(base, source), "real unlock removed base penalty");
        double grass = measureSpeedWithStack(holder, player);
        System.out.println("DEBUG real grass stackSpeed=" + grass);
        context.setBlock(new BlockPos(0, 1, 0), Blocks.STONE);
        player.setOnGround(true);
        double stone = measureSpeedWithStack(holder, player);
        System.out.println("DEBUG real stone stackSpeed=" + stone);
        context.assertTrue(stone < grass, "vegetation stack faster than barren: grass=" + grass + " stone=" + stone);
        player.discard();
        context.succeed();
    }

    @GameTest
    public void perkInactiveWhileSprinting(GameTestHelper context) {
        context.setBlock(new BlockPos(0, 1, 0), Blocks.GRASS_BLOCK);
        var stand = context.absolutePos(new BlockPos(0, 2, 0));
        var player = context.makeMockServerPlayerInLevel();
        player.snapTo(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5, 0.0F, 0.0F);
        player.setOnGround(true);
        var holder = PowerHolderComponent.KEY.get(player);
        holder.addPower(PowerTypeRegistry.get(id("perks/axolotl_vegetation")), id("axolotl_vegetation_sprint_test"));
        var powers = holder.getPowers(ConditionedAttributePower.class, true);
        int activeWalking = 0;
        for (var power : powers) {
            if (power.isActive()) activeWalking++;
        }
        System.out.println("DEBUG sprint walkActive=" + activeWalking);
        context.assertTrue(activeWalking == 1, "one sub power active while walking on grass: " + activeWalking);
        player.setSprinting(true);
        int activeSprinting = 0;
        for (var power : powers) {
            if (power.isActive()) activeSprinting++;
        }
        System.out.println("DEBUG sprint sprintActive=" + activeSprinting + " isSprinting=" + player.isSprinting());
        context.assertTrue(activeSprinting == 0, "both sub powers inactive while sprinting: " + activeSprinting);
        player.setSprinting(false);
        player.setOnGround(true);
        int activeBack = 0;
        for (var power : powers) {
            if (power.isActive()) activeBack++;
        }
        System.out.println("DEBUG sprint restored=" + activeBack);
        context.assertTrue(activeBack == 1, "power restored after sprint stops: " + activeBack);
        player.discard();
        context.succeed();
    }
    private static double measureSpeedWithStack(PowerHolderComponent holder, net.minecraft.world.entity.player.Player player) {
        List<ConditionedAttributePower> applied = new ArrayList<>();
        for (var power : holder.getPowers(ConditionedAttributePower.class, true)) {
            if (power.isActive()) {
                power.addMods();
                applied.add(power);
            }
        }
        double value = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
        for (var power : applied) {
            power.removeMods();
        }
        return value;
    }
}
