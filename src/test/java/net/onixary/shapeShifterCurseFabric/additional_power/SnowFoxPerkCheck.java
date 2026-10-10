package net.onixary.shapeShifterCurseFabric.additional_power;

import com.google.gson.JsonObject;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.*;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.perk.RegPerks;
import net.onixary.shapeShifterCurseFabric.status_effects.RegOtherStatusEffects;

public class SnowFoxPerkCheck {
    /** 自有 effect 是裸 MobEffect，而 1.21 的 hasEffect/removeEffect/addEffect 全收 Holder */
    private static final Holder<MobEffect> FROST_CLAW = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(RegOtherStatusEffects.FROST_CLAW);
    private static Holder<Enchantment> frostWalker(GameTestHelper c) {
        return c.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.FROST_WALKER);
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void configurableFallingAttackCap(GameTestHelper c) {
        var p = player(c, 3);
        var factory = EnhancedFallingAttackPower.createFactory();
        var registries = c.getLevel().registryAccess();
        // 1.21 起 PowerFactory.read 要带 HolderLookup.Provider
        var defaults = (EnhancedFallingAttackPower) factory.read(new JsonObject(), registries).apply(null, p);
        var json = new JsonObject(); json.addProperty("max_multiplier", 4.0f);
        var configured = (EnhancedFallingAttackPower) factory.read(json, registries).apply(null, p);
        c.assertTrue(defaults.getFallMultiplier(20) == 2.0f, "Omitted cap keeps old behavior");
        c.assertTrue(configured.getFallMultiplier(1) == 1.0f, "No bonus below one block");
        c.assertTrue(configured.getFallMultiplier(1.5f) == 2.5f, "Interpolate toward configured cap");
        c.assertTrue(configured.getFallMultiplier(2) == 4.0f && configured.getFallMultiplier(20) == 4.0f, "Cap at two blocks and beyond");
        json.addProperty("max_multiplier", 0.5f);
        var lower = (EnhancedFallingAttackPower) factory.read(json, registries).apply(null, p);
        c.assertTrue(lower.getFallMultiplier(20) == 1.0f, "Cap cannot invert the bonus");
        p.discard(); c.succeed();
    }
    private static ResourceLocation id(String s) { return ResourceLocation.fromNamespaceAndPath("shape-shifter-curse", s); }
    private static ServerPlayer player(GameTestHelper c, double height) {
        var p = c.makeMockServerPlayerInLevel();
        for (int i = 0; i < 61; i++) p.tick();
        p.getAbilities().invulnerable = false; p.getAbilities().flying = false; p.setInvulnerable(false);
        p.setPos(c.absoluteVec(new Vec3(2.5, height, 2.5))); p.setOnGround(false);
        p.getFoodData().setFoodLevel(20); return p;
    }
    private static Power add(ServerPlayer p, String suffix) {
        var type = PowerTypeRegistry.get(id("perks/snow_fox_" + suffix));
        PowerHolderComponent.KEY.get(p).addPower(type, id("snow_test"));
        return PowerHolderComponent.KEY.get(p).getPower(type);
    }
    private static FrostDivePower dive(ServerPlayer p) {
        var power = (FrostDivePower) add(p, "frost_dive");
        var tag = new CompoundTag(); tag.put("cooldown", LongTag.valueOf(-10000));
        power.fromTag(tag, p.level().registryAccess()); return power;
    }
    private static void floor(GameTestHelper c, boolean water) {
        for (int x = 0; x < 6; x++) for (int z = 0; z < 6; z++) {
            c.setBlock(new BlockPos(x, 0, z), water ? Blocks.WATER : Blocks.STONE);
            for (int y = 1; y < 10; y++) c.setBlock(new BlockPos(x, y, z), Blocks.AIR);
        }
    }
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void registrations(GameTestHelper c) {
        for (String key : new String[]{"revenge", "elusive_paws", "air_jump", "frost_dive", "cold_whirlwind_1", "cold_whirlwind_2", "cold_recovery_1", "cold_recovery_2"}) {
            c.assertTrue(PowerTypeRegistry.contains(id("perks/snow_fox_" + key)), "Power loaded: " + key);
            c.assertTrue(RegPerks.getPerk(id("snow_fox_" + key)) != null, "Perk registered: " + key);
        }
        c.succeed();
    }
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void frostClawIndependentFastFall(GameTestHelper c) {
        floor(c, true); var p = player(c, 6);
        p.addEffect(new MobEffectInstance(FROST_CLAW, 200));
        c.assertTrue(EnchantmentHelper.getEnchantmentLevel(frostWalker(c), p) == 1, "Virtual Frost Walker I");
        p.move(MoverType.SELF, new Vec3(0, -6, 0));
        c.assertTrue(c.getBlockState(new BlockPos(2, 0, 2)).is(Blocks.FROSTED_ICE), "Independent buff freezes before fast collision");
        c.assertTrue(p.onGround() && Math.abs(p.getY() - c.absolutePos(new BlockPos(0, 1, 0)).getY()) < 0.01, "Lands on ice, not inside water");
        p.removeEffect(FROST_CLAW);
        // Mojmap 没有 EnchantmentHelper.hasFrostWalker，等价断言 = 附魔等级回落为 0
        c.assertTrue(EnchantmentHelper.getEnchantmentLevel(frostWalker(c), p) == 0, "Virtual enchantment removed with effect");
        p.discard(); c.succeed();
    }
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void diveImpactAndLock(GameTestHelper c) {
        floor(c, true); var p = player(c, 8); var d = dive(p);
        var zombie = c.spawn(EntityType.ZOMBIE, new BlockPos(4, 1, 2)); zombie.setNoAi(true);
        zombie.getAttribute(Attributes.ARMOR).setBaseValue(0);
        // Yarn Entity.setOnFireFor(秒) → Mojmap setRemainingFireTicks(刻)
        p.getFoodData().setFoodLevel(2); p.setRemainingFireTicks(200); d.onUse();
        c.assertTrue(FrostDivePower.isDiving(p) && p.getFoodData().getFoodLevel() == 0 && !p.isOnFire(), "Start costs exactly 2 food and extinguishes");
        var up = (ActiveCooldownPower) add(p, "cold_whirlwind_2"); up.fromTag(LongTag.valueOf(-10000), c.getLevel().registryAccess());
        p.getFoodData().setFoodLevel(20); up.onUse();
        c.assertTrue(p.getFoodData().getFoodLevel() == 20 && p.getDeltaMovement().y < 0, "Whirlwind cannot interrupt or charge food");
        double x = p.getX(), y = p.getY(); p.move(MoverType.SELF, new Vec3(2, 2, 0));
        c.assertTrue(p.getX() == x && p.getY() == y, "Movement lock blocks upward and horizontal impulses");
        // Yarn LivingEntity.handleFallDamage → Mojmap causeFallDamage
        p.causeFallDamage(30, 1, p.damageSources().fall()); c.assertTrue(p.getHealth() == 20, "Dive fall immunity");
        for (int i = 0; i < 15 && FrostDivePower.isDiving(p); i++) { d.tick(); p.move(MoverType.SELF, p.getDeltaMovement()); }
        c.assertTrue(!FrostDivePower.isDiving(p), "Dive ends on actual ground");
        c.assertTrue(zombie.getHealth() == 14, "One six-point impact: " + zombie.getHealth());
        c.assertTrue(zombie.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getAmplifier() == 2, "Slowness III");
        zombie.invulnerableTime = 0; d.tick(); c.assertTrue(zombie.getHealth() == 14, "No repeated impact");
        p.setOnGround(false); d.onUse(); c.assertTrue(!FrostDivePower.isDiving(p), "Eight-second cooldown enforced");
        p.discard(); zombie.discard(); c.succeed();
    }
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void shortDiveAndAirJump(GameTestHelper c) {
        floor(c, false); var p = player(c, 5); var air = (AirJumpPower) add(p, "air_jump");
        air.tick(); air.tick(); air.onUse(); c.assertTrue(Math.abs(p.getDeltaMovement().y - 0.6) < 0.001, "Fixed air jump impulse");
        p.setDeltaMovement(Vec3.ZERO); air.onUse(); c.assertTrue(p.getDeltaMovement().y == 0, "Only one jump per flight");
        p.setOnGround(true); air.tick(); p.setOnGround(false); air.tick(); air.tick(); air.onUse();
        c.assertTrue(p.getDeltaMovement().y > 0, "Landing replenishes air jump");
        var zombie = c.spawn(EntityType.ZOMBIE, new BlockPos(4, 1, 2)); var d = dive(p); d.onUse();
        for (int i = 0; i < 8 && FrostDivePower.isDiving(p); i++) { d.tick(); p.move(MoverType.SELF, p.getDeltaMovement()); }
        c.assertTrue(zombie.getHealth() == 20 && !zombie.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "Exactly four blocks has no shockwave");
        p.discard(); zombie.discard(); c.succeed();
    }
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void revengeAndRecovery(GameTestHelper c) {
        var p = player(c, 1); add(p, "revenge_melee"); add(p, "revenge_projectile");
        var witch = c.spawn(EntityType.WITCH, new BlockPos(4, 1, 2));
        float before = witch.getHealth(); witch.hurt(p.damageSources().playerAttack(p), 10);
        c.assertTrue(Math.abs(before - witch.getHealth() - 13) < 0.01, "Witch attack bonus");
        var arrow = new Arrow(c.getLevel(), p, new ItemStack(Items.ARROW), null); witch.invulnerableTime = 0;
        before = witch.getHealth(); witch.hurt(p.damageSources().arrow(arrow, p), 2);
        c.assertTrue(Math.abs(before - witch.getHealth() - 2.6) < 0.01, "Projectile bonus");
        add(p, "cold_recovery_1_immunity"); p.hurt(p.damageSources().freeze(), 3);
        c.assertTrue(p.getHealth() == 20, "Freeze immunity");
        var heal = add(p, "cold_recovery_1_healing"); p.setHealth(10); c.setBlock(new BlockPos(2, 1, 2), Blocks.POWDER_SNOW);
        heal.tick();
        // ⚠ Yarn 的 `Entity.age` 是 tick 计数器 → Mojmap `Entity.tickCount`
        for (int i = 0; i < 60; i++) { p.tickCount++; heal.tick(); }
        c.assertTrue(p.getHealth() == 12, "Powder snow heals two per three seconds: " + p.getHealth());
        p.discard(); witch.discard(); c.succeed();
    }
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void whirlwindAndFrostRules(GameTestHelper c) {
        floor(c, true); var p = player(c, 4);
        var whirlwind = (ActiveCooldownPower) add(p, "cold_whirlwind_2"); whirlwind.fromTag(LongTag.valueOf(-10000), c.getLevel().registryAccess());
        p.setDeltaMovement(0, -0.5, 0); p.setRemainingFireTicks(80); whirlwind.onUse();
        c.assertTrue(p.getFoodData().getFoodLevel() == 17 && !p.isOnFire(), "Whirlwind payment and extinguish");
        c.assertTrue(Math.abs(p.getDeltaMovement().y - 1.1) < 0.001 && p.getEffect(MobEffects.ABSORPTION).getAmplifier() == 1, "Stronger lift and absorption II");
        whirlwind.onUse(); c.assertTrue(p.getFoodData().getFoodLevel() == 17 && whirlwind.getRemainingTicks() == 100, "Five-second cooldown");
        p.setPos(c.absoluteVec(new Vec3(2.5, 1.6, 2.5)));
        c.setBlock(new BlockPos(1, 0, 2), Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 1));
        c.setBlock(new BlockPos(3, 1, 2), Blocks.STONE);
        // Yarn MobEffect.applyUpdateEffect → Mojmap applyEffectTick（1.21 起返回 boolean，失败会移除效果）
        FROST_CLAW.value().applyEffectTick(p, 0);
        c.assertTrue(c.getBlockState(new BlockPos(1, 0, 2)).is(Blocks.WATER), "Flowing water remains water");
        c.assertTrue(c.getBlockState(new BlockPos(3, 0, 2)).is(Blocks.WATER), "Covered source water remains water");
        c.assertTrue(c.getBlockState(new BlockPos(2, 0, 2)).is(Blocks.FROSTED_ICE), "Airborne source water freezes");
        p.discard(); c.succeed();
    }
}
