package net.onixary.shapeShifterCurseFabric.entity.projectile;

import net.onixary.shapeShifterCurseFabric.additional_power.TrinketsConditionAction;
import net.onixary.shapeShifterCurseFabric.additional_power.WebBridgeAction;
import net.onixary.shapeShifterCurseFabric.blocks.RegCustomBlock;
import net.onixary.shapeShifterCurseFabric.items.RegCustomItem;
import net.onixary.shapeShifterCurseFabric.status_effects.EntangledEffectUtils;
import org.jetbrains.annotations.Nullable;

import static net.onixary.shapeShifterCurseFabric.entity.RegCustomEntity.WEB_BULLET;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class WebBullet extends ThrowableItemProjectile {
    public @Nullable LivingEntity owner = null;
    public int Tier = 1;
    public boolean EnableEntangledEffect = true;
    public boolean EnableTopBlockBuild = true;
    private boolean launched = false;

    public static final WebBridgeAction.WebLadderConfig ladderConfigTier1 = new WebBridgeAction.WebLadderConfig(10, 14, 8, false, 0.0f);
    public static final WebBridgeAction.WebLadderConfig ladderConfigTier2 = new WebBridgeAction.WebLadderConfig(14, 18, 12, true, 0.25f);
    public static final WebBridgeAction.WebLadderConfig ladderConfigTier3 = new WebBridgeAction.WebLadderConfig(18, 24, 16, true, 0.4f);

    public static final int Tier1BuffTime = 200;
    public static final int Tier2BuffTime = 400;
    public static final int Tier3BuffTime = 600;

    public WebBullet(EntityType<? extends ThrowableItemProjectile> entityType, Level world) {
        super(entityType, world);
        this.Tier = 1;
        this.EnableEntangledEffect = true;
        this.EnableTopBlockBuild = true;
    }

    public WebBullet(double d, double e, double f, Level world, int Tier) {
        super(WEB_BULLET, d, e, f, world);
        this.Tier = Tier;
        this.EnableEntangledEffect = true;
        this.EnableTopBlockBuild = true;
    }

    public WebBullet(@org.jetbrains.annotations.Nullable LivingEntity livingEntity, int Tier) {
        super(WEB_BULLET, livingEntity, livingEntity != null ? livingEntity.level() : null);
        this.Tier = Tier;
        this.owner = livingEntity;
        this.EnableEntangledEffect = true;
        this.EnableTopBlockBuild = true;
    }

    public WebBullet(@org.jetbrains.annotations.Nullable LivingEntity livingEntity, int Tier, boolean EnableEntangledEffect, boolean EnableTopBlockBuild) {
        super(WEB_BULLET, livingEntity, livingEntity != null ? livingEntity.level() : null);
        this.Tier = Tier;
        this.owner = livingEntity;
        this.EnableEntangledEffect = EnableEntangledEffect;
        this.EnableTopBlockBuild = EnableTopBlockBuild;
    }

    @Override
    public Item getDefaultItem() {
        return RegCustomItem.WEB_PROJECTILE;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel serverWorld) {
            if (!launched) {
                launched = true;
                if (this.owner != null) {
                    switch (Tier) {
                        case 1 -> serverWorld.playSound(null, this.owner.getX(), this.owner.getY(), this.owner.getZ(),
                                SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 1.0f, 0.6f + this.random.nextFloat() * 0.4f);
                        case 2 -> serverWorld.playSound(null, this.owner.getX(), this.owner.getY(), this.owner.getZ(),
                                SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 1.0f, 0.9f + this.random.nextFloat() * 0.4f);
                        case 3 -> serverWorld.playSound(null, this.owner.getX(), this.owner.getY(), this.owner.getZ(),
                                SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 1.0f, 1.2f + this.random.nextFloat() * 0.4f);
                    }
                }
            }
            switch (Tier) {
                case 1 -> serverWorld.sendParticles(ParticleTypes.ASH,
                        this.getX(), this.getY(), this.getZ(),
                        3, 0.05, 0.05, 0.05, 0.01);
                case 2 -> serverWorld.sendParticles(ParticleTypes.SPIT,
                        this.getX(), this.getY(), this.getZ(),
                        1, 0.05, 0.05, 0.05, 0.01);
                case 3 -> serverWorld.sendParticles(ParticleTypes.CLOUD,
                        this.getX(), this.getY(), this.getZ(),
                        2, 0.05, 0.05, 0.05, 0.01);
            }

            if (this.level().getBlockState(this.blockPosition()).liquid()) {
                this.discard();
            }

            if (this.level().getBlockState(this.blockPosition()).is(RegCustomBlock.TEMP_WEB_BRIDGE)) {
                this.onHitBlock(new BlockHitResult(this.position(), Direction.DOWN, this.blockPosition(), false));
            }
        }
    }

    private boolean isExtraHandVenomSpindleEquipped(Player player) {
        /*
         Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(player);
         if (component.isEmpty()) {
             return false;
         }
         Map<String, TrinketInventory> groupInv = component.get().getInventory().get("hand");
         if (groupInv == null) {
             return false;
         }
         TrinketInventory inv = groupInv.get("extra_hand");
         if (inv == null) {
             return false;
         }
         return inv.getStack(0).isOf(RegCustomItem.VENOM_SPINDLE);
        */
        return TrinketsConditionAction.CheckEquipped(
                player, "auto", "hand", "extra_hand", 0,
                stack -> stack.is(RegCustomItem.VENOM_SPINDLE),
                false
        );
    }

    private void playHitEffects() {
        if (this.level() instanceof ServerLevel serverWorld) {
            serverWorld.sendParticles(ParticleTypes.CLOUD,
                    this.getX(), this.getY(), this.getZ(),
                    20, 0.3, 0.3, 0.3, 0.05);
            serverWorld.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.WET_GRASS_BREAK, SoundSource.NEUTRAL, 1.0f, 0.8f + this.random.nextFloat() * 0.4f);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("web_projectile", true);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
    }

    @Override
    public void onHitBlock(BlockHitResult blockHitResult) {
        WebBridgeAction.WebLadderConfig nowConfig = null;
        switch (Tier) {
            case 1 -> nowConfig = ladderConfigTier1;
            case 2 -> nowConfig = ladderConfigTier2;
            case 3 -> nowConfig = ladderConfigTier3;
            default -> nowConfig = ladderConfigTier1;
        }
        if(!EnableTopBlockBuild){
            nowConfig = new WebBridgeAction.WebLadderConfig(nowConfig.SideBlockNum(), nowConfig.BottomBlockNum(), 0, nowConfig.LargerLadder(), nowConfig.LargerLadderCountPercent());
        }
        WebBridgeAction.BuildWebLadder(this.level(), blockHitResult, nowConfig, RegCustomBlock.TEMP_WEB_BRIDGE);
        playHitEffects();
        this.discard();
    }

    @Override
    public void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        Entity entity = entityHitResult.getEntity();

        //ShapeShifterCurseFabric.LOGGER.info("Hit entity " + entity.getName().getString());

        // 检测 owner 的 extra_hand 槽位是否装备了箭毒纺锤，并根据tier形态施加效果
        if (this.owner instanceof Player player && entity instanceof LivingEntity target) {
            //ShapeShifterCurseFabric.LOGGER.info("Check hit living entity " + entity.getName().getString());
            if (isExtraHandVenomSpindleEquipped(player)) {
                switch (Tier) {
                    case 1 -> {
                        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1));
                        target.addEffect(new MobEffectInstance(MobEffects.POISON, 20, 2));
                        target.hurt(this.damageSources().thrown(this, this.owner), 5.0F);
                    }
                    case 2 -> {
                        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 2));
                        target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 2));
                        target.hurt(this.damageSources().thrown(this, this.owner), 6.0F);
                    }
                    case 3 -> {
                        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 3));
                        target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 2));
                        target.hurt(this.damageSources().thrown(this, this.owner), 8.0F);
                    }
                }
            }
            else {
                // 原版蛛网弹效果
                switch (Tier) {
                    case 1 -> {
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
                        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1));
                        if(EnableEntangledEffect){
                            EntangledEffectUtils.applyEntangledEffect(this.getOwner(), target, Tier1BuffTime);
                        }

                    }
                    case 2 -> {
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2));
                        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 2));
                        if(EnableEntangledEffect){
                            EntangledEffectUtils.applyEntangledEffect(this.getOwner(), target, Tier2BuffTime);
                        }
                    }
                    case 3 -> {
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 3));
                        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 3));
                        if(EnableEntangledEffect){
                            EntangledEffectUtils.applyEntangledEffect(this.getOwner(), target, Tier3BuffTime);
                        }
                    }
                }
            }
        }

        playHitEffects();
        this.discard();
    }
}
