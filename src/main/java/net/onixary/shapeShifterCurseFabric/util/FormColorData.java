package net.onixary.shapeShifterCurseFabric.util;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.custom_ui.FormColorSelectMenu;
import net.onixary.shapeShifterCurseFabric.networking.ModPacketsS2C;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.skin.PlayerSkinComponent;
import net.onixary.shapeShifterCurseFabric.player_form.skin.RegPlayerSkinComponent;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class FormColorData {
    public boolean enableDefaultFormColor = true;
    public final HashMap<Identifier, FormTextureUtils.ColorSetting> formDefaultSetting = new HashMap<>();

    public final HashMap<String, FormTextureUtils.ColorSetting> customSetting = new HashMap<>();
    public final HashMap<Identifier, HashMap<String, FormTextureUtils.ColorSetting>> customSettingByForm = new HashMap<>();

    public static int GlobalSlotCount = 9;
    public static int LocalSlotCount = 3;

    public final HashMap<Identifier, List<String>> FormColorSelectMenu_Form_Local_Names = new HashMap<>();
    public final List<String> FormColorSelectMenu_Global_Names = new ArrayList<String>();
    public final HashMap<Identifier, String> FormColorSelectMenu_Form_Default_Names = new HashMap<>();

    public final List<Identifier> unlockedForms = new ArrayList<Identifier>();

    // V2 UI用的数据 由于UI没设计完 部分值不确定
    public static int V2_GlobalSlotCount = 9;
    public final List<String> V2_FormColorSelectMenu_Global_Names = new ArrayList<String>();

    public CompoundTag dumpColorSetting(FormTextureUtils.ColorSetting colorSetting) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("primaryColor", colorSetting.getPrimaryColor());
        nbt.putInt("accentColor1", colorSetting.getAccentColor1());
        nbt.putInt("accentColor2", colorSetting.getAccentColor2());
        nbt.putInt("eyeColorA", colorSetting.getEyeColorA());
        nbt.putInt("eyeColorB", colorSetting.getEyeColorB());
        nbt.putBoolean("primaryGreyReverse", colorSetting.getPrimaryGreyReverse());
        nbt.putBoolean("accent1GreyReverse", colorSetting.getAccent1GreyReverse());
        nbt.putBoolean("accent2GreyReverse", colorSetting.getAccent2GreyReverse());
        return nbt;
    }

    public FormTextureUtils.ColorSetting loadColorSetting(CompoundTag nbt) {
        return new FormTextureUtils.ColorSetting(nbt.getIntOr("primaryColor", 0), nbt.getIntOr("accentColor1", 0), nbt.getIntOr("accentColor2", 0), nbt.getIntOr("eyeColorA", 0), nbt.getIntOr("eyeColorB", 0), nbt.getBooleanOr("primaryGreyReverse", false), nbt.getBooleanOr("accent1GreyReverse", false), nbt.getBooleanOr("accent2GreyReverse", false));
    }

    public CompoundTag saveCompound() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("enableDefaultFormColor", enableDefaultFormColor);
        CompoundTag formDefaultSettingNbt = new CompoundTag();
        for (Identifier form : formDefaultSetting.keySet()) {
            formDefaultSettingNbt.put(form.toString(), dumpColorSetting(formDefaultSetting.get(form)));
        }
        nbt.put("formDefaultSetting", formDefaultSettingNbt);
        CompoundTag customSettingNbt = new CompoundTag();
        for (String name : customSetting.keySet()) {
            customSettingNbt.put(name, dumpColorSetting(customSetting.get(name)));
        }
        nbt.put("customSetting", customSettingNbt);
        CompoundTag customSettingByFormNbt = new CompoundTag();
        for (Identifier form : customSettingByForm.keySet()) {
            CompoundTag formNbt = new CompoundTag();
            for (String name : customSettingByForm.get(form).keySet()) {
                formNbt.put(name, dumpColorSetting(customSettingByForm.get(form).get(name)));
            }
            customSettingByFormNbt.put(form.toString(), formNbt);
        }
        nbt.put("customSettingByForm", customSettingByFormNbt);
        CompoundTag formColorSelectMenuNbt = new CompoundTag();
        for (Identifier form : FormColorSelectMenu_Form_Local_Names.keySet()) {
            ListTag nbtList = new ListTag();
            for (String name : FormColorSelectMenu_Form_Local_Names.get(form)) {
                nbtList.add(StringTag.valueOf(name));
            }
            formColorSelectMenuNbt.put(form.toString(), nbtList);
        }
        nbt.put("FCS_form_local_setting_names", formColorSelectMenuNbt);
        ListTag nbtList = new ListTag();
        for (String name : FormColorSelectMenu_Global_Names) {
            nbtList.add(StringTag.valueOf(name));
        }
        nbt.put("FCS_global_setting_names", nbtList);
        CompoundTag formColorSelectMenuDefaultNbt = new CompoundTag();
        for (Identifier form : FormColorSelectMenu_Form_Default_Names.keySet()) {
            formColorSelectMenuDefaultNbt.putString(form.toString(), FormColorSelectMenu_Form_Default_Names.get(form));
        }
        nbt.put("FCS_form_default_setting_names", formColorSelectMenuDefaultNbt);
        ListTag nbtList2 = new ListTag();
        for (String name : V2_FormColorSelectMenu_Global_Names) {
            nbtList2.add(StringTag.valueOf(name));
        }
        nbt.put("V2_FCS_global_setting_names", nbtList2);
        ListTag nbtList3 = new ListTag();
        for (Identifier form : unlockedForms) {
            nbtList3.add(StringTag.valueOf(form.toString()));
        }
        nbt.put("unlockedForms", nbtList3);
        return nbt;
    }

    public void loadCompound(CompoundTag compound) {
        formDefaultSetting.clear();
        customSetting.clear();
        customSettingByForm.clear();
        FormColorSelectMenu_Form_Local_Names.clear();
        FormColorSelectMenu_Global_Names.clear();
        unlockedForms.clear();
        if (compound.contains("enableDefaultFormColor")) {
            enableDefaultFormColor = compound.getBooleanOr("enableDefaultFormColor", false);
        }
        if (compound.contains("formDefaultSetting")) {
            CompoundTag formDefaultSettingNbt = compound.getCompoundOrEmpty("formDefaultSetting");
            for (String form : formDefaultSettingNbt.keySet()) {
                try {
                    formDefaultSetting.put(Identifier.tryParse(form), loadColorSetting(formDefaultSettingNbt.getCompoundOrEmpty(form)));
                } catch (Exception e) {
                    ShapeShifterCurseFabric.LOGGER.warn("Failed to load form default color setting for " + form + ": " + e.getMessage());
                }
            }
        }
        if (compound.contains("customSetting")) {
            CompoundTag customSettingNbt = compound.getCompoundOrEmpty("customSetting");
            for (String name : customSettingNbt.keySet()) {
                try {
                    customSetting.put(name, loadColorSetting(customSettingNbt.getCompoundOrEmpty(name)));
                } catch (Exception e) {
                    ShapeShifterCurseFabric.LOGGER.warn("Failed to load custom color setting for " + name + ": " + e.getMessage());
                }
            }
        }
        if (compound.contains("customSettingByForm")) {
            CompoundTag customSettingByFormNbt = compound.getCompoundOrEmpty("customSettingByForm");
            for (String form : customSettingByFormNbt.keySet()) {
                Identifier formId = Identifier.tryParse(form);
                CompoundTag formNbt = customSettingByFormNbt.getCompoundOrEmpty(form);
                for (String name : formNbt.keySet()) {
                    try {
                        customSettingByForm.computeIfAbsent(formId, k -> new HashMap<>()).put(name, loadColorSetting(formNbt.getCompoundOrEmpty(name)));
                    } catch (Exception e) {
                        ShapeShifterCurseFabric.LOGGER.warn("Failed to load custom color setting for " + name + " on form " + form + ": " + e.getMessage());
                    }
                }
            }
        }
        if (compound.contains("FCS_form_local_setting_names")) {
            CompoundTag nbtList = compound.getCompoundOrEmpty("FCS_form_local_setting_names");
            for (String form : nbtList.keySet()) {
                Identifier formId = Identifier.tryParse(form);
                List<String> formSlotNames = FormColorSelectMenu_Form_Local_Names.computeIfAbsent(formId, k -> new ArrayList<>());
                ListTag nbtList2 = nbtList.getListOrEmpty(form);
                for (int i = 0; i < nbtList2.size(); i++) {
                    formSlotNames.add(nbtList2.getStringOr(i, ""));
                }
            }
        }
        if (compound.contains("FCS_global_setting_names")) {
            ListTag nbtList = compound.getListOrEmpty("FCS_global_setting_names");
            for (int i = 0; i < nbtList.size(); i++) {
                FormColorSelectMenu_Global_Names.add(nbtList.getStringOr(i, ""));
            }
        }
        if (compound.contains("FCS_form_default_setting_names")) {
            CompoundTag nbtList = compound.getCompoundOrEmpty("FCS_form_default_setting_names");
            for (String form : nbtList.keySet()) {
                Identifier formId = Identifier.tryParse(form);
                FormColorSelectMenu_Form_Default_Names.put(formId, nbtList.getStringOr(form, ""));
            }
        }
        if (compound.contains("V2_FCS_global_setting_names")) {
            ListTag nbtList = compound.getListOrEmpty("V2_FCS_global_setting_names");
            for (int i = 0; i < nbtList.size(); i++) {
                V2_FormColorSelectMenu_Global_Names.add(nbtList.getStringOr(i, ""));
            }
        }
        if (compound.contains("unlockedForms")) {
            ListTag nbtList = compound.getListOrEmpty("unlockedForms");
            for (int i = 0; i < nbtList.size(); i++) {
                unlockedForms.add(Identifier.tryParse(nbtList.getStringOr(i, "")));
            }
        }
    }

    public boolean isUnlock(Identifier form) {
        return unlockedForms.contains(form);
    }

    public void unlockForm(Identifier form) {
        if (unlockedForms.contains(form)) {
            return;
        }
        unlockedForms.add(form);
        this.writeToConfig();
    }

    public void unlockAll() {
        for (IForm form : RegPlayerForms.playerForms.values()) {
            if (!unlockedForms.contains(form.getFormID())) {
                unlockedForms.add(form.getFormID());
            }
        }
        this.writeToConfig();
    }

    public void clearFormUnlock() {
        unlockedForms.clear();
        unlockedForms.add(RegPlayerForms.ORIGINAL_BEFORE_ENABLE.getFormID());
        this.writeToConfig();
    }

    public static List<Consumer<Identifier>> onFormChangeListeners = new ArrayList<>();

    // 移除V1后记得删
    static {
        onFormChangeListeners.add((form) -> {
            FormColorSelectMenu.onFormChange_STATIC(true, true);
        });
    }

    // 挂一个钩子在网络接受形态上 比如客户端的SYNC_FORM_CHANGE接收函数上
    public void onClientFormChange(Identifier form) {
        if (this.enableDefaultFormColor && ShapeShifterCurseFabric.playerCustomConfig.enable_form_default_color_system && this.formDefaultSetting.containsKey(form)) {
            ModPacketsS2C.sendUpdateCustomColor(this.formDefaultSetting.get(form), false, false,false, false);
        }
        this.unlockForm(form);
        // 延时一下 好同步 "sendUpdateCustomSetting" 的更新
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                onFormChangeListeners.forEach(listener -> listener.accept(form));
            } catch (InterruptedException ignored) {
                onFormChangeListeners.forEach(listener -> listener.accept(form));
            }
        }).start();
    }

    public Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("shape-shifter-curse-form-color-data.nbt");
    }

    // 每次修改后调用
    public void writeToConfig() {
        Path configPath = getConfigPath();
        try {
            NbtIo.writeCompressed(this.saveCompound(), configPath.toFile().toPath());
        } catch (IOException e) {
            ShapeShifterCurseFabric.LOGGER.error("Failed to write form color data to config file: " + e);
        }
    }

    public void loadFormConfig() {
        Path configPath = getConfigPath();
        if (Files.exists(configPath)) {
            try {
                CompoundTag compound = NbtIo.readCompressed(configPath, NbtAccounter.unlimitedHeap());
                this.loadCompound(compound);
            } catch (IOException e) {
                ShapeShifterCurseFabric.LOGGER.error("Failed to load form color data from config file: " + e);
            }
        }
    }

    public static byte[] ColorSettingToBytes(FormTextureUtils.ColorSetting colorSetting) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeInt(1);
            dos.writeInt(colorSetting.getPrimaryColor());
            dos.writeInt(colorSetting.getAccentColor1());
            dos.writeInt(colorSetting.getAccentColor2());
            dos.writeInt(colorSetting.getEyeColorA());
            dos.writeInt(colorSetting.getEyeColorB());
            byte bools = 0;
            bools |= (byte) (colorSetting.getPrimaryGreyReverse() ? 1 : 0);
            bools |= (byte) (colorSetting.getAccent1GreyReverse() ? 2 : 0);
            bools |= (byte) (colorSetting.getAccent2GreyReverse() ? 4 : 0);
            dos.writeByte(bools);
            dos.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    public static @Nullable FormTextureUtils.ColorSetting ColorSettingFromBytes(byte[] bytes) {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes))) {
            if (dis.readInt() != 1) {
                return null;
            }
            int primaryColor = dis.readInt();
            int accentColor1 = dis.readInt();
            int accentColor2 = dis.readInt();
            int eyeColorA = dis.readInt();
            int eyeColorB = dis.readInt();
            byte bools = dis.readByte();
            boolean primaryGreyReverse = (bools & 1) != 0;
            boolean accent1GreyReverse = (bools & 2) != 0;
            boolean accent2GreyReverse = (bools & 4) != 0;
            return new FormTextureUtils.ColorSetting(primaryColor, accentColor1, accentColor2,
                    eyeColorA, eyeColorB, primaryGreyReverse, accent1GreyReverse, accent2GreyReverse);
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] formHex(String hex) {
        if (hex == null || hex.isEmpty() || hex.length() % 2 != 0) {
            return null;
        }
        int len = hex.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            String byteStr = hex.substring(i * 2, i * 2 + 2);
            try {
                int val = Integer.parseInt(byteStr, 16);
                result[i] = (byte) val;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return result;
    }

    public static String toHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            stringBuilder.append(String.format("%02X", b & 0xFF));
        }
        return stringBuilder.toString();
    }

    public static @Nullable FormTextureUtils.ColorSetting ColorSettingFormString(String data) {
        try {
            if (data.startsWith("b")) {
                byte[] bytes = Base64.getDecoder().decode(data.substring(1));
                return ColorSettingFromBytes(bytes);
            } else if (data.startsWith("#")) {
                String hex = data.substring(1);
                byte[] bytes = formHex(hex);
                return ColorSettingFromBytes(bytes);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static String ColorSettingtoString(FormTextureUtils.ColorSetting data, boolean useBase64) {
        if (useBase64) {
            byte[] bytes = FormColorData.ColorSettingToBytes(data);
            return "b" + Base64.getEncoder().encodeToString(bytes);
        } else {
            return "#" + toHex(FormColorData.ColorSettingToBytes(data));
        }
    }

    public String getName_LocalFormSlot(Identifier formID, int index) {
        List<String> list = this.FormColorSelectMenu_Form_Local_Names.get(formID);
        if (list != null && index < list.size()) {
            return list.get(index);
        }
        return "";
    }

    public void setName_LocalFormSlot(Identifier formID, int index, String name) {
        if (index > LocalSlotCount) {
            return;
        }
        List<String> list = this.FormColorSelectMenu_Form_Local_Names.computeIfAbsent(formID, k -> new ArrayList<>());
        if (index >= list.size()) {
            for (int i = list.size(); i <= index; i++) {
                list.add("");
            }
        }
        list.set(index, name);
    }

    public String getName_GlobalSlot(int index) {
        if (index < FormColorSelectMenu_Global_Names.size()) {
            return FormColorSelectMenu_Global_Names.get(index);
        }
        return "";
    }

    public void setName_GlobalSlot(int index, String name) {
        if (index > GlobalSlotCount) {
            return;
        }
        if (index >= FormColorSelectMenu_Global_Names.size()) {
            for (int i = FormColorSelectMenu_Global_Names.size(); i <= index; i++) {
                FormColorSelectMenu_Global_Names.add("");
            }
        }
        FormColorSelectMenu_Global_Names.set(index, name);
    }

    // V2的API
    public String V2_getName_GlobalSlot(int index) {
        if (index < V2_FormColorSelectMenu_Global_Names.size()) {
            return V2_FormColorSelectMenu_Global_Names.get(index);
        }
        return "";
    }

    public void V2_setName_GlobalSlot(int index, String name) {
        if (index > V2_GlobalSlotCount) {
            return;
        }
        if (index >= V2_FormColorSelectMenu_Global_Names.size()) {
            for (int i = V2_FormColorSelectMenu_Global_Names.size(); i <= index; i++) {
                V2_FormColorSelectMenu_Global_Names.add("");
            }
        }
        V2_FormColorSelectMenu_Global_Names.set(index, name);
    }

    public String getName_DefaultSlot(Identifier formID) {
        return this.FormColorSelectMenu_Form_Default_Names.getOrDefault(formID, "");
    }

    public void setName_DefaultSlot(Identifier formID, String name) {
        this.FormColorSelectMenu_Form_Default_Names.put(formID, name);
    }

    public static @Nullable FormTextureUtils.ColorSetting getPlayerColorSetting(boolean ABGR) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return null;
        }
        PlayerSkinComponent skinComponent = RegPlayerSkinComponent.SKIN_SETTINGS.get(player);
        if (ABGR) {
            return skinComponent.getFormColor();
        } else {
            FormTextureUtils.ColorSetting colorSetting = skinComponent.getFormColor();
            return new FormTextureUtils.ColorSetting(
                    FormTextureUtils.ABGR2ARGB(colorSetting.getPrimaryColor()),
                    FormTextureUtils.ABGR2ARGB(colorSetting.getAccentColor1()),
                    FormTextureUtils.ABGR2ARGB(colorSetting.getAccentColor2()),
                    FormTextureUtils.ABGR2ARGB(colorSetting.getEyeColorA()),
                    FormTextureUtils.ABGR2ARGB(colorSetting.getEyeColorB()),
                    colorSetting.getPrimaryGreyReverse(),
                    colorSetting.getAccent1GreyReverse(),
                    colorSetting.getAccent2GreyReverse()
            );
        }
    }

    public static FormTextureUtils.ColorSetting ARGB2ABGR(FormTextureUtils.ColorSetting colorSetting) {
        return new FormTextureUtils.ColorSetting(
                FormTextureUtils.ARGB2ABGR(colorSetting.getPrimaryColor()),
                FormTextureUtils.ARGB2ABGR(colorSetting.getAccentColor1()),
                FormTextureUtils.ARGB2ABGR(colorSetting.getAccentColor2()),
                FormTextureUtils.ARGB2ABGR(colorSetting.getEyeColorA()),
                FormTextureUtils.ARGB2ABGR(colorSetting.getEyeColorB()),
                colorSetting.getPrimaryGreyReverse(),
                colorSetting.getAccent1GreyReverse(),
                colorSetting.getAccent2GreyReverse()
        );
    }

    public static FormTextureUtils.ColorSetting ABGR2ARGB(FormTextureUtils.ColorSetting colorSetting) {
        return new FormTextureUtils.ColorSetting(
                FormTextureUtils.ABGR2ARGB(colorSetting.getPrimaryColor()),
                FormTextureUtils.ABGR2ARGB(colorSetting.getAccentColor1()),
                FormTextureUtils.ABGR2ARGB(colorSetting.getAccentColor2()),
                FormTextureUtils.ABGR2ARGB(colorSetting.getEyeColorA()),
                FormTextureUtils.ABGR2ARGB(colorSetting.getEyeColorB()),
                colorSetting.getPrimaryGreyReverse(),
                colorSetting.getAccent1GreyReverse(),
                colorSetting.getAccent2GreyReverse()
        );
    }

    public static Component toCopyableText(String text, String copyText) {
        return Component.literal(text).withStyle(style -> style.withClickEvent(new ClickEvent.CopyToClipboard(copyText)));
    }

    public static Component appendCopyableText(Component text, String copyText) {
        return text.copy().withStyle(style -> style.withClickEvent(
                new ClickEvent.CopyToClipboard(copyText)
        ));
    }
}