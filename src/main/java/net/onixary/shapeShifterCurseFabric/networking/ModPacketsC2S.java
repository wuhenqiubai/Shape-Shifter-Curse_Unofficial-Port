package net.onixary.shapeShifterCurseFabric.networking;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
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
            ServerPlayer player = ctx.player();
            if (player != null && RegPlayerForms.ORIGINAL_BEFORE_ENABLE.isPlayerForm(player)) {
                TransformManager.startTransform(player, RegPlayerForms.ORIGINAL_SHIFTER, null);
                ShapeShifterCurseFabric.ON_ENABLE_MOD.trigger(player);
                player.sendSystemMessage(Component.translatable("info.shape-shifter-curse.on_enable_mod").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(BytePayload.id(ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "update_skin_setting")), (payload, ctx) -> {
            FriendlyByteBuf buf = payload.data();
            boolean keepOriginalSkin = buf.readBoolean();
            ServerPlayer player = ctx.player();
            PlayerSkinComponent skinComp = RegPlayerSkinComponent.SKIN_SETTINGS.get(player);
            skinComp.setKeepOriginalSkin(keepOriginalSkin);
            RegPlayerSkinComponent.SKIN_SETTINGS.sync(player);
        });
        ServerPlayNetworking.registerGlobalReceiver(BytePayload.id(JUMP_DETACH_REQUEST_ID), (payload, ctx) -> {
            ServerPlayer player = ctx.player();
            BatBlockAttachPower attachPower = PowerHolderComponent.getPowers(player, BatBlockAttachPower.class)
                    .stream().filter(BatBlockAttachPower::isAttached).findFirst().orElse(null);
            if (attachPower != null) attachPower.handleJump(player);
        });
        ServerPlayNetworking.registerGlobalReceiver(BytePayload.id(JUMP_EVENT_ID), (payload, ctx) -> {
            FriendlyByteBuf buf = payload.data();
            UUID playerUuid = buf.readUUID();
            ServerPlayer player = ctx.player();
            if (player.getUUID().equals(playerUuid)) JumpEventCondition.setJumping(player, true);
            PowerHolderComponent.getPowers(player, ActionOnJumpPower.class).forEach(ActionOnJumpPower::executeAction);
        });
        ServerPlayNetworking.registerGlobalReceiver(BytePayload.id(SPRINTING_TO_SNEAKING_EVENT_ID), (payload, ctx) -> {
            FriendlyByteBuf buf = payload.data();
            UUID playerUuid = buf.readUUID();
            ServerPlayer player = ctx.player();
            if (player.getUUID().equals(playerUuid)) {
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

    public static void sendDetachRequest(ServerPlayer player) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        ServerPlayNetworking.send(player, new BytePayload(BytePayload.id(JUMP_DETACH_REQUEST_ID),  buf));
    }

    private static void onUpdatePlayerCustomConfig(BytePayload payload, ServerPlayNetworking.Context ctx) {
        FriendlyByteBuf buf = payload.data();
        boolean keepOriginalSkin = buf.readBoolean();
        boolean enableFormColor = buf.readBoolean();
        boolean enableFormRandomSound = buf.readBoolean();
        ServerPlayer player = ctx.player();
        PlayerSkinComponent component = RegPlayerSkinComponent.SKIN_SETTINGS.get(player);
        component.setKeepOriginalSkin(keepOriginalSkin);
        component.setEnableFormColor(enableFormColor);
        component.setEnableFormRandomSound(enableFormRandomSound);
        RegPlayerSkinComponent.SKIN_SETTINGS.sync(player);
    }

    private static void onUpdatePlayerCustomColor(BytePayload payload, ServerPlayNetworking.Context ctx) {
        FriendlyByteBuf buf = payload.data();
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
        ServerPlayer player = ctx.player();
        PlayerSkinComponent component = RegPlayerSkinComponent.SKIN_SETTINGS.get(player);
        if (extraData) {
            component.setKeepOriginalSkin(keepOriginalSkin);
            component.setEnableFormColor(enableFormColorSystem);
        }
        component.setFormColor(new FormTextureUtils.ColorSetting(primaryColor, accentColor1Color, accentColor2Color, eyeColorA, eyeColorB, primaryGreyReverse, accent1GreyReverse, accent2GreyReverse));
        RegPlayerSkinComponent.SKIN_SETTINGS.sync(player);
    }

    private static void onUpdatePowerAnimationData(BytePayload payload, ServerPlayNetworking.Context ctx) {
        FriendlyByteBuf buf = payload.data();
        ServerPlayer player = ctx.player();
        ResourceLocation animationId = buf.readBoolean() ? buf.readResourceLocation() : null;
        int animationCount = buf.readInt();
        int animationLength = buf.readInt();
        if (player instanceof IPlayerAnimController animPlayer) {
            if (animationId == null) animPlayer.shape_shifter_curse$stopAnimation();
            else if (animationCount >= 0 && animationLength < 0) animPlayer.shape_shifter_curse$playAnimationWithCount(animationId, animationCount);
            else if (animationCount < 0 && animationLength >= 0) animPlayer.shape_shifter_curse$playAnimationWithTime(animationId, animationLength);
            else if (animationCount < 0 && animationLength < 0) animPlayer.shape_shifter_curse$playAnimationLoop(animationId);
            else ShapeShifterCurseFabric.LOGGER.error("Invalid animation data received from player: " + player.getStringUUID());
        }
    }

    private static void onRequestPowerAnimationData(BytePayload payload, ServerPlayNetworking.Context ctx) {
        FriendlyByteBuf buf = payload.data();
        ServerPlayer player = ctx.player();
        UUID targetPlayerUuid = buf.readUUID();
        Player targetPlayer = player.getServer().getPlayerList().getPlayer(targetPlayerUuid);
        if (targetPlayer instanceof IPlayerAnimController animPlayer) {
            ModPacketsS2CServer.sendPowerAnimationDataToClient(player, targetPlayerUuid,
                    animPlayer.shape_shifter_curse$getPowerAnimationID(),
                    animPlayer.shape_shifter_curse$getPowerAnimationCount(),
                    animPlayer.shape_shifter_curse$getPowerAnimationTime());
        }
    }

    private static void receiveSetForm(BytePayload payload, ServerPlayNetworking.Context ctx) {
        FriendlyByteBuf buf = payload.data();
        ServerPlayer player = ctx.player();
        UUID target = buf.readUUID();
        ResourceLocation formID = ResourceLocation.tryParse(buf.readUtf());
        if (target.equals(player.getUUID()) || player.hasPermissions(2)) {
            ServerPlayer targetPlayer = player.getServer().getPlayerList().getPlayer(target);
            if (targetPlayer != null) {
                IForm form = RegPlayerForms.getPlayerForm(formID);
                if (form != null) {
                    TransformManager.startTransform(targetPlayer, form, null);
                }
            }
        }
    }

    private static void receiveSetPatronForm(BytePayload payload, ServerPlayNetworking.Context ctx) {
        FriendlyByteBuf buf = payload.data();
        ServerPlayer player = ctx.player();
        IForm form = RegPlayerForms.getPlayerForm(ResourceLocation.tryParse(buf.readUtf()));
        if (form instanceof DynamicForm pfd && pfd.PlayerUUIDs.contains(player.getUUID())) {
            TransformManager.startTransform(player, form, null);
        }
    }

    private static void receivePatronAuthFile(BytePayload payload, ServerPlayNetworking.Context ctx) {
        byte[] data = payload.data().readByteArray();
        if (data != null) {
            ServerPlayer player = ctx.player();
            if (player != null) {
                AuthServer.loadPatronAuthFile(player, new FriendlyByteBuf(Unpooled.wrappedBuffer(data)));
            }
        }
    }

    /** Called from client initializer: registers C2S payload codecs so the client can send. */
    public static void registerClient() {
        BytePayload.registerC2S(VALIDATE_START_BOOK_BUTTON);
        BytePayload.registerC2S(ResourceLocation.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "update_skin_setting"));
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
}