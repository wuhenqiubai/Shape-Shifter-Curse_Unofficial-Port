package net.onixary.shapeShifterCurseFabric.integration.origins.networking;

import net.minecraft.resources.Identifier;
import net.onixary.shapeShifterCurseFabric.integration.origins.Origins;

public class ModPackets {

    public static final Identifier HANDSHAKE = Origins.identifier("handshake");

    public static final Identifier OPEN_ORIGIN_SCREEN = Identifier.fromNamespaceAndPath(Origins.MODID, "open_origin_screen");
    public static final Identifier CHOOSE_ORIGIN = Identifier.fromNamespaceAndPath(Origins.MODID, "choose_origin");
    public static final Identifier USE_ACTIVE_POWERS = Identifier.fromNamespaceAndPath(Origins.MODID, "use_active_powers");
    public static final Identifier ORIGIN_LIST = Identifier.fromNamespaceAndPath(Origins.MODID, "origin_list");
    public static final Identifier LAYER_LIST = Identifier.fromNamespaceAndPath(Origins.MODID, "layer_list");
    public static final Identifier POWER_LIST = Identifier.fromNamespaceAndPath(Origins.MODID, "power_list");
    public static final Identifier CHOOSE_RANDOM_ORIGIN = Identifier.fromNamespaceAndPath(Origins.MODID, "choose_random_origin");
    public static final Identifier CONFIRM_ORIGIN = Origins.identifier("confirm_origin");
    public static final Identifier PLAYER_LANDED = Origins.identifier("player_landed");
    public static final Identifier BADGE_LIST = Origins.identifier("badge_list");
}