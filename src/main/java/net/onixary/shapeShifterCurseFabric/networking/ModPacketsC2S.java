package net.onixary.shapeShifterCurseFabric.networking;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.additional_power.ActionOnJumpPower;
import net.onixary.shapeShifterCurseFabric.additional_power.ActionOnSprintingToSneakingPower;
import net.onixary.shapeShifterCurseFabric.additional_power.BatBlockAttachPower;
import net.onixary.shapeShifterCurseFabric.additional_power.JumpEventCondition;
import net.onixary.shapeShifterCurseFabric.perk.PerkUtils;
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
        ServerPlayNetworking.registerGlobalReceiver(BytePayload.id(Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "update_skin_setting")), (payload, ctx) -> {
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

        // jump_event condition handle
        ServerPlayNetworking.registerGlobalReceiver(BytePayload.id(JUMP_EVENT_ID), (payload, ctx) -> ctx.server().execute(() -> {
            // 在服务器端设置跳跃状态
            JumpEventCondition.setJumping(ctx.player(), true);
            PowerHolderComponent.getPowers(ctx.player(), ActionOnJumpPower.class).forEach(ActionOnJumpPower::executeAction);
        }));

        // SPRINTING_TO_SNEAKING_EVENT condition handle
        ServerPlayNetworking.registerGlobalReceiver(BytePayload.id(SPRINTING_TO_SNEAKING_EVENT_ID), (payload, ctx) -> ctx.server().execute(() -> {
            // 在服务器端处理疾跑转潜行事件
            PowerHolderComponent.getPowers(ctx.player(), ActionOnSprintingToSneakingPower.class).forEach(ActionOnSprintingToSneakingPower::executeAction);
        }));

        ServerPlayNetworking.registerGlobalReceiver(
                BytePayload.id(UPDATE_CUSTOM_SETTING),
                ModPacketsC2S::onUpdatePlayerCustomConfig
        );


        ServerPlayNetworking.registerGlobalReceiver(
                BytePayload.id(UPDATE_CUSTOM_COLOR),
                net.onixary.shapeShifterCurseFabric.networking.ModPacketsC2S::onUpdatePlayerCustomColor
        );

        ServerPlayNetworking.registerGlobalReceiver(
                BytePayload.id(SET_PATRON_FORM),
                net.onixary.shapeShifterCurseFabric.networking.ModPacketsC2S::receiveSetPatronForm
        );

        ServerPlayNetworking.registerGlobalReceiver(
                BytePayload.id(SET_FORM),
                net.onixary.shapeShifterCurseFabric.networking.ModPacketsC2S::receiveSetForm
        );

        ServerPlayNetworking.registerGlobalReceiver(
                BytePayload.id(UPDATE_POWER_ANIM_DATA_TO_SERVER),
                ModPacketsC2S::onUpdatePowerAnimationData
        );

        ServerPlayNetworking.registerGlobalReceiver(
                BytePayload.id(REQUEST_POWER_ANIM_DATA),
                ModPacketsC2S::onRequestPowerAnimationData
        );

        ServerPlayNetworking.registerGlobalReceiver(
                BytePayload.id(UPLOAD_PATRON_AUTH_FILE),
                ModPacketsC2S::receivePatronAuthFile
        );

        ServerPlayNetworking.registerGlobalReceiver(
                BytePayload.id(ADD_PERK),
                ModPacketsC2S::receiveAddPerk
        );
    }

    /**
     * 上游（1.20.1）在 {@code register()} 里把 {@code VALIDATE_START_BOOK_BUTTON} 接到本方法；
     * 1.21.1 移植时改成了在 {@code register()} 里直接写 lambda，于是本方法不再被注册、成为死代码，
     * 仅留作参考。原方法末尾还挂着 8 行 {@code registerGlobalReceiver}（与 {@code register()} 里的
     * 那批完全重复）——那是内联时漏删的残留，一旦有人把本方法重新接上，Fabric 会因重复注册抛
     * {@code IllegalArgumentException} 直接崩服，故已删除。
     */
    private static void onPressStartBookButton(BytePayload payload, ServerPlayNetworking.Context ctx) {
        // 就凭这个网络Bug 我就可以做一个可以直接还原形态的作弊客户端 还可以给其他玩家还原 不知道为什么要往buf里写uuid
        // UUID playerUuid = packetByteBuf.readUuid();
        ctx.server().execute(() -> {
            // 通过 UUID 获取玩家实例
            // ServerPlayerEntity targetPlayer = minecraftServer.getPlayerManager().getPlayer(playerUuid);
            if (ctx.player() != null && RegPlayerForms.ORIGINAL_BEFORE_ENABLE.isPlayerForm(ctx.player())) {
                TransformManager.startTransform(ctx.player(), RegPlayerForms.ORIGINAL_SHIFTER, null);
                ShapeShifterCurseFabric.ON_ENABLE_MOD.trigger(ctx.player());
                ctx.player().sendSystemMessage(Component.translatable("info.shape-shifter-curse.on_enable_mod").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        });
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
        Identifier animationId = buf.readBoolean() ? buf.readIdentifier() : null;
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
        Player targetPlayer = player.level().getServer().getPlayerList().getPlayer(targetPlayerUuid);
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
        Identifier formID = Identifier.tryParse(buf.readUtf());
        if (target.equals(player.getUUID()) || player.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(2)))) {
            ServerPlayer targetPlayer = player.level().getServer().getPlayerList().getPlayer(target);
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
        IForm form = RegPlayerForms.getPlayerForm(Identifier.tryParse(buf.readUtf()));
        if (form instanceof DynamicForm pfd && pfd.PlayerUUIDs.contains(player.getUUID())) {
            TransformManager.startTransform(player, form, null);
        }
    }

    private static void receivePatronAuthFile(BytePayload payload, ServerPlayNetworking.Context ctx) {
        byte[] data = payload.data().readByteArray();
        if (data != null) {
            ctx.server().execute(() -> {
                AuthServer.loadPatronAuthFile(ctx.player(), new FriendlyByteBuf(Unpooled.wrappedBuffer(data)));
            });
        }
    }

    private static void receiveAddPerk(BytePayload payload, ServerPlayNetworking.Context ctx) {
        Identifier perkTreeId = payload.data().readIdentifier();
        Identifier perkId = payload.data().readIdentifier();
        ctx.server().execute(() -> {
            PerkUtils.addPerkFromClient(ctx.player(), perkTreeId, perkId);
        });
    }

    /** Called from client initializer: registers C2S payload codecs so the client can send. */
    public static void registerClient() {
        BytePayload.registerC2S(VALIDATE_START_BOOK_BUTTON);
        BytePayload.registerC2S(Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, "update_skin_setting"));
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
        BytePayload.registerC2S(ModPackets.ADD_PERK);
    }
}