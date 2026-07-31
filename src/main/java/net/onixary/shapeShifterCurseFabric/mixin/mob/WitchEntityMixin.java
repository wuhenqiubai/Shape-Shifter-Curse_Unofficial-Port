package net.onixary.shapeShifterCurseFabric.mixin.mob;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.additional_power.WitchFriendlyPower;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Witch.class)
public abstract class WitchEntityMixin {

	@Unique
    private static final float POTION_REPLACE_CHANCE = 0.6f;

	@Inject(method = "performRangedAttack", at = @At("HEAD"), cancellable = true)
    private void injectCustomPotionAttack(LivingEntity target, float pullProgress, CallbackInfo ci) {
        Witch witch = (Witch) (Object) this;
        Level world = witch.level();

        if(target instanceof Player player){
            if (PowerHolderComponent.hasPower(player, WitchFriendlyPower.class)) {
                ci.cancel();
            }
            if (RegPlayerForms.ORIGINAL_SHIFTER.isPlayerForm(player) || (ShapeShifterCurseFabric.commonConfig.witchPotionForPreBook && RegPlayerForms.ORIGINAL_BEFORE_ENABLE.isPlayerForm(player))){
                double randomChance = Math.random();
                if(randomChance < POTION_REPLACE_CHANCE){
                    Vec3 vec3d = target.getDeltaMovement();
                    double d = target.getX() + vec3d.x - witch.getX();
                    double e = target.getEyeY() - (double)1.1F - witch.getY();
                    double f = target.getZ() + vec3d.z - witch.getZ();
                    double g = Math.sqrt(d * d + f * f);

                    // 创建自定义溅射式药水
                    net.minecraft.core.Holder<net.minecraft.world.item.alchemy.Potion> familiarFoxPotion =
                            net.minecraft.core.registries.BuiltInRegistries.POTION.get(
                                    net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.POTION,
                                            net.minecraft.resources.Identifier.fromNamespaceAndPath("shape-shifter-curse", "to_familiar_fox_0_potion")))
                                    .orElseThrow();
                    ItemStack potionStack = PotionContents.createItemStack(Items.SPLASH_POTION, familiarFoxPotion);
                    ThrownSplashPotion customPotion = new ThrownSplashPotion(world, witch, potionStack);
                    customPotion.setXRot(customPotion.getXRot() - -20.0F);
                    customPotion.shoot(d, e + g * 0.2, f, 0.75F, 8.0F);

                    if (!witch.isSilent()) {
                        witch.level().playSound(null, witch.getX(), witch.getY(), witch.getZ(), SoundEvents.WITCH_THROW, witch.getSoundSource(), 1.0F, 0.8F);
                    }
                    // 发射自定义药水
                    world.addFreshEntity(customPotion);

                    // 取消原始攻击逻辑
                    ci.cancel();
                }
            }
            else if (PowerHolderComponent.hasPower(target, WitchFriendlyPower.class)) {
                ci.cancel();
            }
        }
    }


}