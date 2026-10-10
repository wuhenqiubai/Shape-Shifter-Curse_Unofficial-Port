package net.onixary.shapeShifterCurseFabric.additional_power;

import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.*;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Tuple;
import net.onixary.shapeShifterCurseFabric.command.ShapeShifterCurseCommand;
import net.onixary.shapeShifterCurseFabric.items.RegCustomItem;
import net.onixary.shapeShifterCurseFabric.perk.*;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;
import net.onixary.shapeShifterCurseFabric.player_form.utils.PlayerFormComponent;

import java.util.ArrayList;
import java.util.List;

public class SubformPerkCheck {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void directedParticleOffsets(GameTestHelper c) {
        var actor = c.makeMockPlayer(GameType.SURVIVAL);
        var target = EntityType.COW.create(c.getLevel());
        actor.setPos(0,1,0);target.setPos(3,2,4);
        var factory = DirectedParticlesAction.getFactory();
        var json = JsonParser.parseString("""
                {"particle":"minecraft:flame", "speed":0.5, "count":3,
                 "actor_offset":{"x":1,"y":2,"z":3},
                 "target_offset":{"x":-1,"y":1,"z":0}}
                """).getAsJsonObject();
        var a = actor.getBoundingBox().getCenter().add(1,2,3);
        var b = target.getBoundingBox().getCenter().add(-1,1,0);
        // 1.21 起 SerializableData 的 read 要带 HolderLookup.Provider
        var registries = c.getLevel().registryAccess();
        var forward = DirectedParticlesAction.packet(factory.getSerializableData().read(json, registries),actor,target);
        // ⚠ Yarn Vec3d.multiply(double) 是标量缩放 → Mojmap 的对应方法是 scale（multiply 是逐分量乘）
        var velocity = new Vec3(forward.getXDist(),forward.getYDist(),forward.getZDist()).scale(forward.getMaxSpeed());
        c.assertTrue(new Vec3(forward.getX(),forward.getY(),forward.getZ()).distanceTo(a)<1e-6,"Actor offset is relative to its body center");
        c.assertTrue(velocity.distanceTo(b.subtract(a).normalize().scale(.5))<1e-6 && forward.getCount()==0,"Packet uses directed velocity mode, not random spread");
        json.addProperty("direction","target_to_actor");
        var reverse = DirectedParticlesAction.packet(factory.getSerializableData().read(json, registries),actor,target);
        c.assertTrue(new Vec3(reverse.getX(),reverse.getY(),reverse.getZ()).distanceTo(b)<1e-6,"Reverse emission starts at target offset");
        c.assertTrue(Math.abs(reverse.getXDist()+forward.getXDist())<1e-6 && Math.abs(reverse.getYDist()+forward.getYDist())<1e-6 && Math.abs(reverse.getZDist()+forward.getZDist())<1e-6,"Reversing retains actor/target offset ownership");
        json.addProperty("type","shape-shifter-curse:directed_particles");
        ApoliDataTypes.BIENTITY_ACTION.read(json, registries).accept(new Tuple<>(actor,target));
        json.remove("actor_offset");json.remove("target_offset");
        var zero = DirectedParticlesAction.packet(factory.getSerializableData().read(json, registries),actor,actor);
        c.assertTrue(zero.getXDist()==0 && zero.getYDist()==0 && zero.getZDist()==0,"Coincident endpoints have zero velocity");
        json.addProperty("direction","invalid");boolean rejected=false;
        try { factory.read(json, registries); } catch (RuntimeException expected) { rejected=true; }
        c.assertTrue(rejected,"Unknown direction rejected during parsing");c.succeed();
    }
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void respawnRestoresUnlockedPerks(GameTestHelper c) {
        var p = c.makeMockServerPlayerInLevel();
        var form = RegPlayerForms.BAT_3_SUB_AVALI;
        FormUtils.setForm(p,form);
        PerkUtils.__addPerk(p,form.getPerkTreeID(),id("avali_environmental_protection_1"));
        PerkUtils.__addPerk(p,form.getPerkTreeID(),id("avali_nano_coating_1"));
        var upgraded = PowerTypeRegistry.get(id("perks/avali_environmental_protection_1"));
        var base = PowerTypeRegistry.get(id("sub_form_avali_water_slowness"));
        c.assertTrue(PowerHolderComponent.KEY.get(p).hasPower(upgraded),"Perk applies before death");
        p.setHealth(0);
        // Yarn PlayerManager.respawnPlayer(p,false) → Mojmap 3 参版；原版死亡重生走的正是 KILLED
        var respawned = c.getLevel().getServer().getPlayerList().respawn(p,false,net.minecraft.world.entity.Entity.RemovalReason.KILLED);
        try {
            c.assertTrue(PerkUtils.getPlayerPerks(respawned,form.getPerkTreeID()).contains(id("avali_environmental_protection_1")),"Unlock record survives real respawn");
            c.assertTrue(PowerHolderComponent.KEY.get(respawned).hasPower(upgraded),"Unlocked power restored after all respawn component copies");
            c.assertTrue(!PowerHolderComponent.KEY.get(respawned).hasPower(base),"Replaced base power remains removed after respawn");
            c.assertTrue(PowerHolderComponent.KEY.get(respawned).hasPower(PowerTypeRegistry.get(id("perks/avali_nano_coating_1"))),"Additional perk restored after respawn");
        } finally { respawned.discard(); }
        c.succeed();
    }
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void perkCommands(GameTestHelper c) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var p = c.makeMockServerPlayerInLevel();
        var forms = PlayerFormComponent.COMPONENT.get(p);
        var avali = RegPlayerForms.BAT_3_SUB_AVALI;
        FormUtils.setForm(p,avali);
        var dispatcher = new CommandDispatcher<CommandSourceStack>();
        ShapeShifterCurseCommand.register(dispatcher);
        // Yarn CommandSource.withLevel(n) → Mojmap withPermission(n)
        var source = p.createCommandSourceStack().withPermission(2);
        String prefix = "shape_shifter_curse perk ";
        c.assertTrue(!dispatcher.getRoot().getChild("shape_shifter_curse").getChild("perk").canUse(source.withPermission(0)),"Non-OP cannot use debug commands");
        c.assertTrue(dispatcher.execute(prefix+"list",source)==7,"Lists current form tree");
        dispatcher.execute(prefix+"toggle_free true",source);
        p.getAbilities().instabuild = false;
        c.assertTrue(PerkUtils.isFreeUnlock(p),"Free unlock independent of creative mode");
        var nbt = new CompoundTag();
        var registries = c.getLevel().registryAccess();
        forms.writeToNbt(nbt, registries);forms.freePerkForms.clear();forms.readFromNbt(nbt, registries);
        c.assertTrue(PerkUtils.isFreeUnlock(p),"Free setting survives NBT sync and reload");
        forms.nowPerkTree=id("snow_fox_3_perk_tree");
        c.assertTrue(dispatcher.execute(prefix+"unlock_all",source)==7,"Uses actual form rather than preview tree");
        c.assertTrue(dispatcher.execute(prefix+"unlock_all",source)==0,"Repeated unlock is idempotent");
        var holder = PowerHolderComponent.KEY.get(p);
        var base = PowerTypeRegistry.get(id("sub_form_avali_water_slowness"));
        var upgraded = PowerTypeRegistry.get(id("perks/avali_environmental_protection_1"));
        c.assertTrue(!holder.hasPower(base) && holder.hasPower(upgraded),"Unlock applies replacement");
        var other = id("snow_fox_3_perk_tree");forms.formPerkMap.put(other,new ArrayList<>(List.of(id("snow_fox_revenge"))));
        c.assertTrue(dispatcher.execute(prefix+"reset_all",source)==7,"Reset all current nodes");
        c.assertTrue(holder.hasPower(base) && !holder.hasPower(upgraded),"Reset restores base powers");
        c.assertTrue(forms.formPerkMap.get(other).size()==1,"Other tree unlocks preserved");
        dispatcher.execute(prefix+"toggle_free false",source);c.assertTrue(!PerkUtils.isFreeUnlock(p),"Free mode turns off");
        p.discard();c.succeed();
    }
    private static ResourceLocation id(String s) { return ResourceLocation.fromNamespaceAndPath("shape-shifter-curse",s); }
    private static Power add(Player p,String s) {
        var type=PowerTypeRegistry.get(id("perks/"+s));
        var holder=PowerHolderComponent.KEY.get(p);holder.addPower(type,id("subform_test"));return holder.getPower(type);
    }
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void registrations(GameTestHelper c) {
        int count=0;
        for(String tree:new String[]{"marbled_polecat","avali"}) for(var node:RegPerks.getPerkTree(id(tree+"_perk_tree")).getAllNodes()) {
            count++;var perk=(NormalPerk)RegPerks.getPerk(node.perkID);
            for(var power:perk.powerAdd)c.assertTrue(PowerTypeRegistry.get(power)!=null,"Loaded "+power);
            for(var power:perk.powerRemove)c.assertTrue(PowerTypeRegistry.get(power)!=null,"Replacement "+power);
        }
        c.assertTrue(count==13,"Thirteen subform perks");c.succeed();
    }
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void jumpsDashAndCombat(GameTestHelper c) {
        var p = c.makeMockPlayer(GameType.SURVIVAL);
        var jump=(AirJumpPower)add(p,"marbled_polecat_double_air_jump");
        var registries = c.getLevel().registryAccess();
        p.setOnGround(false);jump.tick();jump.tick();jump.onUse();jump.onUse();
        // 1.21 起 Power#toTag/fromTag 多了 HolderLookup.Provider 参数
        c.assertTrue(((IntTag)jump.toTag(registries)).getAsInt()==2,"Two extra jumps");
        p.setDeltaMovement(0,-1,0);jump.onUse();c.assertTrue(p.getDeltaMovement().y==-1,"Third extra jump blocked");
        p.setOnGround(true);jump.tick();c.assertTrue(((IntTag)jump.toTag(registries)).getAsInt()==0,"Landing resets jumps");
        jump.fromTag(ByteTag.ONE, registries);c.assertTrue(((IntTag)jump.toTag(registries)).getAsInt()==1,"Legacy jump NBT");
        c.setBlock(new BlockPos(0,0,0),Blocks.STONE);
        p.setPos(c.absoluteVec(new Vec3(.5,1,.5)));p.setYRot(0);p.setXRot(80);
        var dash=(ActiveCooldownPower)add(p,"marbled_polecat_leaping_dash");dash.fromTag(LongTag.valueOf(-10000), registries);
        p.getFoodData().setFoodLevel(6);dash.onUse();c.assertTrue(p.getDeltaMovement().y==-1,"Six food cannot dash");
        p.getFoodData().setFoodLevel(7);dash.onUse();
        c.assertTrue(p.getFoodData().getFoodLevel()==7 && p.getDeltaMovement().z>1.19 && p.getDeltaMovement().y>.59,"Dash horizontal impulse without food cost");
        add(p,"marbled_polecat_combat_noodle_melee");var target=EntityType.ZOMBIE.create(c.getLevel());
        c.assertTrue(ActionOnCombatHitPower.meleeBonus(p,target,false)==4 && ActionOnCombatHitPower.meleeBonus(p,target,true)==0,"Only noncritical melee gets bonus");c.succeed();
    }
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void recipesAndCoatings(GameTestHelper c) {
        var p = c.makeMockPlayer(GameType.SURVIVAL);
        for(int tier=1;tier<=2;tier++) {
            var craft=(ItemOnItemPower)add(p,"avali_tool_modification_"+tier);
            var using=new ItemStack(tier==1?Items.COAL_BLOCK:Items.DIAMOND,2);
            var old=new ItemStack(tier==1?Items.GOLDEN_SWORD:Items.BOW);old.setDamageValue(10);
            // Enchantments.UNBREAKING 是 ResourceKey，enchant 只收 Holder（直接强转会 ClassCastException）
            old.enchant(c.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.UNBREAKING),3);
            p.getInventory().setItem(0,old);var slot=new Slot(p.getInventory(),0,0,0);
            c.assertTrue(craft.doesApply(using,old),"Recipe input");craft.execute(using,old,slot);
            c.assertTrue(using.getCount()==1 && slot.getItem().is(tier==1?RegCustomItem.GRAPHENE_BLADE:RegCustomItem.COMPOUND_KINETIC_BOW) && !slot.getItem().isEnchanted() && slot.getItem().getDamageValue()==0,"Fresh output consumes inputs without enchantments");
        }
        add(p,"avali_nano_coating_1");add(p,"avali_nano_coating_2");
        var iron=new ItemStack(Items.IRON_INGOT,2);p.setItemInHand(InteractionHand.MAIN_HAND,iron);iron.use(c.getLevel(),p,InteractionHand.MAIN_HAND);
        c.assertTrue(iron.getCount()==1 && p.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier()==0,"Tier II retains iron use");
        // Yarn PlayerEntity.getItemCooldownManager().remove(item) → Mojmap ItemCooldowns.removeCooldown(item)
        p.getCooldowns().removeCooldown(Items.IRON_INGOT);p.getCooldowns().removeCooldown(Items.DIAMOND);
        var diamond=new ItemStack(Items.DIAMOND,2);p.setItemInHand(InteractionHand.MAIN_HAND,diamond);diamond.use(c.getLevel(),p,InteractionHand.MAIN_HAND);
        c.assertTrue(diamond.getCount()==1 && p.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier()==1 && p.hasEffect(MobEffects.FIRE_RESISTANCE),"Diamond adds stronger coating");c.succeed();
    }
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void compoundBowProjectile(GameTestHelper c) {
        var p = c.makeMockPlayer(GameType.SURVIVAL);
        p.setPos(c.absoluteVec(new Vec3(.5,2,.5)));
        p.getInventory().setItem(1,new ItemStack(Items.ARROW,2));
        var bow=new ItemStack(RegCustomItem.COMPOUND_KINETIC_BOW);
        p.setItemInHand(InteractionHand.MAIN_HAND,bow);
        // Mojmap 的 Item#onStoppedUsing 是 protected，公开入口是 releaseUsing；
        // 且 getMaxUseTime() → getUseDuration(实体)
        bow.getItem().releaseUsing(bow,c.getLevel(),p,bow.getUseDuration(p)-20);
        var arrows=c.getLevel().getEntitiesOfClass(AbstractArrow.class,p.getBoundingBox().inflate(3),a->a.getOwner()==p);
        c.assertTrue(arrows.size()==1 && arrows.get(0).getBaseDamage()==2.5,"Actual bow shot applies 25 percent base damage bonus");
        c.assertTrue(p.getInventory().getItem(1).getCount()==1 && bow.getDamageValue()==1,"Bow consumes arrow and durability");
        arrows.forEach(net.minecraft.world.entity.Entity::discard);c.succeed();
    }
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void rescueRetriggersAtLowHealth(GameTestHelper c) {
        var p = c.makeMockPlayer(GameType.SURVIVAL);
        var cooldown=(VariableIntPower)add(p,"avali_emergency_protocol_cooldown");
        var tick=add(p,"avali_emergency_protocol_tick");p.setHealth(7);tick.tick();tick.tick();
        c.assertTrue(cooldown.getValue()==0,"Above threshold does not trigger");
        p.setHealth(6);tick.tick();c.assertTrue(cooldown.getValue()==12000 && p.getEffect(MobEffects.MOVEMENT_SPEED).getAmplifier()==1,"Low health triggers ten minute cooldown");
        p.removeAllEffects();tick.tick();c.assertTrue(!p.hasEffect(MobEffects.MOVEMENT_SPEED),"Cooldown blocks rescue");
        cooldown.setValue(1);tick.tick();tick.tick();
        c.assertTrue(p.hasEffect(MobEffects.MOVEMENT_SPEED) && cooldown.getValue()==12000,"Retriggers without healing above threshold");c.succeed();
    }
}
