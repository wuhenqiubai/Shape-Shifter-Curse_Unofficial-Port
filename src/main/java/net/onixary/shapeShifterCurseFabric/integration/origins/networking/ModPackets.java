package net.onixary.shapeShifterCurseFabric.integration.origins.networking;

import net.minecraft.resources.ResourceLocation;
import net.onixary.shapeShifterCurseFabric.integration.origins.Origins;

public class ModPackets {

    public static final ResourceLocation HANDSHAKE = Origins.identifier("handshake");

    public static final ResourceLocation OPEN_ORIGIN_SCREEN = ResourceLocation.fromNamespaceAndPath(Origins.MODID, "open_origin_screen");
    public static final ResourceLocation CHOOSE_ORIGIN = ResourceLocation.fromNamespaceAndPath(Origins.MODID, "choose_origin");
    public static final ResourceLocation USE_ACTIVE_POWERS = ResourceLocation.fromNamespaceAndPath(Origins.MODID, "use_active_powers");
    public static final ResourceLocation ORIGIN_LIST = ResourceLocation.fromNamespaceAndPath(Origins.MODID, "origin_list");
    public static final ResourceLocation LAYER_LIST = ResourceLocation.fromNamespaceAndPath(Origins.MODID, "layer_list");
    public static final ResourceLocation POWER_LIST = ResourceLocation.fromNamespaceAndPath(Origins.MODID, "power_list");
    public static final ResourceLocation CHOOSE_RANDOM_ORIGIN = ResourceLocation.fromNamespaceAndPath(Origins.MODID, "choose_random_origin");
    public static final ResourceLocation CONFIRM_ORIGIN = Origins.identifier("confirm_origin");
    public static final ResourceLocation PLAYER_LANDED = Origins.identifier("player_landed");
    public static final ResourceLocation BADGE_LIST = Origins.identifier("badge_list");
}
