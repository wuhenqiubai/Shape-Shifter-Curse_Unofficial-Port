package net.onixary.shapeShifterCurseFabric.networking;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
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
import net.onixary.shapeShifterCurseFabric.util.PatronUtils;
import net.onixary.shapeShifterCurseFabric.util.Verify.AuthClient;
import net.onixary.shapeShifterCurseFabric.util.Verify.AuthFile;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static net.onixary.shapeShifterCurseFabric.networking.ModPackets.*;
import static net.onixary.shapeShifterCurseFabric.screen_effect.TransformFX.beginTransformEffect;

public class ModPacketsS2C {

    public static void register() {
        // Register S2C payload types before registering handlers
        BytePayload.registerS2C(SYNC_CURSED_MOON_DATA);
        BytePayload.registerS2C(SYNC_FORM_CHANGE);
        BytePayload.registerS2C(SYNC_TRANSFORM_STATE);
        BytePayload.registerS2C(SYNC_BAT_ATTACH_STATE);
        BytePayload.registerS2C(RESET_FIRST_PERSON);
        BytePayload.registerS2C(SYNC_OTHER_PLAYER_BAT_ATTACH_STATE);
        BytePayload.registerS2C(SYNC_FORCE_SNEAK_STATE);
        BytePayload.registerS2C(UPDATE_DYNAMIC_FORM);
        BytePayload.registerS2C(REMOVE_DYNAMIC_FORM_EXCEPT);
        BytePayload.registerS2C(LOGIN_PACKET);
        BytePayload.registerS2C(ACTIVE_VIRTUAL_TOTEM);
        BytePayload.registerS2C(UPDATE_POWER_ANIM_DATA_TO_CLIENT);
        BytePayload.registerS2C(UPDATE_PATRON_LEVEL);
        BytePayload.registerS2C(OPEN_PATRON_FORM_SELECT_MENU);
        BytePayload.registerS2C(OPEN_FORM_SELECT_MENU);
        BytePayload.registerS2C(SET_NO_JUMP_TICK);
        BytePayload.registerS2C(OPEN_FORM_COLOR_SELECT_MENU);
        BytePayload.registerS2C(MODIFY_FCD_DATA);

        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(SYNC_CURSED_MOON_DATA), (payload, ctx) -> {
            boolean isCursedMoon = payload.data().readBoolean();
            ctx.client().execute(() -> {
                CursedMoonClient.isCursedMoon = isCursedMoon;
                CursedMoonClient.middayMessageSent = false;
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(SYNC_FORM_CHANGE), (payload, ctx) -> {
            Identifier newFormID = payload.data().readIdentifier();
            ctx.client().execute(() -> {
                if (ctx.client().player != null) {
                    ShapeShifterCurseFabricClient.refreshPlayerAnimations();
                    ShapeShifterCurseFabricClient.formColorData.onClientFormChange(newFormID);
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(SYNC_TRANSFORM_STATE), (payload, ctx) -> {
            PacketByteBuf buf = payload.data();
            UUID playerUuid = buf.readUuid();
            boolean isTransforming = buf.readBoolean();
            String fromForm = buf.readString();
            String toForm = buf.readString();
            ctx.client().execute(() -> {
                MinecraftClient client = ctx.client();
                if (client.player != null) {
                    ShapeShifterCurseFabricClient.updateTransformState(playerUuid, isTransforming, fromForm.isEmpty() ? null : fromForm, toForm.isEmpty() ? null : toForm);
                    if (client.player.getUuid().equals(playerUuid)) {
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
        });
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(SYNC_BAT_ATTACH_STATE), (payload, ctx) -> {
            PacketByteBuf buf = payload.data();
            boolean isAttached = buf.readBoolean();
            int attachTypeOrdinal = buf.readInt();
            BlockPos attachedPos = buf.readBoolean() ? buf.readBlockPos() : null;
            Direction attachedSide = buf.readBoolean() ? Direction.byId(buf.readInt()) : null;
            ctx.client().execute(() -> {
                if (ctx.client().player != null) {
                    BatBlockAttachPower.syncClientState(ctx.client().player, isAttached, attachTypeOrdinal, attachedPos, attachedSide);
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(RESET_FIRST_PERSON), (payload, ctx) -> ctx.client().execute(TransformManager::executeClientFirstPersonReset));
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(SYNC_OTHER_PLAYER_BAT_ATTACH_STATE), (payload, ctx) -> {
            PacketByteBuf buf = payload.data();
            UUID targetPlayerUuid = buf.readUuid();
            boolean isAttached = buf.readBoolean();
            int attachType = buf.readInt();
            BlockPos attachedPos = buf.readBoolean() ? buf.readBlockPos() : null;
            Direction attachedSide = buf.readBoolean() ? Direction.byId(buf.readInt()) : null;
            ctx.client().execute(() -> ClientPlayerStateManager.updatePlayerAttachState(targetPlayerUuid, isAttached, attachType, attachedPos, attachedSide));
        });
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(SYNC_FORCE_SNEAK_STATE), (payload, ctx) -> {
            boolean shouldForce = payload.data().readBoolean();
            ctx.client().execute(() -> ClientPlayerStateManager.shouldForceSneak = shouldForce);
        });
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(UPDATE_DYNAMIC_FORM), (payload, ctx) -> {
            PacketByteBuf buf = payload.data();
            JsonObject allFrom = new JsonObject();
            int formCount = buf.readInt();
            for (int i = 0; i < formCount; i++) {
                allFrom.add(buf.readString(), new Gson().fromJson(buf.readString(), JsonObject.class));
            }
            ctx.client().execute(() -> RegPlayerForms.ApplyDynamicPlayerForms(allFrom));
        });
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(REMOVE_DYNAMIC_FORM_EXCEPT), (payload, ctx) -> {
            PacketByteBuf buf = payload.data();
            List<Identifier> except = new ArrayList<>();
            int formCount = buf.readInt();
            for (int i = 0; i < formCount; i++) except.add(Identifier.tryParse(buf.readString()));
            ctx.client().execute(() -> RegPlayerForms.removeDynamicPlayerFormsExcept(except));
        });
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(LOGIN_PACKET), (payload, ctx) -> {
            TransformManager.executeClientFirstPersonReset();
            new Thread(() -> {
                for (int i = 0; i < 60; i++) {
                    try { Thread.sleep(1000); sendUpdateCustomSetting(); return; }
                    catch (Exception ignored) {}
                }
                ShapeShifterCurseFabric.LOGGER.error("Failed to send custom setting to server after 60 seconds");
            }).start();
        });
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(ACTIVE_VIRTUAL_TOTEM), (payload, ctx) -> {
            PacketByteBuf buf = payload.data();
            if (ctx.client().world == null) {
                ShapeShifterCurseFabric.LOGGER.error("World is null when receiving active virtual totem packet");
                return;
            }
            PlayerEntity playerEntity = ctx.client().world.getPlayerByUuid(buf.readUuid());
            if (playerEntity == null) {
                ShapeShifterCurseFabric.LOGGER.warn("Can't find player entity when receiving active virtual totem packet");
                return;
            }
            Identifier virtualTotemType = buf.readIdentifier();
            ItemStack totemStack;
            if (buf.readBoolean()) {
                RegistryByteBuf regBuf = new RegistryByteBuf(buf, ctx.client().world.getRegistryManager());
                totemStack = ItemStack.PACKET_CODEC.decode(regBuf);
            } else {
                totemStack = ItemStack.EMPTY;
            }
            ctx.client().execute(() -> VirtualTotemPower.process_virtual_totem_type(playerEntity, virtualTotemType, totemStack));
        });
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(UPDATE_POWER_ANIM_DATA_TO_CLIENT), (payload, ctx) -> {
            PacketByteBuf buf = payload.data();
            UUID playerUuid = buf.readUuid();
            Identifier animationId = buf.readBoolean() ? buf.readIdentifier() : null;
            int animationCount = buf.readInt();
            int animationLength = buf.readInt();
            if (ctx.client().world == null) {
                ShapeShifterCurseFabric.LOGGER.error("World is null when receiving update power anim data packet");
                return;
            }
            PlayerEntity playerEntity = ctx.client().world.getPlayerByUuid(playerUuid);
            ctx.client().execute(() -> {
                if (playerEntity instanceof IPlayerAnimController animPlayer) {
                    animPlayer.shape_shifter_curse$setAnimationData(animationId, animationCount, animationLength);
                } else if (playerEntity != null) {
                    ShapeShifterCurseFabric.LOGGER.error("Player {} is not a IPlayerAnimController", playerEntity.getName());
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(UPDATE_PATRON_LEVEL), (payload, ctx) -> {
            PacketByteBuf buf = payload.data();
            int PairCount = buf.readInt();
            HashMap<UUID, Integer> map = new HashMap<>();
            for (int i = 0; i < PairCount; i++) map.put(buf.readUuid(), buf.readInt());
            ctx.client().execute(() -> PatronUtils.ApplyPatronLevel(map));
        });
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(OPEN_PATRON_FORM_SELECT_MENU), (payload, ctx) -> ctx.client().execute(() -> ctx.client().setScreen(new PatronFormSelectScreen(Text.literal("PatronFromSelectScreen"), ctx.client().player))));
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(OPEN_FORM_SELECT_MENU), (payload, ctx) -> {
            PacketByteBuf buf = payload.data();
            String targetName = buf.readString();
            UUID targetUUID = buf.readUuid();
            ctx.client().execute(() -> ctx.client().setScreen(new NormalFormSelectScreen(Text.literal("FormSelectScreen"), targetName, targetUUID)));
        });
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(SET_NO_JUMP_TICK), (payload, ctx) -> {
            int tick = payload.data().readInt();
            ctx.client().execute(() -> {
                if (ctx.client().player instanceof IJumpController jumpController) {
                    jumpController.shape_shifter_curse$setNoJumpTick(tick);
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(OPEN_FORM_COLOR_SELECT_MENU), (payload, ctx) -> ctx.client().execute(() -> {
            if (ShapeShifterCurseFabric.clientConfig.fcs_use_v1_menu) {
                if (FormColorSelectMenu.instance == null)
                    ctx.client().setScreen(new FormColorSelectMenu(Text.literal("text.shape-shifter-curse.config.form_color_select_menu")));
            } else {
                if (FormColorSelectMenuV2.instance == null)
                    ctx.client().setScreen(new FormColorSelectMenuV2(Text.literal("text.shape-shifter-curse.config.form_color_select_menu_v2")));
            }
        }));
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(MODIFY_FCD_DATA), (payload, ctx) -> {
            PacketByteBuf buf = payload.data();
            String commandType = buf.readString();
            Identifier formID = buf.readIdentifier();
            String arg1 = buf.readString();
            MinecraftClient client = ctx.client();
            switch (commandType) {
                case "save" -> {
                    if (!ShapeShifterCurseFabric.playerCustomConfig.enable_server_modify_FCD_config) return;
                    FormTextureUtils.ColorSetting nowColorSetting = FormColorData.getPlayerColorSetting(false);
                    if (nowColorSetting == null) return;
                    switch (arg1) {
                        case "form" -> ShapeShifterCurseFabricClient.formColorData.customSettingByForm.computeIfAbsent(formID, k -> new HashMap<>()).put(buf.readString(), nowColorSetting);
                        case "global" -> ShapeShifterCurseFabricClient.formColorData.customSetting.put(buf.readString(), nowColorSetting);
                        case "form_default" -> ShapeShifterCurseFabricClient.formColorData.formDefaultSetting.put(formID, nowColorSetting);
                    }
                    ShapeShifterCurseFabricClient.formColorData.writeToConfig();
                }
                case "load" -> {
                    if (!ShapeShifterCurseFabric.playerCustomConfig.enable_server_modify_FCD_config) return;
                    FormTextureUtils.ColorSetting colorSetting = switch (arg1) {
                        case "form" -> ShapeShifterCurseFabricClient.formColorData.customSettingByForm.getOrDefault(formID, new HashMap<>()).getOrDefault(buf.readString(), null);
                        case "global" -> ShapeShifterCurseFabricClient.formColorData.customSetting.getOrDefault(buf.readString(), null);
                        case "form_default" -> ShapeShifterCurseFabricClient.formColorData.formDefaultSetting.getOrDefault(formID, null);
                        default -> null;
                    };
                    if (colorSetting != null) sendUpdateCustomColor(colorSetting, false, false, false, false);
                }
                case "delete" -> {
                    if (!ShapeShifterCurseFabric.playerCustomConfig.enable_server_modify_FCD_config) return;
                    switch (arg1) {
                        case "form" -> ShapeShifterCurseFabricClient.formColorData.customSettingByForm.computeIfAbsent(formID, k -> new HashMap<>()).remove(buf.readString());
                        case "global" -> ShapeShifterCurseFabricClient.formColorData.customSetting.remove(buf.readString());
                        case "form_default" -> ShapeShifterCurseFabricClient.formColorData.formDefaultSetting.remove(formID);
                    }
                    ShapeShifterCurseFabricClient.formColorData.writeToConfig();
                }
                case "config" -> {
                    if (!ShapeShifterCurseFabric.playerCustomConfig.enable_server_modify_FCD_config) return;
                    if ("enable_default_color".equals(arg1)) {
                        ShapeShifterCurseFabricClient.formColorData.enableDefaultFormColor = !ShapeShifterCurseFabricClient.formColorData.enableDefaultFormColor;
                        if (client.player != null) client.player.sendMessage(Text.translatable("message.shape-shifter-curse.enable_default_color", ShapeShifterCurseFabricClient.formColorData.enableDefaultFormColor), true);
                    }
                    ShapeShifterCurseFabricClient.formColorData.writeToConfig();
                }
                case "list" -> {
                    StringBuilder sb = new StringBuilder();
                    switch (arg1) {
                        case "form" -> {
                            sb.append("All Custom Form Color Settings For %s:\n|".formatted(formID));
                            ShapeShifterCurseFabricClient.formColorData.customSettingByForm.getOrDefault(formID, new HashMap<>()).forEach((k, v) -> sb.append(" %s |".formatted(k)));
                        }
                        case "global" -> {
                            sb.append("All Custom Global Color Settings:\n|");
                            ShapeShifterCurseFabricClient.formColorData.customSetting.forEach((k, v) -> sb.append(" %s |".formatted(k)));
                        }
                        case "form_default" -> {
                            sb.append("All Default Form Color Settings:\n|");
                            ShapeShifterCurseFabricClient.formColorData.formDefaultSetting.forEach((k, v) -> sb.append(" %s |".formatted(k)));
                        }
                    }
                    if (client.player != null) client.player.sendMessage(Text.literal(sb.toString()), false);
                }
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(BytePayload.id(ModPackets.MELT_AUTH_SUB_KEY), ModPacketsS2C::receiveNewSubKey);
    }

    public static void sendUpdateCustomColor(FormTextureUtils.ColorSetting colorSetting, boolean sendRAW, boolean sendExtraData, boolean keepOriginalSkin, boolean enableFormColorSystem) {
        PacketByteBuf buf = PacketByteBufs.create();
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

    public static void sendUpdateCustomSetting(boolean ForceUpdate) {
        PacketByteBuf buf = PacketByteBufs.create();
        boolean autoSyncConfig = ShapeShifterCurseFabric.playerCustomConfig.auto_sync_config;
        if (!ForceUpdate && !autoSyncConfig) return;
        buf.writeBoolean(ShapeShifterCurseFabric.playerCustomConfig.keep_original_skin);
        buf.writeBoolean(ShapeShifterCurseFabric.playerCustomConfig.enable_form_color);
        buf.writeBoolean(ShapeShifterCurseFabric.playerCustomConfig.enable_form_random_sound);
        ClientPlayNetworking.send(new BytePayload(BytePayload.id(UPDATE_CUSTOM_SETTING),  buf));
        boolean autoSyncColorConfig = ShapeShifterCurseFabric.playerCustomConfig.auto_sync_color_config;
        if (!ForceUpdate && !autoSyncColorConfig) return;
        buf = PacketByteBufs.create();
        buf.writeBoolean(false);
        buf.writeInt(FormTextureUtils.ARGB2ABGR(ShapeShifterCurseFabric.playerCustomConfig.primaryColor));
        buf.writeInt(FormTextureUtils.ARGB2ABGR(ShapeShifterCurseFabric.playerCustomConfig.accentColor1Color));
        buf.writeInt(FormTextureUtils.ARGB2ABGR(ShapeShifterCurseFabric.playerCustomConfig.accentColor2Color));
        buf.writeInt(FormTextureUtils.ARGB2ABGR(ShapeShifterCurseFabric.playerCustomConfig.eyeColorA));
        buf.writeInt(FormTextureUtils.ARGB2ABGR(ShapeShifterCurseFabric.playerCustomConfig.eyeColorB));
        buf.writeBoolean(ShapeShifterCurseFabric.playerCustomConfig.primaryGreyReverse);
        buf.writeBoolean(ShapeShifterCurseFabric.playerCustomConfig.accent1GreyReverse);
        buf.writeBoolean(ShapeShifterCurseFabric.playerCustomConfig.accent2GreyReverse);
        ClientPlayNetworking.send(new BytePayload(BytePayload.id(UPDATE_CUSTOM_COLOR),  buf));
    }

    public static void sendUpdateCustomSetting() { sendUpdateCustomSetting(false); }

    public static void sendPowerAnimationDataToServer(@Nullable Identifier animationId, int animationCount, int animationLength) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(animationId != null);
        if (animationId != null) buf.writeIdentifier(animationId);
        buf.writeInt(animationCount);
        buf.writeInt(animationLength);
        ClientPlayNetworking.send(new BytePayload(BytePayload.id(UPDATE_POWER_ANIM_DATA_TO_SERVER),  buf));
    }

    public static void sendRequestPlayerAnimationData(UUID targetPlayerUUID) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(targetPlayerUUID);
        ClientPlayNetworking.send(new BytePayload(BytePayload.id(REQUEST_POWER_ANIM_DATA),  buf));
    }

    public static void sendSetPatronForm(Identifier formID) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeIdentifier(formID);
        ClientPlayNetworking.send(new BytePayload(BytePayload.id(SET_PATRON_FORM),  buf));
    }

    public static void sendSetForm(Identifier formID, UUID target) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(target);
        buf.writeIdentifier(formID);
        ClientPlayNetworking.send(new BytePayload(BytePayload.id(SET_FORM),  buf));
    }

    public static void sendPatronAuthFile(AuthFile authFile) {
        if (authFile == null) return;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeByteArray(authFile.getRaw());
        ClientPlayNetworking.send(new BytePayload(BytePayload.id(ModPackets.UPLOAD_PATRON_AUTH_FILE), buf));
    }

    private static void receiveNewSubKey(BytePayload payload, ClientPlayNetworking.Context context) {
        PacketByteBuf keyBuf = new PacketByteBuf(Unpooled.wrappedBuffer(payload.data().readByteArray()));
        context.client().execute(() -> AuthClient.loadServerKey(keyBuf));
    }
}