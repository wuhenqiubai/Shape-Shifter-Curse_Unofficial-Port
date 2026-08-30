package net.onixary.shapeShifterCurseFabric.player_form.forms;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_animation.AnimationHolder;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.*;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateControllerDP.ClimbAnimController;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateControllerDP.OneAnimController;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateControllerDP.RideAnimController;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateControllerDP.WithSneakAnimController;
import net.onixary.shapeShifterCurseFabric.player_form.NormalSubForm;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.utils.IPatronForm;
import net.onixary.shapeShifterCurseFabric.player_form.utils.ModifyCapeRender;
import net.onixary.shapeShifterCurseFabric.util.Verify.PatronDataSegment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class Form_Bat3_Sub_Avali extends NormalSubForm implements IPatronForm, ModifyCapeRender {
    public Form_Bat3_Sub_Avali(ResourceLocation formID) {
        super(formID, RegPlayerForms.BAT_3);
        this.removePower(ShapeShifterCurseFabric.identifier("form_bat_3_sky_speed"));
        this.removePower(ShapeShifterCurseFabric.identifier("form_bat_3_damage_up_when_no_sun"));
        this.removePower(ShapeShifterCurseFabric.identifier("form_bat_3_ground_speed_down"));
        this.removePower(ShapeShifterCurseFabric.identifier("prevent_ranged_weapon_use"));
        this.removePower(ShapeShifterCurseFabric.identifier("form_vegetarian"));
        this.removePower(ShapeShifterCurseFabric.identifier("form_vegetarian_food_up"));
        this.removePower(ShapeShifterCurseFabric.identifier("bat_vision"));
        this.removePower(ShapeShifterCurseFabric.identifier("form_bat_2_sun_health"));
        this.removePower(ShapeShifterCurseFabric.identifier("drop_tool_after_digging"));
        this.removePower(ShapeShifterCurseFabric.identifier("drop_weapon_after_hit"));
        this.removePower(ShapeShifterCurseFabric.identifier("barehand_digging_speed_up"));
        this.removePower(ShapeShifterCurseFabric.identifier("always_harvest"));
        this.removePower(ShapeShifterCurseFabric.identifier("form_camera_bobbing_bat"));
        this.removePower(ShapeShifterCurseFabric.identifier("form_bat_3_block_attach"));
        this.removePower(ShapeShifterCurseFabric.identifier("no_render_arm"));
        this.addPower(ShapeShifterCurseFabric.identifier("sub_form_avali_side_block_attach"));
        this.addPower(ShapeShifterCurseFabric.identifier("sub_form_avali_step_sound"));
    }

    @Override
    public @Nullable Tuple<ResourceLocation, ResourceLocation> getRenderLayerOverride() {
        return new Tuple<>(ResourceLocation.fromNamespaceAndPath("origins", "origin"), ResourceLocation.fromNamespaceAndPath(this.getFormID().getNamespace(), "form_" + this.getFormID().getPath()));
    }

    @Override
    public boolean checkCanUse(@Nullable Player player, @Nullable UUID playerUUID, @Nullable PatronDataSegment patronData) {
        if (patronData == null || player == null) {
            return false;
        }
        return patronData.getLevel() >= 5;
    }

    // 以下暂时沿用 bat_3 的披风渲染参数

    @Override
    public Vec3 getCapeIdleLoc(AbstractClientPlayer player) {
        if (player.onGround()) {
            return new Vec3(0.0f, 0.7f, 0.2f);
        }
        else {
            return new Vec3(0.0, 0.0, 0.125);
        }
    }

    @Override
    public float getCapeBaseRotateAngle(AbstractClientPlayer player) {
        return 100.0f;
    }

    @Override
    public boolean NeedModifyXRotationAngle() {
        return true;
    }

    // avali 专属动画 无 avali 版本的动画(jump/riding)沿用 bat 家族的缺失回退模式

    public static final AnimUtils.AnimationHolderData ANIM_CLIMB =
            new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("avali_climb"));
    public static final AnimUtils.AnimationHolderData ANIM_CLIMB_IDLE =
            new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("avali_attach_side"));
    public static final AnimUtils.AnimationHolderData ANIM_SLEEP =
            new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("avali_sleep"));
    public static final AnimUtils.AnimationHolderData ANIM_ELYTRA_FLY =
            new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("avali_elytra_fly"));


    public static final AbstractAnimStateController IDLE_CONTROLLER = new WithSneakAnimController(new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("avali_idle")), new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("avali_sneak_idle")));
    // 为预防缩放导致的动画帧精度损失，模型中动画时间*2，同时动画器中速度也*2
    public static final AbstractAnimStateController WALK_CONTROLLER = new WithSneakAnimController(new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("avali_walk"), 1.5f * 2.0f, 4), new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("avali_sneak_walk")));
    public static final AbstractAnimStateController SPRINT_CONTROLLER = new WithSneakAnimController(new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("avali_run"), 1.5f * 2.0f, 4), new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("avali_sneak_walk")));
    public static final AbstractAnimStateController MINING_CONTROLLER = new OneAnimController(new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("avali_digging"), 1.8f, 2));
    public static final AbstractAnimStateController ATTACK_CONTROLLER = new OneAnimController(new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("avali_attack"), 1.8f, 2));
    public static final AbstractAnimStateController JUMP_CONTROLLER = new OneAnimController(new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("avali_jump"), 1.5f, 2));
    public static final AbstractAnimStateController FALL_CONTROLLER = new OneAnimController(new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("avali_slow_falling")));
    public static final AbstractAnimStateController RIDE_CONTROLLER = new RideAnimController(new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("avali_ride")), new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("avali_ride")));
    public static final AbstractAnimStateController FLYING_CONTROLLER = new OneAnimController(new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("avali_slow_falling")));
    public static final AbstractAnimStateController CLIMB_CONTROLLER = new ClimbAnimController(ANIM_CLIMB_IDLE, ANIM_CLIMB);
    public static final AbstractAnimStateController SLEEP_CONTROLLER = new OneAnimController(ANIM_SLEEP);
    public static final AbstractAnimStateController FALL_FLYING_CONTROLLER = new OneAnimController(ANIM_ELYTRA_FLY);

    @Override
    public @Nullable AbstractAnimStateController getAnimStateController(Player player, AnimSystem.AnimSystemData animSystemData, @NotNull ResourceLocation animStateID) {
        @Nullable AnimStateEnum animStateEnum = AnimStateEnum.getStateEnum(animStateID);
        if (animStateEnum != null) {
            switch (animStateEnum) {
                case ANIM_STATE_FALL:
                    return FALL_CONTROLLER;
                case ANIM_STATE_JUMP:
                    return JUMP_CONTROLLER;
                case ANIM_STATE_RIDE:
                    return RIDE_CONTROLLER;
                case ANIM_STATE_WALK:
                    return WALK_CONTROLLER;
                case ANIM_STATE_SPRINT:
                    return SPRINT_CONTROLLER;
                case ANIM_STATE_IDLE:
                    return IDLE_CONTROLLER;
                case ANIM_STATE_CLIMB:
                    return CLIMB_CONTROLLER;
                case ANIM_STATE_MINING:
                    return MINING_CONTROLLER;
                case ANIM_STATE_ATTACK:
                    return ATTACK_CONTROLLER;
                case ANIM_STATE_FLYING:
                    return FLYING_CONTROLLER;
                case ANIM_STATE_USE_ITEM:
                    return IDLE_CONTROLLER;
                case ANIM_STATE_SLEEP:
                    return SLEEP_CONTROLLER;
                case ANIM_STATE_CRAWL:
                    return FLYING_CONTROLLER;
                case ANIM_STATE_FALL_FLYING:
                    return FALL_FLYING_CONTROLLER;
                default:
                    return null;
            }
        }
        return super.getAnimStateController(player, animSystemData, animStateID);
    }

    // attach_side 使用 avali 专属动画覆盖 attach_bottom 暂无 avali 版本 经 NormalSubForm 委托沿用主形态 bat_3 的注册

    private static AnimationHolder POWER_ANIM_ATTACH_SIDE = AnimationHolder.EMPTY;

    @Override
    public void registerPowerAnim(Player player, AnimSystem.AnimSystemData animSystemData) {
        POWER_ANIM_ATTACH_SIDE = new AnimationHolder(ShapeShifterCurseFabric.identifier("avali_attach_side"), true);
        super.registerPowerAnim(player, animSystemData);
    }

    @Override
    public @NotNull Tuple<Boolean, @Nullable AnimationHolder> getPowerAnim(Player player, AnimSystem.AnimSystemData animSystemData, @NotNull ResourceLocation powerAnimID) {
        if (powerAnimID.equals(AnimRegistries.POWER_ANIM_ATTACH_SIDE)) {
            return new Tuple<>(true, POWER_ANIM_ATTACH_SIDE);
        }
        return super.getPowerAnim(player, animSystemData, powerAnimID);
    }
}
