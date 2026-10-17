package net.onixary.shapeShifterCurseFabric.player_form;

import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.player_animation.AnimationHolder;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AbstractAnimStateController;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class NormalSubForm extends NormalForm implements ISubForm {
    public final @NotNull IForm masterForm;
    public final @NotNull List<Identifier> power_ADD = new ArrayList<>();
    public final @NotNull List<Identifier> power_REMOVE = new ArrayList<>();
    private @Nullable Consumer<Player> applyScaleFunc = null;

    public NormalSubForm(Identifier formID, @NotNull IForm masterForm) {
        super(formID);
        this.masterForm = masterForm;
        this.loadAllDataFormMasterForm();
    }

    @Override
    public @NotNull IForm getMasterForm() {
        return masterForm;
    }

    @Override
    public @Nullable Tuple<List<Identifier>, List<Identifier>> getLayerModifier() {
        return new Tuple<>(power_ADD, power_REMOVE);
    }

    public void addPower(Identifier... powerIDs) {
        for (Identifier powerID : powerIDs) {
            if (!power_ADD.contains(powerID)) {
                power_ADD.add(powerID);
            }
        }
    }

    public void removePower(Identifier... powerIDs) {
        for (Identifier powerID : powerIDs) {
            if (!power_REMOVE.contains(powerID)) {
                power_REMOVE.add(powerID);
            }
        }
    }

    public void loadAllDataFormMasterForm() {
        IForm masterForm = this.getMasterForm();
        this.bodyType(masterForm.getBodyType());
        IFormGroup masterGroup = masterForm.getFormGroup();
        if (masterGroup != null) {
            masterGroup.registerForm(masterForm.getFormTier(), 0, this);
        }
        this.formFlag(masterForm.getFormFlag().toArray(new String[0]));
    }

    // 写这段代码时忘了返回的是非null值 如果没变化就返回自身 不能直接返回父形态的返回值 得处理一下
    @Override
    public @NotNull IForm _getNextForm(Player player, ITransformReason reason) {
        IForm targetForm = this.getMasterForm()._getNextForm(player, reason);
        if (targetForm.isEquals(this.getMasterForm())) {
            return this;
        }
        return targetForm;
    }

    @Override
    public @NotNull IForm _getPrevForm(Player player, ITransformReason reason) {
        return this.getMasterForm()._getPrevForm(player, reason);
    }

    @Override
    public @Nullable AbstractAnimStateController getAnimStateController(Player player, AnimSystem.AnimSystemData animSystemData, @NotNull Identifier animStateID) {
        return this.getMasterForm().getAnimStateController(player, animSystemData, animStateID);
    }

    @Override
    public void registerPowerAnim(Player player, AnimSystem.AnimSystemData animSystemData) {
        this.getMasterForm().registerPowerAnim(player, animSystemData);
    }


    @Override
    public boolean isPowerAnimRegistered(Player player, AnimSystem.AnimSystemData animSystemData) {
        return this.getMasterForm().isPowerAnimRegistered(player, animSystemData);
    }

    @Override
    public @NotNull Tuple<Boolean, @Nullable AnimationHolder> getPowerAnim(Player player, AnimSystem.AnimSystemData animSystemData, @NotNull Identifier powerAnimID) {
        return this.getMasterForm().getPowerAnim(player, animSystemData, powerAnimID);
    }

    @Override
    public void applyScale(Player player) {
        if (this.applyScaleFunc != null) {
            this.applyScaleFunc.accept(player);
            return;
        } else {
            this.getMasterForm().applyScale(player);
        }
    }

    @Override
    public NormalForm applyScaleFunc(Consumer<Player> func) {
        this.applyScaleFunc = func;
        return this;
    }

    @Override
    public float getDefaultEyeScale() {
        // 子形态若自行配置了 scale 则用自身的，否则沿用 masterForm 的基准 eye_scale
        if (this.applyScaleFunc != null) {
            return super.getDefaultEyeScale();
        }
        return this.getMasterForm().getDefaultEyeScale();
    }

    @Override
    public void onTransform_From(Player player, IForm prevForm) {
        this.getMasterForm().onTransform_From(player, prevForm);
    }

    @Override
    public void onTransform_To(Player player, IForm nextForm) {
        this.getMasterForm().onTransform_To(player, nextForm);
    }

    @Override
    public void onTransform_Finish(Player player) {
        this.getMasterForm().onTransform_Finish(player);
        ISubForm.super.onTransform_Finish(player);
    }

    @Override
    public void afterApplyLayer(Player player) {
        this.getMasterForm().afterApplyLayer(player);
        ISubForm.super.afterApplyLayer(player);
    }

    @Override
    public void onApplyPowerEnd(Player player) {
        this.getMasterForm().onApplyPowerEnd(player);
    }

    @Override
    public void onRegister() {
        ISubForm.super.onRegister();
    }

    @Override
    public @NotNull Tuple<Identifier, Identifier> getFormLayer() {
        return ISubForm.super.getFormLayer();
    }
}