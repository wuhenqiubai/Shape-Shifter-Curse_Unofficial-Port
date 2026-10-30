package net.onixary.shapeShifterCurseFabric.additional_power;

import com.google.gson.JsonParser;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.blocks.RegCustomBlock;
import net.onixary.shapeShifterCurseFabric.entity.RegCustomEntity;
import net.onixary.shapeShifterCurseFabric.entity.projectile.WebBullet;
import net.onixary.shapeShifterCurseFabric.items.RegCustomItem;
import net.onixary.shapeShifterCurseFabric.mana.ManaUtils;
import net.onixary.shapeShifterCurseFabric.minion.IPlayerEntityMinion;
import net.onixary.shapeShifterCurseFabric.minion.mobs.AnubisWolfMinionEntity;
import net.onixary.shapeShifterCurseFabric.perk.NormalPerk;
import net.onixary.shapeShifterCurseFabric.perk.RegPerks;


public class WolfSpiderPerkCheck {
    private static Identifier id(String name) { return Identifier.fromNamespaceAndPath("shape-shifter-curse", name); }

    private static ServerPlayer player(GameTestHelper c) {
        ServerPlayer p = TestPlayers.spawn(c);
        p.setGameMode(GameType.CREATIVE);
        for (int i = 0; i < 61; i++) p.tick();
        p.getAbilities().invulnerable = false; p.getAbilities().flying = false; p.setInvulnerable(false);
        p.setPos(c.absoluteVec(new Vec3(2.5, 2, 2.5)));
        return p;
    }
    private static Power add(ServerPlayer p, String name) {
        var type = PowerTypeRegistry.get(id("perks/"+name));
        PowerHolderComponent.KEY.get(p).addPower(type, id("wolf_spider_test"));
        return PowerHolderComponent.KEY.get(p).getPower(type);
    }
    private static void action(ServerPlayer player, String json) {
        ApoliDataTypes.ENTITY_ACTION.read(JsonParser.parseString(json), player.level().registryAccess()).accept(player);
    }
    private static void webMana(ServerPlayer p) {
        ManaUtils.gainManaTypeID(p, id("web_resource"), id("wolf_spider_test"));
        ManaUtils.setPlayerMana(p, 50);
    }
    private static void clearSpace(GameTestHelper c) {
        for (int x=0;x<8;x++) for (int z=0;z<8;z++) for (int y=0;y<9;y++)
            c.setBlock(new BlockPos(x,y,z), y == 0 ? Blocks.STONE : Blocks.AIR);
    }

    @GameTest
    public void registeredTreesAndPowers(GameTestHelper c) {
        String[] names = {"anubis_wolf_pack_response_1", "anubis_wolf_soul_excitation", "anubis_wolf_pack_response_2", "anubis_wolf_pack_amplification", "anubis_wolf_wither_tolerance_1", "anubis_wolf_wither_tolerance_2", "anubis_wolf_soul_recall", "anubis_wolf_undead_discernment", "spider_four_legged_adaptation", "spider_cocoon_digestion_1", "spider_cocoon_digestion_2", "spider_bridge_weaver", "spider_perceptual_web", "spider_rapid_spinner", "spider_stabilized_projectile", "spider_silk_secretion_1", "spider_silk_secretion_2", "spider_silk_grapple"};
        for (String name : names) {
            var perk = (NormalPerk) RegPerks.getPerk(id(name));
            c.assertTrue(perk != null, "Registered perk: " + name);
            for (var power : perk.powerAdd) c.assertTrue(PowerTypeRegistry.contains(power), "Loaded power: " + power);
        }
        c.succeed();
    }

    @GameTest
    public void wolfSummonsAndRecall(GameTestHelper c) {
        var p=player(c); var owner=(IPlayerEntityMinion)p;
        add(p,"anubis_wolf_pack_response_1"); add(p,"anubis_wolf_pack_amplification");
        action(p,"{\"type\":\"shape-shifter-curse:summon_anubis_wolf_minion\",\"count\":2,\"max_minion_count\":2,\"cooldown\":420,\"minion_level\":3}");
        c.assertTrue(owner.shape_shifter_curse$getMinionsCount(AnubisWolfMinionEntity.MinionID)==2,"Normal cap");
        long normalCooldown=owner.shape_shifter_curse$getCooldownTime(AnubisWolfMinionEntity.MinionID);
        var extra=(ActiveCooldownPower)add(p,"anubis_wolf_soul_excitation"); extra.fromTag(LongTag.valueOf(-10000)); extra.onUse();
        c.assertTrue(owner.shape_shifter_curse$getMinionsCount(AnubisWolfMinionEntity.MinionID)==3,"Extra summon bypasses cap but occupies slot");
        c.assertTrue(normalCooldown==owner.shape_shifter_curse$getCooldownTime(AnubisWolfMinionEntity.MinionID),"Extra summon preserves normal cooldown");
        c.assertTrue(p.getEffect(MobEffects.WITHER).getDuration()==200,"Self wither lasts 10 seconds");
        for (var uuid : owner.shape_shifter_curse$getMinionsByMinionID(AnubisWolfMinionEntity.MinionID)) {
            var wolf=(AnubisWolfMinionEntity)c.getLevel().getEntity(uuid);
            c.assertTrue(wolf.getEffect(MobEffects.STRENGTH).getDuration()==600,"Both summon paths gain Strength I");
        }
        p.setHealth(10);
        var recall=(ActiveCooldownPower)add(p,"anubis_wolf_soul_recall");recall.fromTag(LongTag.valueOf(-10000));recall.onUse();
        c.assertTrue(!p.hasEffect(MobEffects.WITHER) && p.getHealth()==16,"Recall clears wither and heals 2 per death: "+p.getHealth());
        c.assertTrue(owner.shape_shifter_curse$getMinionsCount(AnubisWolfMinionEntity.MinionID)==0,"Recall frees all occupied slots");
        p.tickCount+=421;
        action(p,"{\"type\":\"shape-shifter-curse:summon_anubis_wolf_minion\",\"count\":3,\"max_minion_count\":2,\"cooldown\":420}");
        c.assertTrue(owner.shape_shifter_curse$getCooldownTime(AnubisWolfMinionEntity.MinionID)==p.tickCount,"Partial summon still starts cooldown");
        action(p,"{\"type\":\"shape-shifter-curse:recall_wolf_minions\"}");p.discard();c.succeed();
    }

    @GameTest
    public void witherIntervals(GameTestHelper c) {
        var p=player(c);add(p,"anubis_wolf_wither_tolerance_1");
        var effect=new MobEffectInstance(MobEffects.WITHER,120,0);
        var tickResult = effect.tickServer(c.getLevel(), p, ()->{});
        c.assertTrue(p.getHealth()==19,"Tier I damage every 60 ticks");
        p.invulnerableTime=0; p.setHealth(20);
        new MobEffectInstance(MobEffects.WITHER,80,0).tickServer(c.getLevel(), p, ()->{});
        c.assertTrue(p.getHealth()==20,"Original 40-tick cadence suppressed");
        add(p,"anubis_wolf_wither_tolerance_2");
        new MobEffectInstance(MobEffects.WITHER,80,0).tickServer(c.getLevel(), p, ()->{});
        c.assertTrue(p.getHealth()==19,"Tier II uses 80 ticks and does not stack with I");
        p.invulnerableTime=0;
        new MobEffectInstance(MobEffects.WITHER,40,1).tickServer(c.getLevel(), p, ()->{});
        c.assertTrue(p.getHealth()==18,"Wither II uses 40 ticks");
        var zombie=c.spawn(EntityType.ZOMBIE,new BlockPos(4,1,2));
        new MobEffectInstance(MobEffects.WITHER,40,0).tickServer(c.getLevel(), zombie, ()->{});
        c.assertTrue(zombie.getHealth()<20,"Other entities keep vanilla cadence");
        p.discard();zombie.discard();c.succeed();
    }

    @GameTest
    public void craftingAndThrowable(GameTestHelper c) {
        var p=player(c);webMana(p);var craft=(ItemOnItemPower)add(p,"spider_stabilized_projectile");
        var using=new ItemStack(Items.COBWEB,1);p.getInventory().setItem(0,new ItemStack(Items.COBWEB,1));
        var slot=new Slot(p.getInventory(),0,0,0);
        c.assertTrue(craft.isActive() && craft.doesApply(using,slot.getItem()),"Cobweb combination enabled");
        craft.execute(using,slot.getItem(),slot);
        c.assertTrue(using.isEmpty() && slot.getItem().is(RegCustomItem.WEB_PROJECTILE) && slot.getItem().getCount()==1,"Exactly two cobwebs produce one projectile");
        c.assertTrue(ManaUtils.getPlayerMana(p)==40,"Craft costs 10 web resource");
        ManaUtils.setPlayerMana(p,9);c.assertTrue(!craft.isActive(),"Insufficient mana disables crafting");
        PowerHolderComponent.KEY.get(p).removePower(craft.getType(),id("wolf_spider_test"));
        p.setGameMode(GameType.SURVIVAL); p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(RegCustomItem.WEB_PROJECTILE));
        p.getMainHandItem().use(c.getLevel(),p,InteractionHand.MAIN_HAND);
        var bullet=c.getLevel().getEntities(RegCustomEntity.WEB_BULLET,p.getBoundingBox().inflate(4),e->e.getOwner()==p).get(0);
        c.assertTrue(bullet.Tier==3 && !bullet.EnableVenomSpindle && p.getMainHandItem().isEmpty(),"Non-perk player can fire and consumes the item");
        var out=net.minecraft.world.level.storage.TagValueOutput.createWithoutContext(net.minecraft.util.ProblemReporter.DISCARDING);bullet.addAdditionalSaveData(out);
        var restored=new WebBullet(RegCustomEntity.WEB_BULLET,c.getLevel());restored.readAdditionalSaveData(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, c.getLevel().registryAccess(), out.buildResult()));
        c.assertTrue(restored.Tier==3 && !restored.EnableVenomSpindle,"Projectile configuration survives reload");
        var target=c.spawn(EntityType.ZOMBIE,new BlockPos(4,1,2));
        restored.onHitEntity(new EntityHitResult(target));
        c.assertTrue(target.getEffect(MobEffects.SLOWNESS).getAmplifier()==3 && target.getEffect(MobEffects.WEAKNESS).getDuration()==160,"Fully charged projectile effects survive reload");
        bullet.discard();target.discard();p.discard();c.succeed();
    }

    @GameTest
    public void spiderPassiveAndChargeValues(GameTestHelper c) {
        clearSpace(c);var p=player(c);webMana(p);
        c.assertTrue(CocoonLootChancePower.getChance(p)==40,"Base cocoon chance");
        add(p,"spider_cocoon_digestion_1");c.assertTrue(CocoonLootChancePower.getChance(p)==55,"First upgrade chance");
        add(p,"spider_cocoon_digestion_2");c.assertTrue(CocoonLootChancePower.getChance(p)==65,"Second upgrade replaces chance");
        for (String name : new String[]{"spider_bridge_weaver","spider_rapid_spinner"}) {
            var charge=(ChargePower)add(p,name);
            for(int i=1;i<=3;i++) c.assertTrue(charge.ChargeTierList.get(i).chargeTime==14*i,"Charge thresholds");
            ManaUtils.setPlayerMana(p,50);
            for(int i=0;i<42;i++){charge.onUse();charge.tick();}
            c.assertTrue(charge.nowTier==3 && Math.abs(ManaUtils.getPlayerMana(p)-35)<1,"Full charge still costs approximately 15");
            charge.onRemoved();
        }
        var glow=(EntityGlowPower)add(p,"spider_perceptual_web");
        c.assertTrue(!glow.isActive(),"No outline off web bridge");
        c.setBlock(new BlockPos(2,2,2),RegCustomBlock.TEMP_WEB_BRIDGE);
        c.assertTrue(glow.isActive(),"Standing inside bridge enables outline");
        var sheep=c.spawn(EntityType.SHEEP,new BlockPos(4,2,2));
        c.assertTrue(glow.doesApply(sheep) && !glow.usesTeams() && glow.getRed()==1,"Passive animals receive white outline too");
        sheep.setPos(p.position().add(17,0,0));c.assertTrue(!glow.doesApply(sheep),"Outline range is 16 blocks");
        sheep.discard();p.discard();c.succeed();
    }

    @GameTest
    public void grappleRefundAndFlight(GameTestHelper c) {
        clearSpace(c);var p=player(c);webMana(p);p.setYRot(0);p.setXRot(0);
        var grapple=(SurfaceGrapplePower)add(p,"spider_silk_grapple");
        p.setXRot(-90);grapple.onUse();grapple.onUse();
        c.assertTrue(ManaUtils.getPlayerMana(p)==40,"Hold reserves cost exactly once");
        grapple.release();c.assertTrue(ManaUtils.getPlayerMana(p)==50,"No surface refunds full reservation");
        p.setXRot(0);
        for(int x=1;x<=4;x++)for(int y=1;y<=5;y++)c.setBlock(new BlockPos(x,y,6),Blocks.STONE);
        var target=grapple.findDestination();
        c.assertTrue(target!=null && Math.abs(target.z-c.absoluteVec(new Vec3(0,0,5)).z)<0.01,"Destination is one block outside hit face");
        grapple.onUse();grapple.release();
        for(int i=0;i<12;i++){grapple.tick();p.move(MoverType.SELF,p.getDeltaMovement());}
        c.assertTrue(p.position().distanceTo(target)<0.21 && !p.isNoGravity(),"Flight reaches target and restores gravity");
        grapple.onUse();c.assertTrue(ManaUtils.getPlayerMana(p)==40,"Successful cast starts cooldown");
        p.discard();c.succeed();
    }

    @GameTest
    public void grappleCancellationAndObstacle(GameTestHelper c) {
        clearSpace(c);var p=player(c);webMana(p);p.setYRot(0);p.setXRot(0);
        var grapple=(SurfaceGrapplePower)add(p,"spider_silk_grapple");
        grapple.onUse();grapple.onRemoved();
        c.assertTrue(ManaUtils.getPlayerMana(p)==50,"Removing held power refunds reservation");
        for(int y=1;y<6;y++)c.setBlock(new BlockPos(2,y,6),Blocks.STONE);
        grapple.onUse();grapple.release();
        for(int y=1;y<6;y++)c.setBlock(new BlockPos(2,y,3),Blocks.STONE);
        Vec3 before=p.position();grapple.tick();p.move(MoverType.SELF,p.getDeltaMovement());
        c.assertTrue(p.position().equals(before) && !p.isNoGravity(),"New obstruction stops flight without phasing");
        p.discard();c.succeed();
    }
}
