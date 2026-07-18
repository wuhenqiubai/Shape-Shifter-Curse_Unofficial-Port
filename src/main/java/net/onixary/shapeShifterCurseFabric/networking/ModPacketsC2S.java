package net.onixary.shapeShifterCurseFabric.networking;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.additional_power.ActionOnJumpPower;
import net.onixary.shapeShifterCurseFabric.additional_power.ActionOnSprintingToSneakingPower;
import net.onixary.shapeShifterCurseFabric.additional_power.BatBlockAttachPower;
import net.onixary.shapeShifterCurseFabric.additional_power.JumpEventCondition;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.IPlayerAnimController;
import net.onixary.shapeShifterCurseFabric.player_form.DynamicForm;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.skin.PlayerSkinComponent;
import net.onixary.shapeShifterCurseFabric.player_form.skin.RegPlayerSkinComponent;
import net.onixary.shapeShifterCurseFabric.player_form.utils.TransformManager;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;
import net.onixary.shapeShifterCurseFabric.util.PatronUtils;
import net.onixary.shapeShifterCurseFabric.util.Verify.AuthServer;

import java.util.UUID;

import static net.onixary.shapeShifterCurseFabric.networking.ModPackets.*;

// 应仅在服务器端注册
// This class should only be registered on the server side
public class ModPacketsC2S {

    public static void register() {
        // Register C2S payload types before registering handlers
	    registerClient();

        ServerPlayNetworking.registerGlobalReceiver(BytePayload.id(VALIDATE_START_BOOK_BUTTON), (payload, ctx) -> {
            ServerPlayerEntity player = ctx.player();
            if (player != null && RegPlayerForms.ORIGINAL_BEFORE_ENABLE.isPlayerForm(player)) {
                TransformManager.startTransform(player, RegPlayerForms.ORIGINAL_SHIFTER, null);
                ShapeShifterCurseFabric.ON_ENABLE_MOD.trigger(player);
                player.sendMessage(Text.translatable("info.shape-shifter-curse.on_enable_mod").formatted(Formatting.LIGHT_PURPLE));
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(BytePayload.id(Identifier.of(ShapeShifterCurseFabric.MOD_ID, "update_skin_setting")), (payload, ctx) -> {
            PacketByteBuf buf = payload.data();
            boolean keepOriginalSkin = buf.readBoolean();
            ServerPlayerEntity player = ctx.player();
            PlayerSkinComponent skinComp = RegPlayerSkinComponent.SKIN_SETTINGS.get(player);
            skinComp.setKeepOriginalSkin(keepOriginalSkin);
            RegPlayerSkinComponent.SKIN_SETTINGS.sync(player);
        });
        ServerPlayNetworking.registerGlobalReceiver(BytePayload.id(JUMP_DETACH_REQUEST_ID), (payload, ctx) -> {
            ServerPlayerEntity player = ctx.player();
            BatBlockAttachPower attachPower = PowerHolderComponent.getPowers(player, BatBlockAttachPower.class)
                    .stream().filter(BatBlockAttachPower::isAttached).findFirst().orElse(null);
            if (attachPower != null) attachPower.handleJump(player);
        });
        ServerPlayNetworking.registerGlobalReceiver(BytePayload.id(JUMP_EVENT_ID), (payload, ctx) -> {
            PacketByteBuf buf = payload.data();
            UUID playerUuid = buf.readUuid();
            ServerPlayerEntity player = ctx.player();
            if (player.getUuid().equals(playerUuid)) JumpEventCondition.setJumping(player, true);
            PowerHolderComponent.getPowers(player, ActionOnJumpPower.class).forEach(ActionOnJumpPower::executeAction);
        });
        ServerPlayNetworking.registerGlobalReceiver(BytePayload.id(SPRINTING_TO_SNEAKING_EVENT_ID), (payload, ctx) -> {
            PacketByteBuf buf = payload.data();
            UUID playerUuid = buf.readUuid();
            ServerPlayerEntity player = ctx.player();
            if (player.getUuid().equals(playerUuid)) {
                PowerHolderComponent.getPowers(player, ActionOnSprintingToSneakingPower.class).forEach(ActionOnSprintingToSneakingPower::executeAction);
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(BytePayload.id(UPDATE_CUSTOM_SETTING), ModPacketsC2S::onUpdatePlayerCustomConfig);
        ServerPlayNetworking.registerGlobalReceiver(BytePayload.id(UPDATE_CUSTOM_COLOR), ModPacketsC2S::onUpdatePlayerCustomColor);
        ServerPlayNetworking.registerGlobalReceiver(BytePayload.id(SET_PATRON_FORM), ModPacketsC2S::receiveSetPatronForm);
        ServerPlayNetworking.registerGlobalReceiver(BytePayload.id(SET_FORM), ModPacketsC2S::receiveSetForm);
        ServerPlayNetworking.registerGlobalReceiver(BytePayload.id(UPDATE_POWER_ANIM_DATA_TO_SERVER), ModPacketsC2S::onUpdatePowerAnimationData);
        ServerPlayNetworking.registerGlobalReceiver(BytePayload.id(REQUEST_POWER_ANIM_DATA), ModPacketsC2S::onRequestPowerAnimationData);
        ServerPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.UPLOAD_PATRON_AUTH_FILE), ModPacketsC2S::receivePatronAuthFile);
    }

    public static void sendDetachRequest(ServerPlayerEntity player) {
        PacketByteBuf buf = PacketByteBufs.create();
        ServerPlayNetworking.send(player, new BytePayload(BytePayload.id(JUMP_DETACH_REQUEST_ID),  buf));
    }

    private static void receivePatronAuthFile(BytePayload payload, ServerPlayNetworking.Context ctx) {
        byte[] data = payload.data().readByteArray();
        if (data != null) {
            ServerPlayerEntity player = ctx.player();
            if (player != null) {
                AuthServer.loadPatronAuthFile(player, new PacketByteBuf(Unpooled.wrappedBuffer(data)));
            }
        }
    }

    private static void onUpdatePlayerCustomConfig(BytePayload payload, ServerPlayNetworking.Context ctx) {
        PacketByteBuf buf = payload.data();
        boolean keepOriginalSkin = buf.readBoolean();
        boolean enableFormColor = buf.readBoolean();
        boolean enableFormRandomSound = buf.readBoolean();
        ServerPlayerEntity player = ctx.player();
        PlayerSkinComponent component = RegPlayerSkinComponent.SKIN_SETTINGS.get(player);
        component.setKeepOriginalSkin(keepOriginalSkin);
        component.setEnableFormColor(enableFormColor);
        component.setEnableFormRandomSound(enableFormRandomSound);
        RegPlayerSkinComponent.SKIN_SETTINGS.sync(player);
    }

    private static void onUpdatePlayerCustomColor(BytePayload payload, ServerPlayNetworking.Context ctx) {
        PacketByteBuf buf = payload.data();
        boolean extraData = buf.readBoolean();
        boolean keepOriginalSkin = false;
        boolean enableFormColorSystem = false;
        if (extraData) {
            keepOriginalSkin = buf.readBoolean();
            enableFormColorSystem = buf.readBoolean();
        }
        int primaryColor = buf.readInt();
        int accentColor1Color = buf.readInt();
        int accentColor2Color = buf.readInt();
        int eyeColorA = buf.readInt();
        int eyeColorB = buf.readInt();
        boolean primaryGreyReverse = buf.readBoolean();
        boolean accent1GreyReverse = buf.readBoolean();
        boolean accent2GreyReverse = buf.readBoolean();
        ServerPlayerEntity player = ctx.player();
        PlayerSkinComponent component = RegPlayerSkinComponent.SKIN_SETTINGS.get(player);
        if (extraData) {
            component.setKeepOriginalSkin(keepOriginalSkin);
            component.setEnableFormColor(enableFormColorSystem);
        }
        component.setFormColor(new FormTextureUtils.ColorSetting(primaryColor, accentColor1Color, accentColor2Color, eyeColorA, eyeColorB, primaryGreyReverse, accent1GreyReverse, accent2GreyReverse));
        RegPlayerSkinComponent.SKIN_SETTINGS.sync(player);
    }

    private static void receiveSetPatronForm(BytePayload payload, ServerPlayNetworking.Context ctx) {
        PacketByteBuf buf = payload.data();
        ServerPlayerEntity player = ctx.player();
        IForm form = RegPlayerForms.getPlayerForm(Identifier.tryParse(buf.readString()));
        if (form instanceof DynamicForm pfd && pfd.PlayerUUIDs.contains(player.getUuid())) {
            TransformManager.startTransform(player, form, null);
        }
    }

    private static void receiveSetForm(BytePayload payload, ServerPlayNetworking.Context ctx) {
        PacketByteBuf buf = payload.data();
        ServerPlayerEntity player = ctx.player();
        UUID target = buf.readUuid();
        Identifier formID = Identifier.tryParse(buf.readString());
        if (target.equals(player.getUuid()) || player.hasPermissionLevel(2)) {
            ServerPlayerEntity targetPlayer = player.getServer().getPlayerManager().getPlayer(target);
            if (targetPlayer != null) {
                IForm form = RegPlayerForms.getPlayerForm(formID);
                if (form != null) {
                    TransformManager.startTransform(targetPlayer, form, null);
                }
            }
        }
    }

    private static void onUpdatePowerAnimationData(BytePayload payload, ServerPlayNetworking.Context ctx) {
        PacketByteBuf buf = payload.data();
        ServerPlayerEntity player = ctx.player();
        Identifier animationId = buf.readBoolean() ? buf.readIdentifier() : null;
        int animationCount = buf.readInt();
        int animationLength = buf.readInt();
        if (player instanceof IPlayerAnimController animPlayer) {
            if (animationId == null) animPlayer.shape_shifter_curse$stopAnimation();
            else if (animationCount >= 0 && animationLength < 0) animPlayer.shape_shifter_curse$playAnimationWithCount(animationId, animationCount);
            else if (animationCount < 0 && animationLength >= 0) animPlayer.shape_shifter_curse$playAnimationWithTime(animationId, animationLength);
            else if (animationCount < 0 && animationLength < 0) animPlayer.shape_shifter_curse$playAnimationLoop(animationId);
            else ShapeShifterCurseFabric.LOGGER.error("Invalid animation data received from player: " + player.getUuidAsString());
        }
    }

    private static void onRequestPowerAnimationData(BytePayload payload, ServerPlayNetworking.Context ctx) {
        PacketByteBuf buf = payload.data();
        ServerPlayerEntity player = ctx.player();
        UUID targetPlayerUuid = buf.readUuid();
        PlayerEntity targetPlayer = player.getServer().getPlayerManager().getPlayer(targetPlayerUuid);
        if (targetPlayer instanceof IPlayerAnimController animPlayer) {
            ModPacketsS2CServer.sendPowerAnimationDataToClient(player, targetPlayerUuid,
                    animPlayer.shape_shifter_curse$getPowerAnimationID(),
                    animPlayer.shape_shifter_curse$getPowerAnimationCount(),
                    animPlayer.shape_shifter_curse$getPowerAnimationTime());
        }
    }

    /** Called from client initializer: registers C2S payload codecs so the client can send. */
    public static void registerClient() {
        BytePayload.registerC2S(VALIDATE_START_BOOK_BUTTON);
        BytePayload.registerC2S(Identifier.of(ShapeShifterCurseFabric.MOD_ID, "update_skin_setting"));
        BytePayload.registerC2S(JUMP_DETACH_REQUEST_ID);
        BytePayload.registerC2S(JUMP_EVENT_ID);
        BytePayload.registerC2S(SPRINTING_TO_SNEAKING_EVENT_ID);
        BytePayload.registerC2S(UPDATE_CUSTOM_SETTING);
        BytePayload.registerC2S(UPDATE_CUSTOM_COLOR);
        BytePayload.registerC2S(SET_PATRON_FORM);
        BytePayload.registerC2S(SET_FORM);
        BytePayload.registerC2S(UPDATE_POWER_ANIM_DATA_TO_SERVER);
        BytePayload.registerC2S(REQUEST_POWER_ANIM_DATA);
        BytePayload.registerC2S(ModPackets.UPLOAD_PATRON_AUTH_FILE);
    }

    private static void receiveSetForm(MinecraftServer minecraftServer, ServerPlayerEntity playerEntity, ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        UUID targetPlayerUuid = packetByteBuf.readUuid();
        PlayerEntity target = minecraftServer.getPlayerManager().getPlayer(targetPlayerUuid);
        if (target == null) {
            ShapeShifterCurseFabric.LOGGER.warn("[SetForm] Player {} not found", targetPlayerUuid);
        }
        Identifier formId = packetByteBuf.readIdentifier();
        IForm form = RegPlayerForms.getPlayerForm(formId);
        boolean immediately = packetByteBuf.readBoolean();
        // 网络包可以伪造 所以加个权限验证
        if (playerEntity.getCommandSource().hasPermissionLevel(2) || playerEntity.getAbilities().creativeMode) {
            minecraftServer.execute(() -> {
                if (target == null) {
                    ShapeShifterCurseFabric.LOGGER.warn("[SetForm] Player is null");
                    return;
                }
                TransformManager.forceTransform(target, form, immediately);
            });
            return;
        }
    }

    private static void receiveSetPatronForm(MinecraftServer minecraftServer, ServerPlayerEntity playerEntity, ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        if (!PatronUtils.EnablePatronFeature) {
            ShapeShifterCurseFabric.LOGGER.error("Player {} tried to use patron form but patron feature is disabled", playerEntity.getDisplayName().getString());
            return;
        }
        Identifier formId = packetByteBuf.readIdentifier();
        IForm form = RegPlayerForms.getPlayerForm(formId);

        if (minecraftServer.getCommandSource().hasPermissionLevel(2) || playerEntity.getAbilities().creativeMode) {
            // 权限等级2时跳过反作弊 毕竟可以用setForm了
            minecraftServer.execute(() -> {
                if (playerEntity == null) {
                    ShapeShifterCurseFabric.LOGGER.warn("[SetPatronForm] Player is null");
                    return;
                }
                TransformManager.forceTransform(playerEntity, form, false);
            });
            return;
        }
        if (form instanceof DynamicForm pfd) {
            minecraftServer.execute(() -> {
                if (playerEntity == null) {
                    ShapeShifterCurseFabric.LOGGER.warn("[SetPatronForm] Player is null");
                    return;
                }
                if (pfd.IsPlayerCanUse(playerEntity)) {
                    TransformManager.forceTransform(playerEntity, pfd, false);
                }
                else {
                    // 一般情况下，这里不会执行，因为客户端在发送请求前已经进行了检查 如果触发了这里，说明客户端和服务器之间的数据不同步(小概率 如果不同步早就掉线了) 或者是客户端作弊(大概率)
                    ShapeShifterCurseFabric.LOGGER.warn("Player {} tried to use form {} but they are not allowed", playerEntity.getDisplayName().getString(), formId.toString());
                }
            });
        }
        else if (form != null){
            // 如果是已发布版本 100% 是客户端作弊 一般只会在测试时触发(因为测试版需要填充所有表单用来测试UI)
            ShapeShifterCurseFabric.LOGGER.warn("Player {} tried to use form {} but it is not a dynamic form", playerEntity.getDisplayName().getString(), formId.toString());
        }
        else {
            // 可能是不同步问题
            ShapeShifterCurseFabric.LOGGER.warn("Player {} tried to use form {} but it does not exist", playerEntity.getDisplayName().getString(), formId.toString());
        }
        return;
    }

    private static void receivePatronAuthFile(MinecraftServer minecraftServer, ServerPlayerEntity playerEntity, ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        byte[] data = packetByteBuf.readByteArray();
        if (data != null) {
            minecraftServer.execute(() -> {
                AuthServer.loadPatronAuthFile(playerEntity, new PacketByteBuf(Unpooled.wrappedBuffer(data)));
            });
        }
    }
}

