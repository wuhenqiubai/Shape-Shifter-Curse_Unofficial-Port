package net.onixary.shapeShifterCurseFabric.player_form.utils;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.util.InitialFormUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PlayerFormComponent implements AutoSyncedComponent {
    public static final ComponentKey<PlayerFormComponent> COMPONENT = ComponentRegistry.getOrCreate(ShapeShifterCurseFabric.identifier("player_form"), PlayerFormComponent.class);

    // form的2个值禁止使用非setForm函数修改 除非你知道你在干什么 读取可以直接读 但是修改请使用setForm
    public @NotNull IForm nowForm = RegPlayerForms.ORIGINAL_BEFORE_ENABLE;
    public @Nullable Identifier nowFormID = nowForm.getFormID();
    public @NotNull Identifier fallbackFormID = RegPlayerForms.ORIGINAL_BEFORE_ENABLE.getFormID();
    public final List<IForm> formHistory = new ArrayList<>();
    // 诅咒之月逻辑
    public boolean isCursedMoonApplied = false;
    public boolean lastTransformByCure = false;  // 仅用于诅咒之月 进入和退出诅咒之月时会清空
    public @Nullable IForm BeforeCursedMoonAppliedForm = null;
    public @Nullable IForm AfterCursedMoonAppliedForm = null;
    // 变形系统
    public @Nullable IForm transformTargetForm = null;
    // CTP系统
    public Identifier customPotionFormID = RegPlayerForms.ORIGINAL_BEFORE_ENABLE.getFormID();
    // 本能系统
    public float instinctValue = 0.0f;
    public float instinctRate = 0.0f;
    public HashMap<Identifier, InstinctUtils.InstinctEffect> instinctEffects = new HashMap<>();

    // 临时变量
    public Player player = null;

    public PlayerFormComponent(Player player) {
        this.player = player;
        this.nowForm = InitialFormUtils.getInitialForm(player);
        this.nowFormID = nowForm.getFormID();
        this.fallbackFormID = nowForm.getFormID();
    }

    public @NotNull IForm getFallbackForm() {
        IForm form = RegPlayerForms.getPlayerForm(this.fallbackFormID);
        if (form == null) {
            return InitialFormUtils.getInitialForm(player);
        }
        return form;
    }

    public void setFallbackForm(@Nullable Identifier formID) {
        IForm form = RegPlayerForms.getPlayerForm(formID);
        if (form == null) {
            ShapeShifterCurseFabric.LOGGER.warn("Fallback form not found");
            formID = InitialFormUtils.getInitialForm(player).getFormID();
        } else if (form.isDynamicForm()) {
            ShapeShifterCurseFabric.LOGGER.warn("Fallback form not supported dynamic form");
            formID = InitialFormUtils.getInitialForm(player).getFormID();
        } else if (form instanceof NeedCheckUsableForm) {
            ShapeShifterCurseFabric.LOGGER.warn("Fallback form not supported need check usable form");
            formID = InitialFormUtils.getInitialForm(player).getFormID();
        }
        this.fallbackFormID = formID;
    }

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        if (tag.contains("no_form_id") && tag.getBoolean("no_form_id")) {
            nowFormID = null;
            nowForm = RegPlayerForms.ORIGINAL_BEFORE_ENABLE;
        } else {
            if (tag.contains("nowFormID")) {
                nowFormID = Identifier.tryParse(tag.getString("nowFormID"));
                nowForm = FormUtils.parseForm(nowFormID, RegPlayerForms.ORIGINAL_BEFORE_ENABLE);
            } else {
                nowFormID = RegPlayerForms.ORIGINAL_BEFORE_ENABLE.getFormID();
                nowForm = RegPlayerForms.ORIGINAL_BEFORE_ENABLE;
            }
        }
        if (tag.contains("fallbackFormID")) {
            Identifier fallbackFormIDNullable = Identifier.tryParse(tag.getString("fallbackFormID"));
            fallbackFormID = fallbackFormIDNullable == null ? RegPlayerForms.ORIGINAL_BEFORE_ENABLE.getFormID() : fallbackFormIDNullable;
        }
        // 旧版兼容补丁 只迁移形态数据 其他全Drop了
        if (tag.contains("currentForm")) {
            nowFormID = Identifier.tryParse(tag.getString("currentForm"));
            nowForm = FormUtils.parseForm(nowFormID, RegPlayerForms.ORIGINAL_BEFORE_ENABLE);
        }
        if (tag.contains("formHistory")) {
            formHistory.clear();
            ListTag history = tag.getList("formHistory", Tag.TAG_STRING);
            formHistory.clear();
            for (Tag element : history) {
                IForm form = FormUtils.parseForm(Identifier.tryParse(element.getAsString()), null);
                if (form != null) {
                    formHistory.add(form);
                }
            }
        }
        if (tag.contains("isCursedMoonApplied")) {
            isCursedMoonApplied = tag.getBoolean("isCursedMoonApplied");
        } else {
            isCursedMoonApplied = false;
        }
        if (tag.contains("lastTransformByCure")) {
            lastTransformByCure = tag.getBoolean("lastTransformByCure");
        } else {
            lastTransformByCure = false;
        }
        if (tag.contains("BeforeCursedMoonAppliedForm")) {
            BeforeCursedMoonAppliedForm = FormUtils.parseForm(Identifier.tryParse(tag.getString("BeforeCursedMoonAppliedForm")), null);
        } else {
            BeforeCursedMoonAppliedForm = null;
        }
        if (tag.contains("AfterCursedMoonAppliedForm")) {
            AfterCursedMoonAppliedForm = FormUtils.parseForm(Identifier.tryParse(tag.getString("AfterCursedMoonAppliedForm")), null);
        } else {
            AfterCursedMoonAppliedForm = null;
        }
        if (tag.contains("transformTargetForm")) {
            transformTargetForm = FormUtils.parseForm(Identifier.tryParse(tag.getString("transformTargetForm")), null);
        } else {
            transformTargetForm = null;
        }
        if (tag.contains("customPotionFormID")) {
            customPotionFormID = Identifier.tryParse(tag.getString("customPotionFormID"));
        } else {
            customPotionFormID = RegPlayerForms.ORIGINAL_BEFORE_ENABLE.getFormID();
        }
        if (tag.contains("instinctValue")) {
            instinctValue = tag.getFloat("instinctValue");
        } else {
            instinctValue = 0f;
        }
        if (tag.contains("instinctRate")) {
            instinctRate = tag.getFloat("instinctRate");
        } else {
            instinctRate = 0f;
        }
        if (tag.contains("instinctEffects")) {
            instinctEffects.clear();
            CompoundTag effects = tag.getCompound("instinctEffects");
            for (String key : effects.getAllKeys()) {
                instinctEffects.put(Identifier.tryParse(key), InstinctUtils.InstinctEffect.fromNBT(effects.getCompound(key)));
            }
        }
        if (player.level().isClientSide()) {
            InstinctUtils.fromInstinctUpdate(instinctValue, instinctRate);
        }
    }

    @Override
    public void writeToNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        if (nowFormID != null) {
            tag.putString("nowFormID", nowFormID.toString());
        } else {
            tag.putBoolean("no_form_id", true);
        }
        tag.putString("fallbackFormID", fallbackFormID.toString());
        ListTag history = new ListTag();
        for (IForm form : formHistory) {
            history.add(StringTag.valueOf(form.getFormID().toString()));
        }
        tag.put("formHistory", history);
        tag.putBoolean("isCursedMoonApplied", isCursedMoonApplied);
        tag.putBoolean("lastTransformByCure", lastTransformByCure);
        if (BeforeCursedMoonAppliedForm != null) {
            tag.putString("BeforeCursedMoonAppliedForm", BeforeCursedMoonAppliedForm.getFormID().toString());
        }
        if (AfterCursedMoonAppliedForm != null) {
            tag.putString("AfterCursedMoonAppliedForm", AfterCursedMoonAppliedForm.getFormID().toString());
        }
        if (transformTargetForm != null) {
            tag.putString("transformTargetForm", transformTargetForm.getFormID().toString());
        }
        if (customPotionFormID != null) {
            tag.putString("customPotionFormID", customPotionFormID.toString());
        }
        tag.putFloat("instinctValue", instinctValue);
        tag.putFloat("instinctRate", instinctRate);
        CompoundTag effects = new CompoundTag();
        for (Map.Entry<Identifier, InstinctUtils.InstinctEffect> entry : instinctEffects.entrySet()) {
            CompoundTag effect = new CompoundTag();
            entry.getValue().toNBT(effect);
            effects.put(entry.getKey().toString(), effect);
        }
        tag.put("instinctEffects", effects);
    }

    public void clear() {
        this.nowForm = InitialFormUtils.getInitialForm(this.player);
        this.nowFormID = nowForm.getFormID();
        formHistory.clear();
        isCursedMoonApplied = false;
        lastTransformByCure = false;
        BeforeCursedMoonAppliedForm = null;
        AfterCursedMoonAppliedForm = null;
        transformTargetForm = null;
        customPotionFormID = RegPlayerForms.ORIGINAL_BEFORE_ENABLE.getFormID();
        instinctValue = 0.0f;
        instinctRate = 0.0f;
        instinctEffects.clear();
    }

    public void sync() {
        COMPONENT.sync(this.player);
    }

    public void setForm(IForm form) {
        nowForm = form;
        nowFormID = form.getFormID();
    }

    public void setForm(Identifier formID) {
        nowForm = FormUtils.parseForm(formID, RegPlayerForms.ORIGINAL_BEFORE_ENABLE);
        nowFormID = formID;
    }
}