package net.onixary.shapeShifterCurseFabric.networking;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.additional_power.BatBlockAttachPower;
import net.onixary.shapeShifterCurseFabric.additional_power.VirtualTotemPower;
import net.onixary.shapeShifterCurseFabric.client.ClientPlayerStateManager;
import net.onixary.shapeShifterCurseFabric.client.ShapeShifterCurseFabricClient;
import net.onixary.shapeShifterCurseFabric.cursed_moon.CursedMoonClient;
import net.onixary.shapeShifterCurseFabric.custom_ui.FormColorSelectMenu;
import net.onixary.shapeShifterCurseFabric.custom_ui.FormColorSelectMenuV2;
import net.onixary.shapeShifterCurseFabric.custom_ui.NormalFormSelectScreen;
import net.onixary.shapeShifterCurseFabric.custom_ui.PatronFormSelectScreen;
import net.onixary.shapeShifterCurseFabric.data.StaticParams;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.IPlayerAnimController;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.utils.TransformManager;
import net.onixary.shapeShifterCurseFabric.screen_effect.TransformOverlay;
import net.onixary.shapeShifterCurseFabric.util.FormColorData;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;
import net.onixary.shapeShifterCurseFabric.util.Interface.IJumpController;
import net.onixary.shapeShifterCurseFabric.util.Interface.IMoveController;
import net.onixary.shapeShifterCurseFabric.util.PatronUtils;
import net.onixary.shapeShifterCurseFabric.util.SuperUserUtils;
import net.onixary.shapeShifterCurseFabric.util.Verify.AuthClient;
import net.onixary.shapeShifterCurseFabric.util.Verify.AuthFile;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static net.onixary.shapeShifterCurseFabric.networking.ModPackets.*;
import static net.onixary.shapeShifterCurseFabric.screen_effect.TransformFX.beginTransformEffect;

// 应仅在客户端注册
// This class should only be registered on the client side
// 纯客户端类，所有的receive方法都只在这里调用
// This is a pure client-side class, all receive methods are called only here
public class ModPacketsS2C {

    public static void register() {
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
        BytePayload.registerS2C(ModPackets.UPDATE_PATRON_LEVEL);
        BytePayload.registerS2C(ModPackets.OPEN_PATRON_FORM_SELECT_MENU);
        BytePayload.registerS2C(ModPackets.OPEN_FORM_SELECT_MENU);
        BytePayload.registerS2C(ModPackets.SET_NO_JUMP_TICK);
        BytePayload.registerS2C(ModPackets.SET_NO_MOVE_TICK);
        BytePayload.registerS2C(ModPackets.OPEN_FORM_COLOR_SELECT_MENU);
        BytePayload.registerS2C(ModPackets.MODIFY_FCD_DATA);
        BytePayload.registerS2C(ModPackets.MELT_AUTH_SUB_KEY);
        BytePayload.registerS2C(ModPackets.REQUEST_PATRON_AUTH_FILE);
        BytePayload.registerS2C(ModPackets.SET_SUPER_USER_LEVEL);
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.SYNC_CURSED_MOON_DATA), ModPacketsS2C::receiveCursedMoonData);
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.SYNC_FORM_CHANGE), ModPacketsS2C::receiveFormChange);
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.SYNC_TRANSFORM_STATE), ModPacketsS2C::receiveTransformState);
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.SYNC_BAT_ATTACH_STATE), ModPacketsS2C::receiveBatAttachState);
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.RESET_FIRST_PERSON), ModPacketsS2C::receiveResetFirstPerson);
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.SYNC_OTHER_PLAYER_BAT_ATTACH_STATE), ModPacketsS2C::receiveOtherPlayerBatAttachState);
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.SYNC_FORCE_SNEAK_STATE), ModPacketsS2C::receiveForceSneakState);
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.UPDATE_DYNAMIC_FORM), ModPacketsS2C::handleUpdateDynamicForm);
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.REMOVE_DYNAMIC_FORM_EXCEPT), ModPacketsS2C::handleRemoveDynamicExcept);
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.LOGIN_PACKET), ModPacketsS2C::onPlayerConnectServer);
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.ACTIVE_VIRTUAL_TOTEM), ModPacketsS2C::receiveActiveVirtualTotem);
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.UPDATE_POWER_ANIM_DATA_TO_CLIENT), ModPacketsS2C::receivePowerAnimationData);
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.UPDATE_PATRON_LEVEL), ModPacketsS2C::receiveUpdatePatronLevel);
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.OPEN_PATRON_FORM_SELECT_MENU), ModPacketsS2C::receiveOpenPatronFormSelectMenu);
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.OPEN_FORM_SELECT_MENU), ModPacketsS2C::receiveOpenFormSelectMenu);
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.SET_NO_JUMP_TICK), ModPacketsS2C::receiveSetNoJumpTick);
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.SET_NO_MOVE_TICK), ModPacketsS2C::receiveSetNoMoveTick);
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.OPEN_FORM_COLOR_SELECT_MENU), ModPacketsS2C::receiveOpenFCSMenu);
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.MODIFY_FCD_DATA), ModPacketsS2C::receiveModifyFCDData);
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.REQUEST_PATRON_AUTH_FILE), ModPacketsS2C::receiveRequestPatronAuthFile);
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.MELT_AUTH_SUB_KEY), ModPacketsS2C::receiveNewSubKey);
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.SET_SUPER_USER_LEVEL), ModPacketsS2C::receiveSetSuperUserLevel);
    }

    /* 重构后不需要了 仅用于参考旧实现逻辑
    public static void handleSyncEffectAttachment(
		MinecraftClient client,
		ClientPlayNetworkHandler handler,
		PacketByteBuf buf,
		PacketSender sender
	) {
        // 从数据包读取NBT
        NbtCompound nbt = buf.readNbt();
        client.execute(() -> {
            // 更新客户端缓存
            ClientEffectAttachmentCache.update(nbt);
        });
    }
     */

    public static void receiveCursedMoonData(BytePayload payload, ClientPlayNetworking.Context ctx) {
        boolean isCursedMoon = payload.data().readBoolean();
        ctx.client().execute(() -> {
            CursedMoonClient.isCursedMoon = isCursedMoon;
            CursedMoonClient.middayMessageSent = false;
        });
    }

    /**
     * 接收形态变化同步包
     */
    public static void receiveFormChange(BytePayload payload, ClientPlayNetworking.Context ctx) {
        Identifier newFormID = payload.data().readIdentifier();

        ctx.client().execute(() -> {
            if (ctx.client().player != null) {
                // 更新 formColorData 的数据(其实是FormColorSelectMenu的数据) 如果启动了自动切换 那么还会自动切换颜色数据
                ShapeShifterCurseFabricClient.formColorData.onClientFormChange(newFormID);
            }
        });
    }

    /**
     * 接收变身状态同步包
     */
    public static void receiveTransformState(BytePayload payload, ClientPlayNetworking.Context ctx) {
        UUID playerUuid = payload.data().readUUID();
        boolean isTransforming = payload.data().readBoolean();
        String fromForm = payload.data().readUtf();
        String toForm = payload.data().readUtf();

        ctx.client().execute(() -> {
            if (ctx.client().player != null) {
                ShapeShifterCurseFabricClient.updateTransformState(playerUuid, isTransforming, fromForm.isEmpty() ? null : fromForm, toForm.isEmpty() ? null : toForm);
                if (ctx.client().player.getUUID().equals(playerUuid)) {
                    if (isTransforming) {
                        TransformManager.transformTimer = 0;
                        ShapeShifterCurseFabricClient.emitTransformParticle(StaticParams.TRANSFORM_FX_DURATION_IN);
                        beginTransformEffect();
                        TransformOverlay.INSTANCE.setEnableOverlay(true);
                    } else {
                        TransformManager.transformTimer = -1;
                        TransformOverlay.INSTANCE.setEnableOverlay(false);
                    }
                }
            }
        });
    }

    /**
     * 接收FirstPerson重置包
     */
    public static void receiveResetFirstPerson(BytePayload payload, ClientPlayNetworking.Context ctx) {
        ctx.client().execute(TransformManager::executeClientFirstPersonReset);
    }

    /**
     * 接收蝙蝠吸附状态同步包
     */
    public static void receiveBatAttachState(BytePayload payload, ClientPlayNetworking.Context ctx) {
        boolean isAttached = payload.data().readBoolean();
        int attachTypeOrdinal = payload.data().readInt();

        BlockPos attachedPos;
        if (payload.data().readBoolean()) {
            attachedPos = payload.data().readBlockPos();
        } else {
            attachedPos = null;
        }

        Direction attachedSide;
        if (payload.data().readBoolean()) {
            attachedSide = Direction.from3DDataValue(payload.data().readInt());
        } else {
            attachedSide = null;
        }

        ctx.client().execute(() -> {
            if (ctx.client().player != null) {
                // 获取客户端的BatBlockAttachPower并同步状态
                BatBlockAttachPower.syncClientState(ctx.client().player, isAttached, attachTypeOrdinal, attachedPos, attachedSide);
            }
        });
    }

    /**
     * 接收其他玩家的蝙蝠吸附状态同步包
     */
    public static void receiveOtherPlayerBatAttachState(BytePayload payload, ClientPlayNetworking.Context ctx) {
        UUID targetPlayerUuid = payload.data().readUUID();
        boolean isAttached = payload.data().readBoolean();
        int attachType = payload.data().readInt();

        BlockPos attachedPos;
        Direction attachedSide;

        if (payload.data().readBoolean()) {
            attachedPos = payload.data().readBlockPos();
        } else {
            attachedPos = null;
        }

        if (payload.data().readBoolean()) {
            attachedSide = Direction.from3DDataValue(payload.data().readInt());
        } else {
            attachedSide = null;
        }

        ctx.client().execute(() -> {
            ClientPlayerStateManager.updatePlayerAttachState(targetPlayerUuid, isAttached,
                    attachType, attachedPos, attachedSide);
        });
    }

    private static void receiveForceSneakState(BytePayload payload, ClientPlayNetworking.Context ctx) {
        boolean shouldForce = payload.data().readBoolean();
        ctx.client().execute(() -> {
            ClientPlayerStateManager.shouldForceSneak = shouldForce;
        });
    }

    private static void handleUpdateDynamicForm(BytePayload payload, ClientPlayNetworking.Context ctx) {
        // 读取String -> JsonObject
        JsonObject allFrom = new JsonObject();
        int formCount = payload.data().readInt();
        for (int i = 0; i < formCount; i++) {
            String formName = payload.data().readUtf();
            String jsonStr = payload.data().readUtf();
            JsonObject jsonObject = new Gson().fromJson(jsonStr, JsonObject.class);
            allFrom.add(formName, jsonObject);
        }
        ctx.client().execute(() -> {
            RegPlayerForms.ApplyDynamicPlayerForms(allFrom);
        });
    }

    private static void handleRemoveDynamicExcept(BytePayload payload, ClientPlayNetworking.Context ctx) {
        // 读取String -> JsonObject
        List<Identifier> except = new ArrayList<>();
        int formCount = payload.data().readInt();
        for (int i = 0; i < formCount; i++) {
            String formName = payload.data().readUtf();
            except.add(Identifier.tryParse(formName));
        }
        ctx.client().execute(() -> {
            RegPlayerForms.removeDynamicPlayerFormsExcept(except);
        });
    }

    public static void onPlayerConnectServer(BytePayload payload, ClientPlayNetworking.Context ctx) {
        // 还原FPM设置 或许可以通过注入式修改配置来减少此类Bug 比如在FPM读取offset时修改返回值
        TransformManager.executeClientFirstPersonReset();
        new Thread(() -> {
            // 延时5s, 等待服务器component加载完成 重复12次 共计1min
            for (int i = 0; i < 60; i++) {
                try {
                    Thread.sleep(1000);
                    sendUpdateCustomSetting();
                    return;
                } catch (Exception ignored) {
                }
            }
            ShapeShifterCurseFabric.LOGGER.error("Failed to send custom setting to server after 60 seconds");
        }).start();
    }

    public static void sendUpdateCustomColor(FormTextureUtils.ColorSetting colorSetting, boolean sendRAW, boolean sendExtraData, boolean keepOriginalSkin, boolean enableFormColorSystem) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(sendExtraData);
        if (sendExtraData) {
            buf.writeBoolean(keepOriginalSkin);
            buf.writeBoolean(enableFormColorSystem);
        }
        if (sendRAW) {
            buf.writeInt(colorSetting.getPrimaryColor());
            buf.writeInt(colorSetting.getAccentColor1());
            buf.writeInt(colorSetting.getAccentColor2());
            buf.writeInt(colorSetting.getEyeColorA());
            buf.writeInt(colorSetting.getEyeColorB());
        } else {
            buf.writeInt(FormTextureUtils.ARGB2ABGR(colorSetting.getPrimaryColor()));
            buf.writeInt(FormTextureUtils.ARGB2ABGR(colorSetting.getAccentColor1()));
            buf.writeInt(FormTextureUtils.ARGB2ABGR(colorSetting.getAccentColor2()));
            buf.writeInt(FormTextureUtils.ARGB2ABGR(colorSetting.getEyeColorA()));
            buf.writeInt(FormTextureUtils.ARGB2ABGR(colorSetting.getEyeColorB()));
        }
        buf.writeBoolean(colorSetting.getPrimaryGreyReverse());
        buf.writeBoolean(colorSetting.getAccent1GreyReverse());
        buf.writeBoolean(colorSetting.getAccent2GreyReverse());
        ClientPlayNetworking.send(new BytePayload(BytePayload.id(UPDATE_CUSTOM_COLOR),  buf));
    }

    // 临时先放这里，以后再整理
    public static void sendUpdateCustomSetting(boolean ForceUpdate) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        boolean autoSyncConfig = ShapeShifterCurseFabric.playerCustomConfig.auto_sync_config;
        if (!ForceUpdate && !autoSyncConfig) {
            return;
        }
        buf.writeBoolean(ShapeShifterCurseFabric.playerCustomConfig.keep_original_skin);
        buf.writeBoolean(ShapeShifterCurseFabric.playerCustomConfig.enable_form_color);
        buf.writeBoolean(ShapeShifterCurseFabric.playerCustomConfig.enable_form_random_sound);
        ClientPlayNetworking.send(new BytePayload(BytePayload.id(UPDATE_CUSTOM_SETTING),  buf));
        boolean autoSyncColorConfig = ShapeShifterCurseFabric.playerCustomConfig.auto_sync_color_config;
        if (!ForceUpdate && !autoSyncColorConfig) {
            return;
        }
        buf = PacketByteBufs.create();
        buf.writeBoolean(false);
        int AGBRInt = 0;
        AGBRInt = FormTextureUtils.ARGB2ABGR(ShapeShifterCurseFabric.playerCustomConfig.primaryColor);
        buf.writeInt(AGBRInt);
        AGBRInt = FormTextureUtils.ARGB2ABGR(ShapeShifterCurseFabric.playerCustomConfig.accentColor1Color);
        buf.writeInt(AGBRInt);
        AGBRInt = FormTextureUtils.ARGB2ABGR(ShapeShifterCurseFabric.playerCustomConfig.accentColor2Color);
        buf.writeInt(AGBRInt);
        AGBRInt = FormTextureUtils.ARGB2ABGR(ShapeShifterCurseFabric.playerCustomConfig.eyeColorA);
        buf.writeInt(AGBRInt);
        AGBRInt = FormTextureUtils.ARGB2ABGR(ShapeShifterCurseFabric.playerCustomConfig.eyeColorB);
        buf.writeInt(AGBRInt);
        buf.writeBoolean(ShapeShifterCurseFabric.playerCustomConfig.primaryGreyReverse);
        buf.writeBoolean(ShapeShifterCurseFabric.playerCustomConfig.accent1GreyReverse);
        buf.writeBoolean(ShapeShifterCurseFabric.playerCustomConfig.accent2GreyReverse);
        ClientPlayNetworking.send(new BytePayload(BytePayload.id(UPDATE_CUSTOM_COLOR),  buf));
    }

    public static void sendUpdateCustomSetting() {
        sendUpdateCustomSetting(false);
    }

    public static void receiveActiveVirtualTotem(BytePayload payload, ClientPlayNetworking.Context ctx) {
        // LivingEntity entity, int virtualTotemType, ItemStack totemStack
        if (ctx.client().level == null) {
            ShapeShifterCurseFabric.LOGGER.error("World is null when receiving active virtual totem packet");
            return;
        }
        Player playerEntity = ctx.client().level.getPlayerByUUID(payload.data().readUUID());
        if (playerEntity == null) {
            ShapeShifterCurseFabric.LOGGER.warn("Can't find player entity when receiving active virtual totem packet");
            return;
        }
        Identifier virtualTotemType = payload.data().readIdentifier();
        RegistryFriendlyByteBuf regBuf = new RegistryFriendlyByteBuf(payload.data(), ctx.client().level.registryAccess());
        ItemStack totemStack = ItemStack.OPTIONAL_STREAM_CODEC.decode(regBuf);
        // ConcurrentModificationException 需要把这个操作放到Client线程而非Network线程
        ctx.client().execute(() -> VirtualTotemPower.process_virtual_totem_type(playerEntity, virtualTotemType, totemStack));
    }

    public static void receivePowerAnimationData(BytePayload payload, ClientPlayNetworking.Context ctx) {
        UUID playerUuid = payload.data().readUUID();
        @Nullable Identifier animationId;
        if (payload.data().readBoolean()) {
            animationId = payload.data().readIdentifier();
        }
        else {
            animationId = null;
        }
        int animationCount = payload.data().readInt();
        int animationLength = payload.data().readInt();
        if (ctx.client().level == null) {
            ShapeShifterCurseFabric.LOGGER.error("World is null when receiving update power anim data packet");
            return;
        }
        Player playerEntity = ctx.client().level.getPlayerByUUID(playerUuid);
        if (playerEntity == null) {
            ShapeShifterCurseFabric.LOGGER.warn("Can't find player entity when receiving update power anim data packet");
            return;
        }
        // ShapeShifterCurseFabric.LOGGER.info("Received power animation data for player " + playerUuid + " animationId " + animationId + " animationCount " + animationCount + " animationLength " + animationLength);
        ctx.client().execute(() -> {
            if (playerEntity instanceof IPlayerAnimController animPlayer) {
                animPlayer.shape_shifter_curse$setAnimationData(animationId, animationCount, animationLength);
            } else {
                ShapeShifterCurseFabric.LOGGER.error("Player {} is not a IPlayerAnimController when receiving update power anim data packet", playerEntity.getName());
            }
        });
    }

    public static void sendPowerAnimationDataToServer(@Nullable Identifier animationId, int animationCount, int animationLength) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        if (animationId != null) {
            buf.writeBoolean(true);
            buf.writeIdentifier(animationId);
        }
        else {
            buf.writeBoolean(false);
        }
        buf.writeInt(animationCount);
        buf.writeInt(animationLength);
        ClientPlayNetworking.send(new BytePayload(BytePayload.id(UPDATE_POWER_ANIM_DATA_TO_SERVER),  buf));
    }

    public static void sendRequestPlayerAnimationData(UUID targetPlayerUUID) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUUID(targetPlayerUUID);
        ClientPlayNetworking.send(new BytePayload(BytePayload.id(REQUEST_POWER_ANIM_DATA),  buf));
    }

    public static void receiveUpdatePatronLevel(BytePayload payload, ClientPlayNetworking.Context ctx) {
        int PairCount = payload.data().readInt();
        HashMap<UUID, Integer> map = new HashMap<>();
        for (int i = 0; i < PairCount; i++) {
            UUID uuid = payload.data().readUUID();
            int level = payload.data().readInt();
            map.put(uuid, level);
        }
        ctx.client().execute(() -> {
            PatronUtils.ApplyPatronLevel(map);
        });
    }

    public static void receiveOpenPatronFormSelectMenu(BytePayload payload, ClientPlayNetworking.Context ctx) {
        ctx.client().execute(() -> {
            Screen screen = new PatronFormSelectScreen(Component.literal("PatronFromSelectScreen"), ctx.client().player);
            ctx.client().setScreen(screen);
        });
    }

    public static void receiveOpenFormSelectMenu(BytePayload payload, ClientPlayNetworking.Context ctx) {
        String targetName = payload.data().readUtf();
        UUID targetUUID = payload.data().readUUID();
        ctx.client().execute(() -> {
            Screen screen = new NormalFormSelectScreen(Component.literal("FormSelectScreen"), targetName, targetUUID);
            ctx.client().setScreen(screen);
        });
    }

    public static void sendSetPatronForm(Identifier formID) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeIdentifier(formID);
        ClientPlayNetworking.send(new BytePayload(BytePayload.id(SET_PATRON_FORM),  buf));
    }

    public static void sendSetForm(Identifier formID, UUID target, boolean immediate) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUUID(target);
        buf.writeIdentifier(formID);
        buf.writeBoolean(immediate);
        ClientPlayNetworking.send(new BytePayload(BytePayload.id(SET_FORM),  buf));
    }

    public static void receiveSetNoJumpTick(BytePayload payload, ClientPlayNetworking.Context ctx) {
        int tick = payload.data().readInt();
        ctx.client().execute(() -> {
            if (ctx.client().player instanceof IJumpController jumpController) {
                jumpController.shape_shifter_curse$setNoJumpTick(tick);
            }
        });
    }

    public static void receiveSetNoMoveTick(BytePayload payload, ClientPlayNetworking.Context ctx) {
        int tick = payload.data().readInt();
        ctx.client().execute(() -> {
            if (ctx.client().player instanceof IMoveController moveController) {
                moveController.shape_shifter_curse$setNoMoveTick(tick);
            }
        });
    }

    public static void receiveOpenFCSMenu(BytePayload payload, ClientPlayNetworking.Context ctx) {
        ctx.client().execute(() -> {
            if (ShapeShifterCurseFabric.clientConfig.fcs_use_v1_menu) {
                if (FormColorSelectMenu.instance == null) {
                    Screen screen = new FormColorSelectMenu(Component.literal("text.shape-shifter-curse.config.form_color_select_menu"));
                    ctx.client().setScreen(screen);
                }
            } else {
                if (FormColorSelectMenuV2.instance == null) {
                    Screen screen = new FormColorSelectMenuV2(Component.literal("text.shape-shifter-curse.config.form_color_select_menu_v2"));
                    ctx.client().setScreen(screen);
                }
            }
        });
    }

    public static void receiveModifyFCDData(BytePayload payload, ClientPlayNetworking.Context ctx) {
        String commandType = payload.data().readUtf();
        Identifier formID = payload.data().readIdentifier();
        String arg1 = payload.data().readUtf();
        String arg2 = payload.data().readUtf();
        String arg3 = payload.data().readUtf();
        String arg4 = payload.data().readUtf();
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
        switch (commandType) {
            case "save" -> {
                if (!ShapeShifterCurseFabric.playerCustomConfig.enable_server_modify_FCD_config) {
                    return;
                }
                FormTextureUtils.ColorSetting nowColorSetting = FormColorData.getPlayerColorSetting(false);
                if (nowColorSetting == null) {
                    return;
                }
                switch (arg1) {
                    case "form" -> {
                        ShapeShifterCurseFabricClient.formColorData.customSettingByForm.computeIfAbsent(formID, k -> new HashMap<>()).put(arg2, nowColorSetting);
                    }
                    case "global" -> {
                        ShapeShifterCurseFabricClient.formColorData.customSetting.put(arg2, nowColorSetting);
                    }
                    case "form_default" -> {
                        ShapeShifterCurseFabricClient.formColorData.formDefaultSetting.put(formID, nowColorSetting);
                    }
                }
                ShapeShifterCurseFabricClient.formColorData.writeToConfig();
            }
            case "load" -> {
                if (!ShapeShifterCurseFabric.playerCustomConfig.enable_server_modify_FCD_config) {
                    return;
                }
                FormTextureUtils.ColorSetting colorSetting = null;
                switch (arg1) {
                    case "form" -> {
                        colorSetting = ShapeShifterCurseFabricClient.formColorData.customSettingByForm.getOrDefault(formID, new HashMap<>()).getOrDefault(arg2, null);
                    }
                    case "global" -> {
                        colorSetting = ShapeShifterCurseFabricClient.formColorData.customSetting.getOrDefault(arg2, null);
                    }
                    case "form_default" -> {
                        colorSetting = ShapeShifterCurseFabricClient.formColorData.formDefaultSetting.getOrDefault(formID, null);
                    }
                }
                if (colorSetting != null) {
                    ModPacketsS2C.sendUpdateCustomColor(colorSetting, false, false, false, false);
                }
            }
            case "delete" -> {
                if (!ShapeShifterCurseFabric.playerCustomConfig.enable_server_modify_FCD_config) {
                    return;
                }
                switch (arg1) {
                    case "form" -> {
                        ShapeShifterCurseFabricClient.formColorData.customSettingByForm.computeIfAbsent(formID, k -> new HashMap<>()).remove(arg2);
                    }
                    case "global" -> {
                        ShapeShifterCurseFabricClient.formColorData.customSetting.remove(arg2);
                    }
                    case "form_default" -> {
                        ShapeShifterCurseFabricClient.formColorData.formDefaultSetting.remove(formID);
                    }
                }
                ShapeShifterCurseFabricClient.formColorData.writeToConfig();
            }
            case "config" -> {
                if (!ShapeShifterCurseFabric.playerCustomConfig.enable_server_modify_FCD_config) {
                    return;
                }
                switch (arg1) {
                    case "enable_default_color" -> {
                        ShapeShifterCurseFabricClient.formColorData.enableDefaultFormColor = !ShapeShifterCurseFabricClient.formColorData.enableDefaultFormColor;
                        if (ctx.client().player != null) {
                            ctx.client().player.displayClientMessage(Component.translatable("message.shape-shifter-curse.enable_default_color", ShapeShifterCurseFabricClient.formColorData.enableDefaultFormColor), true);
                        }
                    }
                }
                ShapeShifterCurseFabricClient.formColorData.writeToConfig();
            }
            case "list" -> {
                switch (arg1) {
                    case "form" -> {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("All Custom Form Color Settings For %s:\n|".formatted(formID));
                        ShapeShifterCurseFabricClient.formColorData.customSettingByForm.getOrDefault(formID, new HashMap<>()).forEach((k, v) -> stringBuilder.append(" %s |".formatted(k)));
                        if (ctx.client().player != null) {
                            ctx.client().player.displayClientMessage(Component.literal(stringBuilder.toString()), false);
                        }
                    }
                    case "global" -> {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("All Custom Global Color Settings:\n|");
                        ShapeShifterCurseFabricClient.formColorData.customSetting.forEach((k, v) -> stringBuilder.append(" %s |".formatted(k)));
                        if (ctx.client().player != null) {
                            ctx.client().player.displayClientMessage(Component.literal(stringBuilder.toString()), false);
                        }
                    }
                    case "form_default" -> {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("All Default Form Color Settings:\n|");
                        ShapeShifterCurseFabricClient.formColorData.formDefaultSetting.forEach((k, v) -> stringBuilder.append(" %s |".formatted(k)));
                        if (ctx.client().player != null) {
                            ctx.client().player.displayClientMessage(Component.literal(stringBuilder.toString()), false);
                        }
                    }
                }
            }
        }
    }

    public static void sendPatronAuthFile(@Nullable AuthFile authFile) {
        if (authFile == null) {
            return;
        }
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeByteArray(authFile.getRaw());
        ClientPlayNetworking.send(new BytePayload(BytePayload.id(ModPackets.UPLOAD_PATRON_AUTH_FILE), buf));
    }

    private static void receiveRequestPatronAuthFile(BytePayload payload, ClientPlayNetworking.Context ctx) {
        UUID playerID = payload.data().readUUID();
        boolean forceReReadFile = payload.data().readBoolean();
        ctx.client().execute(() -> {
            ShapeShifterCurseFabricClient.onRequestAuthFile();
            AuthClient.requestAuthFile(playerID, forceReReadFile);
        });
    }

    private static void receiveNewSubKey(BytePayload payload, ClientPlayNetworking.Context ctx) {
        FriendlyByteBuf keyBuf = new FriendlyByteBuf(Unpooled.wrappedBuffer(payload.data().readByteArray()));
        ctx.client().execute(() -> {
            AuthClient.loadServerKey(keyBuf);
        });
    }

    private static void receiveSetSuperUserLevel(BytePayload payload, ClientPlayNetworking.Context ctx) {
        int level = payload.data().readInt();
        ctx.client().execute(() -> {
            SuperUserUtils.setClientSULevel(level);
        });
    }
}