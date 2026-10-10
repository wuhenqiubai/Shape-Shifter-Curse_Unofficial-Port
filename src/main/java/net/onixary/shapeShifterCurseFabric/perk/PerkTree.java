package net.onixary.shapeShifterCurseFabric.perk;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

// Common Side
public class PerkTree {
    public static class PerkNode {
        public final ResourceLocation perkID;
        public final int tier;
        public final int y;
        public final @NotNull ArrayList<@NotNull IDependent> dependents;

        public PerkNode(ResourceLocation perkID, int tier, int y) {
            this.perkID = perkID;
            this.tier = tier;
            this.y = y;
            this.dependents = new ArrayList<>();
        }

        public PerkNode(ResourceLocation perkID, int tier, int y, @NotNull IDependent... dependentPerkIDs) {
            this.perkID = perkID;
            this.tier = tier;
            this.y = y;
            this.dependents = new ArrayList<>(Arrays.asList(dependentPerkIDs));
        }
    }

    public final ResourceLocation treeID;
    public final List<PerkNode> perkNodes = new ArrayList<>();
    public final List<PerkNode> virtualNodes = new ArrayList<>();
    public final Map<ResourceLocation, PerkNode> perkNodeMap = new HashMap<>();
    public final Map<ResourceLocation, PerkNode> virtualNodeMap = new HashMap<>();

    public PerkTree(ResourceLocation treeID) {
        this.treeID = treeID;
    }

    public ResourceLocation getID() {
        return treeID;
    }

    public PerkTree addNode(ResourceLocation perkID, int tier, int y) {
        return this.addNode(new PerkNode(perkID, tier, y));
    }

    public PerkTree addVirtualNode(ResourceLocation perkID, int tier, int y) {
        return this.addVirtualNode(new PerkNode(perkID, tier, y));
    }

    public PerkTree addNode(ResourceLocation perkID, int tier, int y, IDependent... dependents) {
        return this.addNode(new PerkNode(perkID, tier, y, dependents));
    }

    public PerkTree addVirtualNode(ResourceLocation perkID, int tier, int y, IDependent... dependents) {
        return this.addVirtualNode(new PerkNode(perkID, tier, y, dependents));
    }

    public PerkTree addNode(PerkNode perkNode) {
        perkNodes.add(perkNode);
        perkNodeMap.put(perkNode.perkID, perkNode);
        return this;
    }

    public PerkTree addVirtualNode(PerkNode perkNode) {
        virtualNodes.add(perkNode);
        virtualNodeMap.put(perkNode.perkID, perkNode);
        return this;
    }

    public @Nullable PerkNode getNode(ResourceLocation perkID) {
        return perkNodeMap.get(perkID);
    }

    public @Nullable PerkNode getVirtualNode(ResourceLocation perkID) {
        return virtualNodeMap.get(perkID);
    }

    public @NotNull List<PerkNode> getDependentNode(ResourceLocation perkID) {
        PerkNode perkNode = getNode(perkID);
        if (perkNode != null) {
            return perkNodes.stream().filter(perkNode1 -> perkNode1.dependents.contains(perkID)).toList();
        }
        return null;
    }

    public @NotNull List<ResourceLocation> getAllPerks() {
        return new ArrayList<>(perkNodeMap.keySet());
    }

    public @NotNull List<ResourceLocation> getAllVirtualPerks() {
        return new ArrayList<>(virtualNodeMap.keySet());
    }

    public @NotNull List<PerkNode> getAllNodes() {
        return new ArrayList<>(perkNodes);
    }

    public @NotNull List<PerkNode> getAllVirtualNodes() {
        return new ArrayList<>(virtualNodes);
    }
}
