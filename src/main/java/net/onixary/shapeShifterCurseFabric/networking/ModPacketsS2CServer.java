package net.onixary.shapeShifterCurseFabric.networking;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.additional_power.VirtualTotemPower;
import net.onixary.shapeShifterCurseFabric.perk.IPerk;
import net.onixary.shapeShifterCurseFabric.perk.PerkTree;
import net.onixary.shapeShifterCurseFabric.perk.PerkUtils;
import net.onixary.shapeShifterCurseFabric.perk.RegPerks;
import net.onixary.shapeShifterCurseFabric.player_form.DynamicForm;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.onixary.shapeShifterCurseFabric.networking.ModPackets.UPDATE_POWER_ANIM_DATA_TO_CLIENT;

// 纯服务端类，所有send方法都只在这里调用
// This is a pure server-side class, all send methods are called only here
public class ModPacketsS2CServer {

    public static void sendCursedMoonData(ServerPlayer player, boolean isCursedMoon) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(isCursedMoon);
        ServerPlayNetworking.send(player, new BytePayload(BytePayload.id(ModPackets.SYNC_CURSED_MOON_DATA), buf));
    }

    // 发送形态变化同步包
    public static void sendFormChange(ServerPlayer player, ResourceLocation newFormID) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeResourceLocation(newFormID);
        ServerPlayNetworking.send(player, new BytePayload(BytePayload.id(ModPackets.SYNC_FORM_CHANGE), buf));
    }

    /* 重构后不需要了 仅用于参考旧实现逻辑
    public static void sendSyncEffectAttachment(ServerPlayer player, PlayerEffectAttachment attachment) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeNbt(attachment.toNbt());
        //ShapeShifterCurseFabric.LOGGER.info("Attachment sent, nbt: " + attachment.toNbt());
        ServerPlayNetworking.send(player, new BytePayload(BytePayload.id(ModPackets.SYNC_EFFECT_ATTACHMENT),  buf));
    }
     */

    // 发送变身状态同步包
    public static void sendTransformState(ServerPlayer player, boolean isTransforming, ResourceLocation fromForm, ResourceLocation toForm) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUUID(player.getUUID());
        buf.writeBoolean(isTransforming);
        buf.writeUtf(fromForm == null ? "" : fromForm.toString());
        buf.writeUtf(toForm== null ? "" : toForm.toString());
        for (ServerPlayer p : player.serverLevel().players()) {
            FriendlyByteBuf copy = PacketByteBufs.copy(buf);
            ServerPlayNetworking.send(p, new BytePayload(BytePayload.id(ModPackets.SYNC_TRANSFORM_STATE), copy));
        }
    }

    // 发送蝙蝠吸附状态同步包
    public static void sendBatAttachState(ServerPlayer player, boolean isAttached,
                                          int attachType, BlockPos attachedPos, Direction attachedSide) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(isAttached);
        buf.writeInt(attachType); // AttachType枚举的ordinal值

        if (attachedPos != null) {
            buf.writeBoolean(true);
            buf.writeBlockPos(attachedPos);
        } else {
            buf.writeBoolean(false);
        }

        if (attachedSide != null) {
            buf.writeBoolean(true);
            buf.writeInt(attachedSide.get3DDataValue());
        } else {
            buf.writeBoolean(false);
        }

        ServerPlayNetworking.send(player, new BytePayload(BytePayload.id(ModPackets.SYNC_BAT_ATTACH_STATE), buf));
    }

    // 广播给附近其他玩家的蝙蝠吸附状态
    public static void broadcastBatAttachState(ServerPlayer targetPlayer, boolean isAttached,
                                               int attachType, BlockPos attachedPos, Direction attachedSide) {
        // 获取附近的所有玩家（64格范围内）
        targetPlayer.serverLevel().getPlayers(player ->
                player != targetPlayer &&
                        player.distanceToSqr(targetPlayer) <= 64 * 64
        ).forEach(nearbyPlayer -> {
            // 发送目标玩家的吸附状态给附近玩家
            sendOtherPlayerBatAttachState(nearbyPlayer, targetPlayer.getUUID(),
                    isAttached, attachType, attachedPos, attachedSide);
        });
    }

    // 发送其他玩家的蝙蝠吸附状态
    public static void sendOtherPlayerBatAttachState(ServerPlayer receiver, java.util.UUID targetPlayerUuid,
                                                     boolean isAttached, int attachType,
                                                     BlockPos attachedPos, Direction attachedSide) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUUID(targetPlayerUuid);
        buf.writeBoolean(isAttached);
        buf.writeInt(attachType);

        if (attachedPos != null) {
            buf.writeBoolean(true);
            buf.writeBlockPos(attachedPos);
        } else {
            buf.writeBoolean(false);
        }

        if (attachedSide != null) {
            buf.writeBoolean(true);
            buf.writeInt(attachedSide.get3DDataValue());
        } else {
            buf.writeBoolean(false);
        }

        ServerPlayNetworking.send(receiver, new BytePayload(BytePayload.id(ModPackets.SYNC_OTHER_PLAYER_BAT_ATTACH_STATE), buf));
    }

    // 发送强制潜行状态同步包
    public static void sendForceSneakState(ServerPlayer player, boolean shouldForceSneak) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(shouldForceSneak);
        ServerPlayNetworking.send(player, new BytePayload(BytePayload.id(ModPackets.SYNC_FORCE_SNEAK_STATE), buf));
    }

    private static void sendRemoveDynamicFormExcept(ServerPlayer player) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(RegPlayerForms.dynamicPlayerForms.size());
        for (ResourceLocation formId : RegPlayerForms.dynamicPlayerForms) {
            buf.writeUtf(formId.toString());
        }
        ServerPlayNetworking.send(player, new BytePayload(BytePayload.id(ModPackets.REMOVE_DYNAMIC_FORM_EXCEPT), buf));
    }

    // 发送动态Form同步包 旧的最大32K 本来以为挺多的，结果发现单个就快4K
    public static void sendUpdateDynamicForm(ServerPlayer player, JsonObject forms) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(forms.size()); // 发送动态Form数量
        for (String formName : forms.keySet()) {
            buf.writeUtf(formName);
            buf.writeUtf(forms.get(formName).toString());
        }
        ServerPlayNetworking.send(player, new BytePayload(BytePayload.id(ModPackets.UPDATE_DYNAMIC_FORM), buf));
    }

    // 现在理论 单包32K Form数量无限
    public static void updateDynamicForm(ServerPlayer player) {
        int MaxFormPerPacket = 63;  // 2M / 32K - 1
        HashMap<ResourceLocation, DynamicForm> forms = RegPlayerForms.DumpDynamicPlayerForms();
        sendRemoveDynamicFormExcept(player);
        for (int i = 0; i < forms.size(); i += MaxFormPerPacket) {
            JsonObject jsonForms = new JsonObject();
            for (int j = 0; j < MaxFormPerPacket && i + j < forms.size(); j++) {
                ResourceLocation formId = RegPlayerForms.dynamicPlayerForms.get(i + j);
                jsonForms.add(formId.toString(), forms.get(formId).toJson());
            }
            sendUpdateDynamicForm(player, jsonForms);
        }
    }

    // 我暂时没找到玩家进入服务去时的Hook，所以暂时由服务器询问来代替
    public static void sendPlayerLogin(ServerPlayer player) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        ServerPlayNetworking.send(player, new BytePayload(BytePayload.id(ModPackets.LOGIN_PACKET), buf));
    }

    // 已删除 updatePatronForms / updateOldPatronLevel / OpenOldPatronFormSelectMenu（随 Patreon 验证一并删除）

    public static void OpenFormSelectMenu(ServerPlayer player, Player target) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUtf(target.getScoreboardName());
        buf.writeUUID(target.getUUID());
        ServerPlayNetworking.send(player, new BytePayload(BytePayload.id(ModPackets.OPEN_FORM_SELECT_MENU), buf));
    }

    public static void sendActiveVirtualTotem(ServerPlayer player, VirtualTotemPower virtualTotemPower) {
        player.serverLevel().getPlayers(near_player -> near_player.distanceToSqr(player) <= 64 * 64).forEach(
                nearPlayer -> {
                    FriendlyByteBuf buf = virtualTotemPower.create_packet_byte_buf();
                    if (buf != null) {
                        ServerPlayNetworking.send(nearPlayer, new BytePayload(BytePayload.id(ModPackets.ACTIVE_VIRTUAL_TOTEM), buf));
                    }
                }
        );
    }

    public static void sendPowerAnimationDataToClient(ServerPlayer player, UUID PlayerUUID, @Nullable ResourceLocation animationId, int animationCount, int animationLength) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUUID(PlayerUUID);
        if (animationId != null) {
            buf.writeBoolean(true);
            buf.writeResourceLocation(animationId);
        } else {
            buf.writeBoolean(false);
        }
        buf.writeInt(animationCount);
        buf.writeInt(animationLength);
        ServerPlayNetworking.send(player, new BytePayload(BytePayload.id(UPDATE_POWER_ANIM_DATA_TO_CLIENT), buf));
    }

    public static void sendPowerAnimationDataToNearPlayer(ServerPlayer player, @Nullable ResourceLocation animationId, int animationCount, int animationLength) {
        player.serverLevel().getPlayers(near_player -> near_player.distanceToSqr(player) <= 128 * 128).forEach(
                nearPlayer -> sendPowerAnimationDataToClient(nearPlayer, player.getUUID(), animationId, animationCount, animationLength)
        );
    }

    public static void sendNoJumpTick(ServerPlayer player, int tick) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(tick);
        ServerPlayNetworking.send(player, new BytePayload(BytePayload.id(ModPackets.SET_NO_JUMP_TICK), buf));
    }

    public static void sendNoMoveTick(ServerPlayer player, int tick) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(tick);
        ServerPlayNetworking.send(player, new BytePayload(BytePayload.id(ModPackets.SET_NO_MOVE_TICK), buf));
    }


    public static void sendOpenFCSMenu(ServerPlayer player) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        ServerPlayNetworking.send(player, new BytePayload(BytePayload.id(ModPackets.OPEN_FORM_COLOR_SELECT_MENU), buf));
    }

    public static void sendModifyFCDData(ServerPlayer player, String commandType, ResourceLocation formID, String arg1, String arg2, String arg3, String arg4) {
        // commandType ->
        // save ->
        //     formID
        //     arg1 -> slot_type [form, global, form_default]
        //     arg2 -> slot_name
        // load ->
        //     formID
        //     arg1 -> slot_type [form, global, form_default]
        //     arg2 -> slot_name
        // delete ->
        //     formID
        //     arg1 -> slot_type [form, global, form_default]
        //     arg2 -> slot_name
        // config ->
        //     formID -> not used
        //     arg1 -> config_type [enable_default_color]
        //     arg2 -> config_value -> not used only toggle
        // list ->
        //     formID
        //     arg1 -> slot_type [form, global, form_default]
        // to_chat
        //     formID -> not used
        //     arg1 -> send_type [local, server]
        //     arg2 -> message_type [raw, command]
        //     arg3 -> encode_type [base64, hex]

        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUtf(commandType);
        buf.writeResourceLocation(formID);
        buf.writeUtf(arg1);
        buf.writeUtf(arg2);
        buf.writeUtf(arg3);
        buf.writeUtf(arg4);
        ServerPlayNetworking.send(player, new BytePayload(BytePayload.id(ModPackets.MODIFY_FCD_DATA), buf));
    }

    // 已删除 requestPatronAuthFile / sendNewSubKey / sendSetSuperUserLevel（随 Patreon 验证一并删除）

    // 服务端也要注册 S2C payload 类型（fabric networking：发送端必须注册 playS2C，否则 id 未知会回退 DiscardedPayload codec、
    // 把 BytePayload 强转 DiscardedPayload 抛 ClassCastException，玩家进服即崩 -- issue #21）。
    // 只注册类型、不注册客户端 receiver（receiver 仍在客户端 ModPacketsS2C）。REGISTERED_S2C 幂等，双端注册互不冲突。
    public static void registerServerS2C() {
        BytePayload.registerS2C(ModPackets.SYNC_CURSED_MOON_DATA);
        BytePayload.registerS2C(ModPackets.SYNC_FORM_CHANGE);
        BytePayload.registerS2C(ModPackets.SYNC_TRANSFORM_STATE);
        BytePayload.registerS2C(ModPackets.SYNC_BAT_ATTACH_STATE);
        BytePayload.registerS2C(ModPackets.RESET_FIRST_PERSON);
        BytePayload.registerS2C(ModPackets.SYNC_OTHER_PLAYER_BAT_ATTACH_STATE);
        BytePayload.registerS2C(ModPackets.SYNC_FORCE_SNEAK_STATE);
        BytePayload.registerS2C(ModPackets.UPDATE_DYNAMIC_FORM);
        BytePayload.registerS2C(ModPackets.REMOVE_DYNAMIC_FORM_EXCEPT);
        BytePayload.registerS2C(ModPackets.LOGIN_PACKET);
        BytePayload.registerS2C(ModPackets.ACTIVE_VIRTUAL_TOTEM);
        BytePayload.registerS2C(ModPackets.UPDATE_POWER_ANIM_DATA_TO_CLIENT);
        BytePayload.registerS2C(ModPackets.OPEN_FORM_SELECT_MENU);
        BytePayload.registerS2C(ModPackets.SET_NO_JUMP_TICK);
        BytePayload.registerS2C(ModPackets.SET_NO_MOVE_TICK);
        BytePayload.registerS2C(ModPackets.OPEN_FORM_COLOR_SELECT_MENU);
        BytePayload.registerS2C(ModPackets.MODIFY_FCD_DATA);
        // 已移除 MELT_AUTH_SUB_KEY / REQUEST_PATRON_AUTH_FILE / SET_SUPER_USER_LEVEL 的 S2C payload 注册（随 Patreon 验证一并删除）
        // 以下 4 个由本类 send（sendPerkAvailability / sendPerkData / sendOpenFormUpgradeMenu /
        // sendOpenSelectSubFormMenu），此前只写在客户端 ModPacketsS2C#register 的清单里 ——
        // 服务端编码时查不到 payload type，会抛 IllegalArgumentException("Unknown payload type: ...")，
        // 即「一发就崩服」。与 issue #21 是同一类漏注册。
        BytePayload.registerS2C(ModPackets.SYNC_PERK_AVAILABILITY);
        BytePayload.registerS2C(ModPackets.SYNC_PERK_DATA);
        BytePayload.registerS2C(ModPackets.OPEN_FORM_UPGRADE_MENU);
        BytePayload.registerS2C(ModPackets.OPEN_SELECT_SUB_FORM_MENU);
    }

    public static void sendPerkAvailability(ServerPlayer player, boolean fullUpdate, HashMap<ResourceLocation, Boolean> perkAvailability) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(fullUpdate);
        buf.writeInt(perkAvailability.size());
        for (Map.Entry<ResourceLocation, Boolean> entry : perkAvailability.entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            buf.writeBoolean(entry.getValue());
        }
        ServerPlayNetworking.send(player, new BytePayload(BytePayload.id(ModPackets.SYNC_PERK_AVAILABILITY), buf));
    }

    public static void sendPerkAvailabilityFull(ServerPlayer player) {
        sendPerkAvailability(player, true, PerkUtils.getPlayerPerkAvailability(player));
    }

    public static void sendPerkData(ServerPlayer player, boolean fullUpdate, IPerk... perks) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(fullUpdate);
        buf.writeInt(perks.length);
        for (IPerk perk : perks) {
            buf.writeResourceLocation(perk.getID());
            buf.writeInt(perk.getXpCost());
        }
        ServerPlayNetworking.send(player, new BytePayload(BytePayload.id(ModPackets.SYNC_PERK_DATA), buf));
    }

    public static void sendPerkDataFull(ServerPlayer player) {
        PerkTree perkTree = PerkUtils.getPlayerNowPerkTree(player);
        if (perkTree == null) {
            return;
        }
        List<ResourceLocation> perks = perkTree.getAllPerks();
        sendPerkData(player, true, perks.stream().map(RegPerks::getPerk).filter(Objects::nonNull).toArray(IPerk[]::new));
    }

    public static void sendOpenFormUpgradeMenu(ServerPlayer player, int tier) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(tier);
        ServerPlayNetworking.send(player, new BytePayload(BytePayload.id(ModPackets.OPEN_FORM_UPGRADE_MENU), buf));
    }

    public static void sendOpenSelectSubFormMenu(ServerPlayer player) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        ServerPlayNetworking.send(player, new BytePayload(BytePayload.id(ModPackets.OPEN_SELECT_SUB_FORM_MENU), buf));
    }
}