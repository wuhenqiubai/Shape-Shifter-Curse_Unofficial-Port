package net.onixary.shapeShifterCurseFabric.perk;

import com.google.common.base.Objects;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.blocks.FormAttunerBlock;
import net.onixary.shapeShifterCurseFabric.blocks.block_entity.FormAttunerBlockEntity;
import net.onixary.shapeShifterCurseFabric.cursed_moon.CursedMoon;
import net.onixary.shapeShifterCurseFabric.networking.ModPacketsS2C;
import net.onixary.shapeShifterCurseFabric.player_form.utils.PlayerFormComponent;
import net.onixary.shapeShifterCurseFabric.util.util.cost.ICost;
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

    public static @Nullable List<IPerk> getPlayerPerksObject(Player player, ResourceLocation perkTreeID) {
        // 仅服务器端 客户端不保证数据能完整拿到
        List<ResourceLocation> perkList = getPlayerPerks(player, perkTreeID);
        if (perkList == null) return null;
        List<IPerk> perkDataList = new ArrayList<>();
        for (ResourceLocation perkID : perkList) {
            IPerk perkData = RegPerks.getPerk(perkID);
            if (perkData != null) {
                perkDataList.add(perkData);
            }
        }
        return perkDataList;
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
        if (!perkData.canRepeat()) {
            perkList.add(perkID);
        }
        component.sync();
        perkData.onGain(player, component.nowForm);
        PowerHolderComponent.KEY.sync(player);
    }

    public static void addPerk(Player player, ResourceLocation perkTreeID, ResourceLocation perkID) {
        if (!(player instanceof ServerPlayer playerEntity)) {
            ModPacketsS2C.sendAddPerk(perkTreeID, perkID);
            return;
        }
        IPerk perkData = RegPerks.getPerk(perkID);
        if (perkData == null) return;
        PerkTree perkTree = RegPerks.getPerkTree(perkTreeID);
        if (perkTree == null) return;
        if (!perkTree.getAllPerks().contains(perkID)) return;
        PlayerFormComponent component = PlayerFormComponent.COMPONENT.get(player);
        if (perkData.canGain(player, component.nowForm)) {
            __addPerk(player, perkTreeID, perkID);
        }
        removeInValidPerk(player, perkTreeID);
    }

    public static void addPerkFromClient(Player player, ResourceLocation perkTreeID, ResourceLocation perkID) {
        if (!(player instanceof ServerPlayer playerEntity)) return;
        PlayerFormComponent component = PlayerFormComponent.COMPONENT.get(player);
        if (!Objects.equal(perkTreeID, component.nowPerkTree)) return;
        IPerk perkData = RegPerks.getPerk(perkID);
        if (perkData == null) return;
        PerkTree perkTree = RegPerks.getPerkTree(perkTreeID);
        if (perkTree == null) return;
        if (!perkTree.getAllPerks().contains(perkID)) return;

        ICost cost = perkData.getCost();
        if (!player.getAbilities().instabuild) {
            if (!cost.getType().canPay(cost, player)) {
                return;
            }
        }

        PerkTree.PerkNode node = perkTree.getNode(perkID);
        if (node == null) return;
        List<ResourceLocation> playerPerkList = getPlayerPerks(player, perkTreeID);
        if (!node.dependentPerkIDs.isEmpty()) {
            if (playerPerkList == null) return;
            for (ResourceLocation dependentPerkID : node.dependentPerkIDs) {
                if (!playerPerkList.contains(dependentPerkID)) return;
            }
        }
        if (playerPerkList != null && playerPerkList.contains(perkID)) {
            return;
        }
        int tier = node.tier;
        // 感觉Tier0在无诅咒之月可以点可以作为特性使用 可以在tier0设置一些特殊的Perk
        if (tier > 0 && !isCanGainPerk(player)) {
            return;
        }
        @Nullable FormAttunerBlockEntity lastUsedAttuner = FormAttunerBlock.getPlayerLastUsedAttuner(player);
        if (lastUsedAttuner == null || lastUsedAttuner.level < tier) {
            return;
        }

        if (perkData.canGain(player, component.nowForm)) {
            if (!player.getAbilities().instabuild) {
                cost.getType().pay(cost, player);
            }
            __addPerk(player, perkTreeID, perkID);
        }
        removeInValidPerk(player, perkTreeID);
    }

    public static boolean isCanGainPerk(Player player) {
        // 仅检测从客户端提交的加点请求 服务器端的加点请求直接过 所以这里只能加环境检测
        Level world = player.level();
        if (world.dimension() != Level.OVERWORLD) {
            return false;
        }
        if (!CursedMoon.isInCursedMoon(world)) {
            return false;
        }
        return true;
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

    public static ResourceLocation getPlayerNowPerkTreeID(Player player) {
        PlayerFormComponent component = PlayerFormComponent.COMPONENT.get(player);
        return component.nowPerkTree;
    }

    public static @Nullable PerkTree getPlayerNowPerkTree(Player player) {
        ResourceLocation perkTreeID = getPlayerNowPerkTreeID(player);
        return RegPerks.getPerkTree(perkTreeID);
    }

    public static void setPlayerNowPerkTreeID(Player player, ResourceLocation perkTreeID) {
        PlayerFormComponent component = PlayerFormComponent.COMPONENT.get(player);
        component.nowPerkTree = perkTreeID;
        component.sync();
    }

    public static HashMap<ResourceLocation, Boolean> getPlayerPerkAvailability(Player player) {
        PerkTree perkTree = getPlayerNowPerkTree(player);
        if (perkTree == null) return new HashMap<>();
        HashMap<ResourceLocation, Boolean> perkAvailability = new HashMap<>();
        for (ResourceLocation perkID : perkTree.getAllPerks()) {
            IPerk perkData = RegPerks.getPerk(perkID);
            if (perkData != null) {
                perkAvailability.put(perkID, perkData.canGain(player, PlayerFormComponent.COMPONENT.get(player).nowForm));
            }
        }
        return perkAvailability;
    }
}
