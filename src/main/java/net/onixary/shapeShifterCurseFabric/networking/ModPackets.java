package net.onixary.shapeShifterCurseFabric.networking;

import net.minecraft.resources.ResourceLocation;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

public class ModPackets {
    public static final ResourceLocation CHANNEL = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "main");

    public static final ResourceLocation VALIDATE_START_BOOK_BUTTON = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "validate_start_book_button");

    // 新增服务端到客户端的附件同步包
    // New server-to-client attachment sync packet
    // public static final Identifier SYNC_EFFECT_ATTACHMENT = Identifier.of(ShapeShifterCurseFabric.MOD_ID, "sync_effect_attachment");


    public static final ResourceLocation SYNC_CURSED_MOON_DATA = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "sync_cursed_moon_data");

    // 添加形态变化同步包
    public static final ResourceLocation SYNC_FORM_CHANGE = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "sync_form_change");

    // 添加变身状态同步包
    public static final ResourceLocation SYNC_TRANSFORM_STATE = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "sync_transform_state");

    // 添加Overlay效果相关的网络包
    public static final ResourceLocation RESET_FIRST_PERSON = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "reset_first_person");

    // Bat attach power sync packet
    public static final ResourceLocation SYNC_BAT_ATTACH_STATE = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "sync_bat_attach_state");
    public static final ResourceLocation JUMP_DETACH_REQUEST_ID = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "jump_detach_request");
    public static final ResourceLocation SYNC_OTHER_PLAYER_BAT_ATTACH_STATE = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "sync_other_player_bat_attach_state");

    // jump_event packets
    public static final ResourceLocation JUMP_EVENT_ID = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "jump_event");

    public static final ResourceLocation SPRINTING_TO_SNEAKING_EVENT_ID = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "sprinting_to_sneaking_event");

    public static final ResourceLocation SYNC_FORCE_SNEAK_STATE = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "sync_force_sneak_state");

    public static final ResourceLocation UPDATE_DYNAMIC_FORM = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "update_dynamic_form");
    public static final ResourceLocation REMOVE_DYNAMIC_FORM_EXCEPT = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "remove_dynamic_form_except");

    public static final ResourceLocation LOGIN_PACKET = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "login_packet");  // 我暂时没找到玩家进入服务去时的Hook，所以暂时由服务器询问来代替
    public static final ResourceLocation UPDATE_CUSTOM_SETTING = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "update_custom_setting");
    public static final ResourceLocation UPDATE_CUSTOM_COLOR = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "update_custom_color");

    public static final ResourceLocation ACTIVE_VIRTUAL_TOTEM = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "active_virtual_totem");

    public static final ResourceLocation UPDATE_PATRON_LEVEL = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "update_patron_level");
    public static final ResourceLocation OPEN_PATRON_FORM_SELECT_MENU = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "open_patron_form_select_menu");
    public static final ResourceLocation SET_PATRON_FORM = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "set_patron_form");

    // S2C 结构: UUID-玩家UUID Boolean-是否启用动画, (仅在启用动画时包含)Identifier-动画ID, Int-动画次数. Int-动画时长
    public static final ResourceLocation UPDATE_POWER_ANIM_DATA_TO_CLIENT = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "update_power_anim_data_to_client");
    // C2S 结构: Boolean-是否启用动画, (仅在启用动画时包含)Identifier-动画ID, Int-动画次数. Int-动画时长
    public static final ResourceLocation UPDATE_POWER_ANIM_DATA_TO_SERVER = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "update_power_anim_data_to_server");
    // C2S 结构: UUID-玩家UUID
    public static final ResourceLocation REQUEST_POWER_ANIM_DATA = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "request_power_anim_data");

    public static final ResourceLocation SET_FORM = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "set_form");
    public static final ResourceLocation OPEN_FORM_SELECT_MENU = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "open_form_select_menu");

    public static final ResourceLocation SET_NO_JUMP_TICK = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "set_no_jump_tick");
    public static final ResourceLocation SET_NO_MOVE_TICK = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "set_no_move_tick");

    public static final ResourceLocation OPEN_FORM_COLOR_SELECT_MENU = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "open_form_color_select_menu");
    public static final ResourceLocation MODIFY_FCD_DATA = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "modify_fcd_data");

    public static final ResourceLocation REQUEST_PATRON_AUTH_FILE = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "request_patron_auth_file");
    public static final ResourceLocation UPLOAD_PATRON_AUTH_FILE = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "upload_patron_auth_file");
    public static final ResourceLocation MELT_AUTH_SUB_KEY = ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "melt_auth_sub_key");
}