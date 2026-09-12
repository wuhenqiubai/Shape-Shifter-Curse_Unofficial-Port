package net.onixary.shapeShifterCurseFabric.perk;

import net.minecraft.resources.ResourceLocation;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class RegPerks {
    public static final HashMap<ResourceLocation, IPerk> PerkRegistry = new HashMap<>();
    public static final HashMap<ResourceLocation, ResourceLocation> PerkIconRegistry = new HashMap<>();
    public static final HashMap<ResourceLocation, PerkTree> PerkTreeRegistry = new HashMap<>();

    public static final ResourceLocation FALLBACK_PERK_ICON = ShapeShifterCurseFabric.identifier("textures/perk/fallback.png");

    public static final ResourceLocation P_FireBallPlusL1 = registerPerk(
            new NormalPerk(ShapeShifterCurseFabric.identifier("fire_ball_plus_1"))
                    .addPower()
                    .removePower()
    );

    public static final ResourceLocation P_FireBallPlusL2 = registerPerk(
            new NormalPerk(ShapeShifterCurseFabric.identifier("fire_ball_plus_2"))
                    .addPower()
                    .removePower()
    );

    public static final ResourceLocation T_FFoxTree = registerPerkTree(
            new PerkTree(ShapeShifterCurseFabric.identifier("f_fox_tree"))
                    .addNode(ShapeShifterCurseFabric.identifier("fire_ball_plus_1"), 1, 0, null)
                    .addNode(ShapeShifterCurseFabric.identifier("fire_ball_plus_2"), 2, 0, ShapeShifterCurseFabric.identifier("fire_ball_plus_1"))
    );

    static {
        registerPerkIcon(P_FireBallPlusL1, ShapeShifterCurseFabric.identifier("textures/perk/fire_ball_plus_1.png"));
        registerPerkIcon(P_FireBallPlusL2, ShapeShifterCurseFabric.identifier("textures/perk/fire_ball_plus_2.png"));
    }

    public static ResourceLocation registerPerk(IPerk perk) {
        PerkRegistry.put(perk.getID(), perk);
        return perk.getID();
    }

    public static @Nullable IPerk getPerk(ResourceLocation perkID) {
        return PerkRegistry.get(perkID);
    }

    public static ResourceLocation registerPerkTree(PerkTree perkTree) {
        PerkTreeRegistry.put(perkTree.getID(), perkTree);
        return perkTree.getID();
    }

    public static @Nullable PerkTree getPerkTree(ResourceLocation perkTreeID) {
        return PerkTreeRegistry.get(perkTreeID);
    }

    public static void registerPerkIcon(ResourceLocation perkID, ResourceLocation iconID) {
        PerkIconRegistry.put(perkID, iconID);
    }

    public static @Nullable ResourceLocation getPerkIcon(ResourceLocation perkID) {
        return PerkIconRegistry.get(perkID);
    }
}
