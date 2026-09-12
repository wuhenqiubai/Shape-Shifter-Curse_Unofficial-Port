package net.onixary.shapeShifterCurseFabric.perk;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.player_form.utils.PlayerFormComponent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PerkUtils {
    public static HashMap<ResourceLocation, List<ResourceLocation>> getPlayerPerks(Player player) {
        PlayerFormComponent component = PlayerFormComponent.COMPONENT.get(player);
        return component.formPerkMap;
    }

    public static @Nullable List<ResourceLocation> getPlayerPerks(Player player, ResourceLocation perkTreeID) {
        return getPlayerPerks(player).get(perkTreeID);
    }

    public static void removeInValidPerk(Player player, ResourceLocation perkTreeID) {
        if (!(player instanceof ServerPlayer playerEntity)) return;
        PlayerFormComponent component = PlayerFormComponent.COMPONENT.get(player);
        PerkTree perkTree = RegPerks.getPerkTree(perkTreeID);
        if (perkTree == null) {
            if (component.formPerkMap.containsKey(perkTreeID)) {
                component.formPerkMap.remove(perkTreeID);
                component.sync();
            }
            return;
        }
        List<ResourceLocation> playerPerkList = component.formPerkMap.get(perkTreeID);
        if (playerPerkList == null) return;
        List<ResourceLocation> validPerkList = perkTree.getAllPerks();
        List<ResourceLocation> finalPerks = new ArrayList<>();
        for (ResourceLocation playerPerkID : playerPerkList) {
            if (validPerkList.contains(playerPerkID) && RegPerks.getPerk(playerPerkID) != null) {
                finalPerks.add(playerPerkID);
            }
        }
        component.formPerkMap.put(perkTreeID, finalPerks);
        component.sync();
    }

    public static void __addPerk(Player player, ResourceLocation perkTreeID, ResourceLocation perkID) {
        IPerk perkData = RegPerks.getPerk(perkID);
        if (perkData == null) return;
        PlayerFormComponent component = PlayerFormComponent.COMPONENT.get(player);
        List<ResourceLocation> perkList = component.formPerkMap.computeIfAbsent(perkTreeID, k -> new ArrayList<>());
        perkList.add(perkID);
        component.sync();
        perkData.onGain(player, component.nowForm);
    }

    public static void addPerk(Player player, ResourceLocation perkTreeID, ResourceLocation perkID) {
        if (!(player instanceof ServerPlayer playerEntity)) {
            // TODO 发送加技能点请求
            return;
        }
        IPerk perkData = RegPerks.getPerk(perkID);
        if (perkData == null) return;
        PerkTree perkTree = RegPerks.getPerkTree(perkTreeID);
        if (perkTree == null) return;
        if (!perkTree.getAllPerks().contains(perkID)) return;

        __addPerk(player, perkTreeID, perkID);
        removeInValidPerk(player, perkTreeID);
    }

    public static void addPerkFromClient(Player player, ResourceLocation perkTreeID, ResourceLocation perkID) {
        if (!(player instanceof ServerPlayer playerEntity)) return;
        IPerk perkData = RegPerks.getPerk(perkID);
        if (perkData == null) return;
        PerkTree perkTree = RegPerks.getPerkTree(perkTreeID);
        if (perkTree == null) return;
        if (!perkTree.getAllPerks().contains(perkID)) return;

        PerkTree.PerkNode node = perkTree.getNode(perkID);
        if (node == null) return;
        if (node.dependentPerkID() != null) {
            List<ResourceLocation> playerPerkList = getPlayerPerks(player, perkTreeID);
            if (playerPerkList == null || !playerPerkList.contains(node.dependentPerkID())) return;
        }
        int tier = node.tier();
        // TODO tier 判断 需要给升级方块加个玩家UUID表 记录最后一个使用的升级方块等级
        __addPerk(player, perkTreeID, perkID);
        removeInValidPerk(player, perkTreeID);
    }

    public static void loadAllPerk(Player player, ResourceLocation perkTreeID) {
        if (!(player instanceof ServerPlayer playerEntity)) return;
        removeInValidPerk(player, perkTreeID);
        PlayerFormComponent component = PlayerFormComponent.COMPONENT.get(player);
        List<ResourceLocation> perkList = component.formPerkMap.get(perkTreeID);
        if (perkList == null) return;
        for (ResourceLocation perkID : perkList) {
            IPerk perkData = RegPerks.getPerk(perkID);
            if (perkData != null) {
                perkData.onLoad(player, component.nowForm);
            }
        }
    }
}
