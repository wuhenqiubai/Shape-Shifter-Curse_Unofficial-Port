package net.onixary.shapeShifterCurseFabric.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.items.accessory.AccessoryItem;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;
import net.onixary.shapeShifterCurseFabric.util.Accessory.AccessoryUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TrinketUtils {
    public interface CustomPowerTrinketInterface {
        void onFormChange(ItemStack stack, AccessoryItem.SlotData slot, Player entity);
    }

    public static class TrinketPowerData {
        public final List<ResourceLocation> accessoryPowers;
        public final List<ResourceLocation> allFormPowerAdd;
        public final List<ResourceLocation> allFormPowerRemove;
        public final HashMap<ResourceLocation, List<ResourceLocation>> formPowerAdd;
        public final HashMap<ResourceLocation, List<ResourceLocation>> formPowerRemove;
        public final HashMap<ResourceLocation, HashMap<ResourceLocation, List<ResourceLocation>>> layerPowerAddMap;
        public final HashMap<ResourceLocation, HashMap<ResourceLocation, List<ResourceLocation>>> layerPowerRemoveMap;

        private Tuple<List<ResourceLocation>, List<ResourceLocation>> parsePowerList(JsonObject jsonObject) {
            List<ResourceLocation> allFormPowerAdd = new ArrayList<>();
            List<ResourceLocation> allFormPowerRemove = new ArrayList<>();
            if (jsonObject.has("add")) {
                jsonObject.get("add").getAsJsonArray().forEach(jsonElement -> {
                    ResourceLocation powerID = ResourceLocation.tryParse(jsonElement.getAsString());
                    if (powerID != null) {
                        allFormPowerAdd.add(powerID);
                    }
                });
            }
            if (jsonObject.has("remove")) {
                jsonObject.get("remove").getAsJsonArray().forEach(jsonElement -> {
                    ResourceLocation powerID = ResourceLocation.tryParse(jsonElement.getAsString());
                    if (powerID != null) {
                        allFormPowerRemove.add(powerID);
                    }
                });
            }
            return new Tuple<>(allFormPowerAdd, allFormPowerRemove);
        }

        public TrinketPowerData(JsonObject jsonObject) {
            if (jsonObject == null) {
                this.accessoryPowers = new ArrayList<>();
                this.allFormPowerAdd = new ArrayList<>();
                this.allFormPowerRemove = new ArrayList<>();
                this.formPowerAdd = new HashMap<>();
                this.formPowerRemove = new HashMap<>();
                this.layerPowerAddMap = new HashMap<>();
                this.layerPowerRemoveMap = new HashMap<>();
                return;
            }
            List<ResourceLocation> accessoryPowers = new ArrayList<>();
            List<ResourceLocation> allFormPowerAdd = new ArrayList<>();
            List<ResourceLocation> allFormPowerRemove = new ArrayList<>();
            HashMap<ResourceLocation, List<ResourceLocation>> formPowerAdd = new HashMap<>();
            HashMap<ResourceLocation, List<ResourceLocation>> formPowerRemove = new HashMap<>();
            HashMap<ResourceLocation, HashMap<ResourceLocation, List<ResourceLocation>>> layerPowerAddMap = new HashMap<>();
            HashMap<ResourceLocation, HashMap<ResourceLocation, List<ResourceLocation>>> layerPowerRemoveMap = new HashMap<>();
            if (jsonObject.has("accessory_powers") && jsonObject.get("accessory_powers").isJsonArray()) {
                JsonArray accessoryPowerArray = jsonObject.get("accessory_powers").getAsJsonArray();
                accessoryPowerArray.forEach(jsonElement -> {
                    ResourceLocation powerID = ResourceLocation.tryParse(jsonElement.getAsString());
                    if (powerID != null) {
                        accessoryPowers.add(powerID);
                    }
                });
            }
            if (jsonObject.has("all_form") && jsonObject.get("all_form").isJsonObject()) {
                Tuple<List<ResourceLocation>, List<ResourceLocation>> allFormPowerList = parsePowerList(jsonObject.get("all_form").getAsJsonObject());
                allFormPowerAdd = allFormPowerList.getA();
                allFormPowerRemove = allFormPowerList.getB();
            }
            if (jsonObject.has("forms") && jsonObject.get("forms").isJsonObject()) {
                JsonObject formData = jsonObject.get("forms").getAsJsonObject();
                for (String formID : formData.keySet()) {
                    ResourceLocation currentFormID = ResourceLocation.tryParse(formID);
                    if (currentFormID == null) {
                        ShapeShifterCurseFabric.LOGGER.warn("Error On Parsing Trinket Power Data: Invalid Form ID: {}", formID);
                        continue;
                    }
                    JsonObject formPowerData = formData.get(formID).getAsJsonObject();
                    Tuple<List<ResourceLocation>, List<ResourceLocation>> formPowerList = parsePowerList(formPowerData);
                    formPowerAdd.put(currentFormID, formPowerList.getA());
                    formPowerRemove.put(currentFormID, formPowerList.getB());
                }
            }
            if (jsonObject.has("layers") && jsonObject.get("layers").isJsonObject()) {
                JsonObject layerGroupData = jsonObject.get("layers").getAsJsonObject();
                for (String layerGroupID : layerGroupData.keySet()) {
                    ResourceLocation currentLayerGroupID = ResourceLocation.tryParse(layerGroupID);
                    if (currentLayerGroupID == null) {
                        ShapeShifterCurseFabric.LOGGER.warn("Error On Parsing Trinket Power Data: Invalid Layer Group ID: {}", layerGroupID);
                    }
                    HashMap<ResourceLocation, List<ResourceLocation>> layerPowerAddMap2 = new HashMap<>();
                    HashMap<ResourceLocation, List<ResourceLocation>> layerPowerRemoveMap2 = new HashMap<>();
                    JsonObject layerData = layerGroupData.get(layerGroupID).getAsJsonObject();
                    for (String layerID : layerData.keySet()) {
                        ResourceLocation currentLayerID = ResourceLocation.tryParse(layerID);
                        if (currentLayerID == null) {
                            ShapeShifterCurseFabric.LOGGER.warn("Error On Parsing Trinket Power Data: Invalid Layer ID: {}", layerID);
                            continue;
                        }
                        JsonObject layerPowerData = layerData.get(layerID).getAsJsonObject();
                        Tuple<List<ResourceLocation>, List<ResourceLocation>> layerPowerList = parsePowerList(layerPowerData);
                        layerPowerAddMap2.put(currentLayerID, layerPowerList.getA());
                        layerPowerRemoveMap2.put(currentLayerID, layerPowerList.getB());
                    }
                    layerPowerAddMap.put(currentLayerGroupID, layerPowerAddMap2);
                    layerPowerRemoveMap.put(currentLayerGroupID, layerPowerRemoveMap2);
                }
            }

            this.accessoryPowers = accessoryPowers;
            this.allFormPowerAdd = allFormPowerAdd;
            this.allFormPowerRemove = allFormPowerRemove;
            this.formPowerAdd = formPowerAdd;
            this.formPowerRemove = formPowerRemove;
            this.layerPowerAddMap = layerPowerAddMap;
            this.layerPowerRemoveMap = layerPowerRemoveMap;
        }

        public TrinketPowerData Merge(TrinketPowerData other) {
            for (ResourceLocation powerID : other.accessoryPowers) {
                if (!accessoryPowers.contains(powerID)) {
                    accessoryPowers.add(powerID);
                }
            }
            for (ResourceLocation powerID : other.allFormPowerAdd) {
                if (!allFormPowerAdd.contains(powerID)) {
                    allFormPowerAdd.add(powerID);
                }
            }
            for (ResourceLocation powerID : other.allFormPowerRemove) {
                if (!allFormPowerRemove.contains(powerID)) {
                    allFormPowerRemove.add(powerID);
                }
            }
            for (ResourceLocation formID : other.formPowerAdd.keySet()) {
                List<ResourceLocation> selfAddPowerList = formPowerAdd.computeIfAbsent(formID, k -> new ArrayList<>());
                List<ResourceLocation> addPowerList = other.formPowerAdd.get(formID);
                for (ResourceLocation powerID : addPowerList) {
                    if (!selfAddPowerList.contains(powerID)) {
                        selfAddPowerList.add(powerID);
                    }
                }
            }
            for (ResourceLocation formID : other.formPowerRemove.keySet()) {
                List<ResourceLocation> selfRemovePowerList = this.formPowerRemove.computeIfAbsent(formID, k -> new ArrayList<>());
                List<ResourceLocation> removePowerList = other.formPowerRemove.get(formID);
                for (ResourceLocation powerID : removePowerList) {
                    if (!selfRemovePowerList.contains(powerID)) {
                        selfRemovePowerList.add(powerID);
                    }
                }
            }
            for (ResourceLocation layerGroupID : other.layerPowerAddMap.keySet()) {
                HashMap<ResourceLocation, List<ResourceLocation>> selfLayerGroupPowerAddMap = this.layerPowerAddMap.computeIfAbsent(layerGroupID, k -> new HashMap<>());
                HashMap<ResourceLocation, List<ResourceLocation>> layerPowerAddMap = other.layerPowerAddMap.get(layerGroupID);
                for (ResourceLocation layerID : layerPowerAddMap.keySet()) {
                    List<ResourceLocation> selfLayerPowerAddList = selfLayerGroupPowerAddMap.computeIfAbsent(layerID, k -> new ArrayList<>());
                    List<ResourceLocation> layerPowerAddList = layerPowerAddMap.get(layerID);
                    for (ResourceLocation powerID : layerPowerAddList) {
                        if (!selfLayerPowerAddList.contains(powerID)) {
                            selfLayerPowerAddList.add(powerID);
                        }
                    }
                }
            }
            for (ResourceLocation layerGroupID : other.layerPowerRemoveMap.keySet()) {
                HashMap<ResourceLocation, List<ResourceLocation>> selfLayerGroupPowerRemoveMap = this.layerPowerRemoveMap.computeIfAbsent(layerGroupID, k -> new HashMap<>());
                HashMap<ResourceLocation, List<ResourceLocation>> layerPowerRemoveMap = other.layerPowerRemoveMap.get(layerGroupID);
                for (ResourceLocation layerID : layerPowerRemoveMap.keySet()) {
                    List<ResourceLocation> selfLayerPowerRemoveList = selfLayerGroupPowerRemoveMap.computeIfAbsent(layerID, k -> new ArrayList<>());
                    List<ResourceLocation> layerPowerRemoveList = layerPowerRemoveMap.get(layerID);
                    for (ResourceLocation powerID : layerPowerRemoveList) {
                        if (!selfLayerPowerRemoveList.contains(powerID)) {
                            selfLayerPowerRemoveList.add(powerID);
                        }
                    }
                }
            }
            return this;
        }

        private void AddPower(Player player, ResourceLocation powerID, ResourceLocation sourceID) {
            PowerType<?> powerType = PowerTypeRegistry.get(powerID);
            if (powerType != null) {
                PowerHolderComponent powerHolder = PowerHolderComponent.KEY.get(player);
                powerHolder.addPower(powerType, sourceID);
            }
        }

        private void RemovePower(Player player, ResourceLocation powerID, ResourceLocation sourceID) {
            PowerType<?> powerType = PowerTypeRegistry.get(powerID);
            if (powerType != null) {
                PowerHolderComponent powerHolder = PowerHolderComponent.KEY.get(player);
                powerHolder.removePower(powerType, sourceID);
            }
        }

        public void onPlayerFormChangeReApply(Player player) {
            IForm form = FormUtils.getPlayerForm(player);
            Tuple<ResourceLocation, ResourceLocation> currentFormLayer = form.getFormLayer();
            ResourceLocation currentFormID = form.getFormID();
            ResourceLocation currentOriginsID = currentFormLayer.getB();
            for (ResourceLocation powerID : allFormPowerAdd) {
                this.AddPower(player, powerID, currentOriginsID);
            }
            for (ResourceLocation powerID : allFormPowerRemove) {
                this.RemovePower(player, powerID, currentOriginsID);
            }
            List<ResourceLocation> formPowerAddList = formPowerAdd.get(currentFormID);
            List<ResourceLocation> formPowerRemoveList = formPowerRemove.get(currentFormID);
            List<ResourceLocation> layerGroupPowerAddList = layerPowerAddMap.getOrDefault(currentFormLayer.getA(), new HashMap<>()).get(currentFormLayer.getB());
            List<ResourceLocation> layerGroupPowerRemoveList = layerPowerRemoveMap.getOrDefault(currentFormLayer.getA(), new HashMap<>()).get(currentFormLayer.getB());
            if (formPowerAddList != null) {
                for (ResourceLocation powerID : formPowerAddList) {
                    this.AddPower(player, powerID, currentOriginsID);
                }
            }
            if (layerGroupPowerAddList != null) {
                for (ResourceLocation powerID : layerGroupPowerAddList) {
                    this.AddPower(player, powerID, currentOriginsID);
                }
            }
            if (formPowerRemoveList != null) {
                for (ResourceLocation powerID : formPowerRemoveList) {
                    this.RemovePower(player, powerID, currentOriginsID);
                }
            }
            if (layerGroupPowerRemoveList != null) {
                for (ResourceLocation powerID : layerGroupPowerRemoveList) {
                    this.RemovePower(player, powerID, currentOriginsID);
                }
            }
        }

        public void onPlayerEquip(Player player, ResourceLocation itemID) {
            for (ResourceLocation powerID : accessoryPowers) {
                this.AddPower(player, powerID, itemID);
            }
            this.onPlayerFormChangeReApply(player);
        }

        public void onPlayerUnEquip(Player player, ResourceLocation itemID) {
            IForm form = FormUtils.getPlayerForm(player);
            Tuple<ResourceLocation, ResourceLocation> currentFormLayer = form.getFormLayer();
            ResourceLocation currentFormID = form.getFormID();
            ResourceLocation currentOriginsID = currentFormLayer.getB();
            for (ResourceLocation powerID : accessoryPowers) {
                this.RemovePower(player, powerID, itemID);
            }
            for (ResourceLocation powerID : allFormPowerAdd) {
                this.RemovePower(player, powerID, currentOriginsID);
            }
            for (ResourceLocation powerID : allFormPowerRemove) {
                this.AddPower(player, powerID, currentOriginsID);
            }
            List<ResourceLocation> formPowerAddList = formPowerAdd.get(currentFormID);
            List<ResourceLocation> formPowerRemoveList = formPowerRemove.get(currentFormID);
            List<ResourceLocation> layerGroupPowerAddList = layerPowerAddMap.getOrDefault(currentFormLayer.getA(), new HashMap<>()).get(currentFormLayer.getB());
            List<ResourceLocation> layerGroupPowerRemoveList = layerPowerRemoveMap.getOrDefault(currentFormLayer.getA(), new HashMap<>()).get(currentFormLayer.getB());
            if (formPowerAddList != null) {
                for (ResourceLocation powerID : formPowerAddList) {
                    this.RemovePower(player, powerID, currentOriginsID);
                }
            }
            if (layerGroupPowerAddList != null) {
                for (ResourceLocation powerID : layerGroupPowerAddList) {
                    this.RemovePower(player, powerID, currentOriginsID);
                }
            }
            if (formPowerRemoveList != null) {
                for (ResourceLocation powerID : formPowerRemoveList) {
                    this.AddPower(player, powerID, currentOriginsID);
                }
            }
            if (layerGroupPowerRemoveList != null) {
                for (ResourceLocation powerID : layerGroupPowerRemoveList) {
                    this.AddPower(player, powerID, currentOriginsID);
                }
            }
        }
    }

    public static final HashMap<ResourceLocation, TrinketPowerData> accessoryPowerRegistry = new HashMap<>();
    private static final HashMap<ResourceLocation, Boolean> accessoryMixinAutoRegistry = new HashMap<>();

    public static void registerAccessoryPower(ResourceLocation itemIdentifier, TrinketPowerData powerData) {
        if (accessoryPowerRegistry.containsKey(itemIdentifier)) {
            accessoryPowerRegistry.get(itemIdentifier).Merge(powerData);
        } else {
            accessoryPowerRegistry.put(itemIdentifier, powerData);
        }
    }

    public static void clearAccessoryPower() {
        accessoryPowerRegistry.clear();
    }

    public static void registerAccessoryMixinAuto(ResourceLocation itemIdentifier, boolean auto) {
        accessoryMixinAutoRegistry.put(itemIdentifier, auto);
    }

    public static @Nullable TrinketPowerData getAccessoryPower(ResourceLocation itemIdentifier) {
        return accessoryPowerRegistry.get(itemIdentifier);
    }

    public static boolean getAccessoryMixinAuto(ResourceLocation itemIdentifier) {
        return accessoryMixinAutoRegistry.getOrDefault(itemIdentifier, true);
    }

    public static void ApplyAccessoryPowerOnPlayerFormChange(Player player, ResourceLocation accessoryID) {
        if (player.level().isClientSide) {
            return;  // 仅在服务器端执行
        }
        TrinketPowerData powerData = getAccessoryPower(accessoryID);
        if (powerData == null) {
            return;
        }
        powerData.onPlayerFormChangeReApply(player);
    }

    public static List<Tuple<AccessoryItem.SlotData, ItemStack>> getAllAccessory(Player player) {
        List<Tuple<AccessoryItem.SlotData, ItemStack>> allAccessory = new ArrayList<>();

        for (Map.Entry<String, AccessoryUtils.AccessoryIO> accessoryReg : AccessoryUtils.activeAccessoryModInterfaces.entrySet()) {
            String ioName = accessoryReg.getKey();
            AccessoryUtils.AccessoryIO accessoryIO = accessoryReg.getValue();
            @Nullable Map<Tuple<@Nullable String, String>, List<ItemStack>> allSlots = accessoryIO.getEntitySlots(player);
            if (allSlots != null) {
                for (Map.Entry<Tuple<@Nullable String, String>, List<ItemStack>> entry : allSlots.entrySet()) {
                    Tuple<@Nullable String, String> slotPair = entry.getKey();
                    List<ItemStack> stacks = entry.getValue();
                    int Index = 0;
                    for (ItemStack stack : stacks) {
                        if (stack.getItem() instanceof AccessoryItem && accessoryIO != AccessoryUtils.nowAccessoryMod) {
                            continue;
                        }
                        AccessoryItem.SlotData data = null;
                        if (slotPair.getA() == null) {
                            data = new AccessoryItem.SlotData(ResourceLocation.fromNamespaceAndPath(ioName, slotPair.getB()), Index);
                        } else {
                            data = new AccessoryItem.SlotData(ResourceLocation.fromNamespaceAndPath(ioName, "%s/%s".formatted(slotPair.getA(), slotPair.getB())), Index);
                        }
                        allAccessory.add(new Tuple<>(data, stack));
                        Index++;
                    }
                }
            }
        }
        return allAccessory;
    }

    // 适配新架构的函数 等完工后再改 现在为了不影响正在使用的功能 先注释掉
    public static void ReApplyAccessoryPowerOnPlayerFormChange(Player player) {
        List<Tuple<AccessoryItem.SlotData, ItemStack>> allAccessory = getAllAccessory(player);
        for (Tuple<AccessoryItem.SlotData, ItemStack> accessoryPair : allAccessory) {
            AccessoryItem.SlotData slot = accessoryPair.getA();
            ItemStack stack = accessoryPair.getB();
            if (stack.getItem() instanceof CustomPowerTrinketInterface cpti) {
                cpti.onFormChange(stack, slot, player);
            } else {
                ApplyAccessoryPowerOnPlayerFormChange(player, BuiltInRegistries.ITEM.getKey(stack.getItem()));
            }
        }
    }

    // public static void ReApplyAccessoryPowerOnPlayerFormChange(PlayerEntity player) {
    //     Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(player);
    //     if (component.isEmpty()) {
    //         return;
    //     }
    //     for (Pair<SlotReference, ItemStack> accessoryPair : component.get().getAllEquipped()) {
    //         SlotReference slot = accessoryPair.getLeft();
    //         ItemStack stack = accessoryPair.getRight();
    //         if (stack.getItem() instanceof CustomPowerTrinketInterface cpti) {
    //             cpti.onFormChange(stack, slot, player);
    //         } else {
    //             ApplyAccessoryPowerOnPlayerFormChange(player, Registries.ITEM.getId(stack.getItem()));
    //         }
    //     }
    // }

    public static void ApplyAccessoryPowerOnEquip(Player player, ResourceLocation accessoryID) {
        if (player.level().isClientSide) {
            return;  // 仅在服务器端执行
        }
        TrinketPowerData powerData = getAccessoryPower(accessoryID);
        if (powerData == null) {
            return;
        }
        powerData.onPlayerEquip(player, accessoryID);
    }

    public static void ApplyAccessoryPowerOnUnEquip(Player player, ResourceLocation accessoryID) {
        if (player.level().isClientSide) {
            return;  // 仅在服务器端执行
        }
        TrinketPowerData powerData = getAccessoryPower(accessoryID);
        if (powerData == null) {
            return;
        }
        powerData.onPlayerUnEquip(player, accessoryID);
    }

    public static void loadAccessoryPowerData(JsonObject jsonObject) {
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String ItemIDRaw = entry.getKey();
            ResourceLocation itemID = ResourceLocation.tryParse(ItemIDRaw);
            if (itemID == null) {
                continue;
            }
            JsonElement jsonElement = entry.getValue();
            if (!jsonElement.isJsonObject()) {
                continue;
            }
            JsonObject jsonPowerData = jsonElement.getAsJsonObject();
            TrinketPowerData powerData = new TrinketPowerData(jsonPowerData);
            registerAccessoryPower(itemID, powerData);
        }
    }
}
