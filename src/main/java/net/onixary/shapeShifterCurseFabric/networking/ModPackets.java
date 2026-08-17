package net.onixary.shapeShifterCurseFabric.networking;

import net.minecraft.resources.Identifier;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

public class ModPackets {
    public static final Identifier CHANNEL = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "main");

    public static final Identifier VALIDATE_START_BOOK_BUTTON = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "validate_start_book_button");

    // 新增服务端到客户端的附件同步包
    // New server-to-client attachment sync packet
    // public static final Identifier SYNC_EFFECT_ATTACHMENT = Identifier.of(ShapeShifterCurseFabric.MOD_ID, "sync_effect_attachment");


    public static final Identifier SYNC_CURSED_MOON_DATA = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "sync_cursed_moon_data");

    // 添加形态变化同步包
    public static final Identifier SYNC_FORM_CHANGE = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "sync_form_change");

    // 添加变身状态同步包
    public static final Identifier SYNC_TRANSFORM_STATE = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "sync_transform_state");

    // 添加Overlay效果相关的网络包
    public static final Identifier RESET_FIRST_PERSON = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "reset_first_person");

    // Bat attach power sync packet
    public static final Identifier SYNC_BAT_ATTACH_STATE = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "sync_bat_attach_state");
    public static final Identifier JUMP_DETACH_REQUEST_ID = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "jump_detach_request");
    public static final Identifier SYNC_OTHER_PLAYER_BAT_ATTACH_STATE = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "sync_other_player_bat_attach_state");

    // jump_event packets
    public static final Identifier JUMP_EVENT_ID = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "jump_event");

    public static final Identifier SPRINTING_TO_SNEAKING_EVENT_ID = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "sprinting_to_sneaking_event");

    public static final Identifier SYNC_FORCE_SNEAK_STATE = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "sync_force_sneak_state");

    public static final Identifier UPDATE_DYNAMIC_FORM = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "update_dynamic_form");
    public static final Identifier REMOVE_DYNAMIC_FORM_EXCEPT = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "remove_dynamic_form_except");

    public static final Identifier LOGIN_PACKET = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "login_packet");  // 我暂时没找到玩家进入服务去时的Hook，所以暂时由服务器询问来代替
    public static final Identifier UPDATE_CUSTOM_SETTING = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "update_custom_setting");
    public static final Identifier UPDATE_CUSTOM_COLOR = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "update_custom_color");

    public static final Identifier ACTIVE_VIRTUAL_TOTEM = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "active_virtual_totem");

    public static final Identifier UPDATE_PATRON_LEVEL = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "update_patron_level");
    public static final Identifier OPEN_PATRON_FORM_SELECT_MENU = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "open_patron_form_select_menu");
    public static final Identifier SET_PATRON_FORM = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "set_patron_form");

    // S2C 结构: UUID-玩家UUID Boolean-是否启用动画, (仅在启用动画时包含)Identifier-动画ID, Int-动画次数. Int-动画时长
    public static final Identifier UPDATE_POWER_ANIM_DATA_TO_CLIENT = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "update_power_anim_data_to_client");
    // C2S 结构: Boolean-是否启用动画, (仅在启用动画时包含)Identifier-动画ID, Int-动画次数. Int-动画时长
    public static final Identifier UPDATE_POWER_ANIM_DATA_TO_SERVER = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "update_power_anim_data_to_server");
    // C2S 结构: UUID-玩家UUID
    public static final Identifier REQUEST_POWER_ANIM_DATA = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "request_power_anim_data");

    public static final Identifier SET_FORM = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "set_form");
    public static final Identifier OPEN_FORM_SELECT_MENU = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "open_form_select_menu");

    public static final Identifier SET_NO_JUMP_TICK = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "set_no_jump_tick");
    public static final Identifier SET_NO_MOVE_TICK = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "set_no_move_tick");

    public static final Identifier OPEN_FORM_COLOR_SELECT_MENU = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "open_form_color_select_menu");
    public static final Identifier MODIFY_FCD_DATA = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "modify_fcd_data");

    public static final Identifier REQUEST_PATRON_AUTH_FILE = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "request_patron_auth_file");
    public static final Identifier UPLOAD_PATRON_AUTH_FILE = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "upload_patron_auth_file");
    public static final Identifier MELT_AUTH_SUB_KEY = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "melt_auth_sub_key");

    public static final Identifier SET_SUPER_USER_LEVEL = Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "set_super_user_level");
}