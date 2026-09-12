package net.onixary.shapeShifterCurseFabric.perk;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Common Side
public class PerkTree {
    public record PerkNode(ResourceLocation perkID, int tier, int y, @Nullable ResourceLocation dependentPerkID) {
    }

    public final ResourceLocation treeID;
    public final List<PerkNode> perkNodes = new ArrayList<>();
    public final Map<ResourceLocation, PerkNode> perkNodeMap = new HashMap<>();

    public PerkTree(ResourceLocation treeID) {
        this.treeID = treeID;
    }

    public ResourceLocation getID() {
        return treeID;
    }

    public PerkTree addNode(ResourceLocation perkID, int tier, int y, @Nullable ResourceLocation dependentPerkID) {
        return this.addNode(new PerkNode(perkID, tier, y, dependentPerkID));
    }

    public PerkTree addNode(PerkNode perkNode) {
        perkNodes.add(perkNode);
        perkNodeMap.put(perkNode.perkID, perkNode);
        return this;
    }

    public @Nullable PerkNode getNode(ResourceLocation perkID) {
        return perkNodeMap.get(perkID);
    }

    public @Nullable PerkNode getDependentNode(ResourceLocation perkID) {
        PerkNode perkNode = getNode(perkID);
        if (perkNode != null && perkNode.dependentPerkID != null) {
            return getNode(perkNode.dependentPerkID);
        }
        return null;
    }

    public @NotNull List<ResourceLocation> getAllPerks() {
        return new ArrayList<>(perkNodeMap.keySet());
    }

    public @NotNull List<PerkNode> getAllNodes() {
        return new ArrayList<>(perkNodes);
    }
}
