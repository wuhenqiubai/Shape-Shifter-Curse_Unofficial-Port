package net.onixary.shapeShifterCurseFabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.cursed_moon.CursedMoon;
import net.onixary.shapeShifterCurseFabric.entity.projectile.WebBullet;
import net.onixary.shapeShifterCurseFabric.mana.RegManaComponent;
import net.onixary.shapeShifterCurseFabric.minion.RegPlayerMinionComponent;
import net.onixary.shapeShifterCurseFabric.networking.ModPacketsS2CServer;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.skin.RegPlayerSkinComponent;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;
import net.onixary.shapeShifterCurseFabric.player_form.utils.PlayerFormComponent;
import net.onixary.shapeShifterCurseFabric.player_form.utils.TransformManager;
import net.onixary.shapeShifterCurseFabric.status_effects.attachment.EffectManager;
import net.onixary.shapeShifterCurseFabric.util.FormColorData;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;
import net.onixary.shapeShifterCurseFabric.util.SuperUserUtils;
import net.onixary.shapeShifterCurseFabric.util.Verify.DebuggerUtils;
import net.onixary.shapeShifterCurseFabric.util.Verify.PatronDataSegment;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ShapeShifterCurseCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(
                literal("shape_shifter_curse")
                        .then(literal("set_form").requires(cs -> cs.hasPermission(2))
                                .then(argument("target", EntityArgument.player())
                                        .then(argument("form", new FormArgumentType(FormArgumentType.SET_FORM_ARG))
                                                .executes(context -> ShapeShifterCurseCommand.setForm(context, Component.translatable("command.shape_shifter_curse.no_permission_form")))
                                        )
                                )
                        )
                        .then(literal("transform_to_form").requires(cs -> cs.hasPermission(2))
                                .then(argument("target", EntityArgument.player())
                                        .then(argument("form", new FormArgumentType(FormArgumentType.SET_FORM_ARG))
                                                .executes(context -> ShapeShifterCurseCommand.transformToForm(context, Component.translatable("command.shape_shifter_curse.no_permission_form")))
                                        )
                                )
                        )
                        .then(literal("set_dynamic_form").requires(cs -> cs.hasPermission(2))
                                .then(argument("target", EntityArgument.player())
                                        .then(argument("form", new FormArgumentType(FormArgumentType.SET_DYNAMIC_FORM_ARG))
                                                .executes(context -> ShapeShifterCurseCommand.setForm(context, Component.translatable("command.shape_shifter_curse.no_permission_dynamic_form")))
                                        )
                                )
                        )
                        .then(literal("transform_to_dynamic_form").requires(cs -> cs.hasPermission(2))
                                .then(argument("target", EntityArgument.player())
                                        .then(argument("form", new FormArgumentType(FormArgumentType.SET_DYNAMIC_FORM_ARG))
                                                .executes(context -> ShapeShifterCurseCommand.transformToForm(context, Component.translatable("command.shape_shifter_curse.no_permission_dynamic_form")))
                                        )
                                )
                        )
                        .then(literal("set_sub_form").requires(cs -> cs.hasPermission(2))
                                .then(argument("target", EntityArgument.player())
                                        .then(argument("form", new FormArgumentType(FormArgumentType.SET_SUB_FORM_ARG))
                                                .executes(context -> ShapeShifterCurseCommand.setForm(context, Component.translatable("command.shape_shifter_curse.no_permission_sub_form")))
                                        )
                                )
                        )
                        .then(literal("transform_to_sub_form").requires(cs -> cs.hasPermission(2))
                                .then(argument("target", EntityArgument.player())
                                        .then(argument("form", new FormArgumentType(FormArgumentType.SET_SUB_FORM_ARG))
                                                .executes(context -> ShapeShifterCurseCommand.transformToForm(context, Component.translatable("command.shape_shifter_curse.no_permission_sub_form")))
                                        )
                                )
                        )
                        .then(literal("jump_to_next_cursed_moon").requires(cs -> cs.hasPermission(2))
                                .executes(ShapeShifterCurseCommand::jumpToNextCursedMoon)
                        )
                        .then(literal("world_time").requires(cs -> cs.hasPermission(2))
                                .then(literal("set").then(argument("time", IntegerArgumentType.integer())
                                        .executes(ShapeShifterCurseCommand::setWorldTime))
                                )
                                .then(literal("add").then(argument("time", IntegerArgumentType.integer())
                                        .executes(ShapeShifterCurseCommand::addWorldTime))
                                )
                        )
                        .then(literal("adjust_feral_item_loc").requires(cs -> cs.hasPermission(2))
                                .then(argument("rot_center", Vec3Argument.vec3())
                                        .then(argument("pos_offset", Vec3Argument.vec3())
                                                .then(argument("euler_x", FloatArgumentType.floatArg())
                                                        .executes(ShapeShifterCurseCommand::adjustFeralItemLoc)
                                                )
                                        )
                                )
                        )
                        .then(literal("keep_original_skin").requires(cs -> cs.hasPermission(0))
                                .then(argument("value", BoolArgumentType.bool())
                                        .executes(ShapeShifterCurseCommand::setPlayerSkin)
                                )
                        )
                        .then(literal("set_form_color").requires(cs -> cs.hasPermission(0))
                                .executes(ShapeShifterCurseCommand::logFormColorSetting)
                                .then(argument("enable", BoolArgumentType.bool())
                                        .executes(ShapeShifterCurseCommand::setFormColorEnable)
                                        .then(argument("primaryColorRGBA", StringArgumentType.string())
                                                .then(argument("accentColor1RGBA", StringArgumentType.string())
                                                        .then(argument("accentColor2RGBA", StringArgumentType.string())
                                                                .then(argument("eyeColor", StringArgumentType.string())
                                                                        .then(argument("primaryGreyReverse", BoolArgumentType.bool())
                                                                                .then(argument("accent1GreyReverse", BoolArgumentType.bool())
                                                                                        .then(argument("accent2GreyReverse", BoolArgumentType.bool())
                                                                                                .executes(ShapeShifterCurseCommand::setFormColor)
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(literal("debug")
                                .then(literal("dev_command").executes(ShapeShifterCurseCommand::devCommand))
                                .then(literal("clear_player_form_data")
                                        .then(argument("target", EntityArgument.player())
                                                .executes(ShapeShifterCurseCommand::clearPlayerFormData)
                                        )
                                )
                                .then(literal("clear_player_skin_data")
                                        .then(argument("target", EntityArgument.player())
                                                .executes(ShapeShifterCurseCommand::clearPlayerSkinData)
                                        )
                                )
                                .then(literal("clear_player_minion_data")
                                        .then(argument("target", EntityArgument.player())
                                                .executes(ShapeShifterCurseCommand::clearPlayerMinionData)
                                        )
                                )
                                .then(literal("clear_player_mana_data")
                                        .then(argument("target", EntityArgument.player())
                                                .executes(ShapeShifterCurseCommand::clearPlayerManaData)
                                        )
                                )
                                .then(literal("su")
                                        .then(argument("level", IntegerArgumentType.integer(-1, 4))
                                                .executes(ShapeShifterCurseCommand::SU_Command)
                                        )
                                )
                                .then(literal("set_form")
                                        .then(argument("target", EntityArgument.player())
                                                .then(argument("form", new FormArgumentType(FormArgumentType.ALL_FORM_ARG))
                                                        .executes(ShapeShifterCurseCommand::setDebugForm)
                                                )
                                        )
                                )
                                .then(literal("reupload_auth_file")
                                        .executes(ShapeShifterCurseCommand::requestNewAuthData)
                                )
                        )
                        .then(literal("patron_info").requires(cs -> cs.hasPermission(0))
                                .executes(ShapeShifterCurseCommand::logPatronInfo)
                        )
                        .then(literal("form_color").requires(cs -> cs.hasPermission(0))
                                .then(literal("menu").executes(ShapeShifterCurseCommand::FC_Menu))
                                .then(literal("save")
                                        .then(argument("type", new MiscArgumentType.Enum_ArgumentType("form", "global", "form_default"))
                                                .then(argument("slot_name", StringArgumentType.string())
                                                        .executes(ShapeShifterCurseCommand::FC_Save)
                                                        .then(argument("form", new FormArgumentType(FormArgumentType.ALL_FORM_ARG))
                                                                .executes(ShapeShifterCurseCommand::FC_Save)
                                                        )
                                                )
                                        )
                                )
                                .then(literal("load")
                                        .then(argument("type", new MiscArgumentType.Enum_ArgumentType("form", "global", "form_default"))
                                                .then(argument("slot_name", StringArgumentType.string())
                                                        .executes(ShapeShifterCurseCommand::FC_Load)
                                                        .then(argument("form", new FormArgumentType(FormArgumentType.ALL_FORM_ARG))
                                                                .executes(ShapeShifterCurseCommand::FC_Load)
                                                        )
                                                )
                                        )
                                )
                                .then(literal("delete")
                                        .then(argument("type", new MiscArgumentType.Enum_ArgumentType("form", "global", "form_default"))
                                                .then(argument("slot_name", StringArgumentType.string())
                                                        .executes(ShapeShifterCurseCommand::FC_Delete)
                                                        .then(argument("form", new FormArgumentType(FormArgumentType.ALL_FORM_ARG))
                                                                .executes(ShapeShifterCurseCommand::FC_Delete)
                                                        )
                                                )
                                        )
                                )
                                .then(literal("config")
                                        .then(argument("type", new MiscArgumentType.Enum_ArgumentType("enable_default_color"))
                                                .executes(ShapeShifterCurseCommand::FC_Config)
                                        )
                                )
                                .then(literal("list")
                                        .then(argument("type", new MiscArgumentType.Enum_ArgumentType("form", "global", "form_default"))
                                                .executes(ShapeShifterCurseCommand::FC_List)
                                                .then(argument("form", new FormArgumentType(FormArgumentType.ALL_FORM_ARG))
                                                        .executes(ShapeShifterCurseCommand::FC_List)
                                                )
                                        )
                                )
                                .then(literal("to_chat")
                                        .then(argument("type", new MiscArgumentType.Enum_ArgumentType("local", "server"))
                                                .then(argument("message_type", new MiscArgumentType.Enum_ArgumentType("raw", "command"))
                                                        .then(argument("encode_type", new MiscArgumentType.Enum_ArgumentType("base64", "hex"))
                                                                .executes(ShapeShifterCurseCommand::FC_ToChat)
                                                        )
                                                )
                                        )
                                )
                                .then(literal("set_color_from_string")
                                        .then(argument("color_string", StringArgumentType.string())
                                                .executes(ShapeShifterCurseCommand::FC_SetColorFromString)
                                        )
                                )
                        )
        );
    }

    private static int setForm(CommandContext<CommandSourceStack> commandContext, Component failText) throws CommandSyntaxException {
        // set form without transform effect
        ServerPlayer target = EntityArgument.getPlayer(commandContext, "target");
        IForm form = FormArgumentType.getForm(commandContext, "form");
        CommandSourceStack serverCommandSource = commandContext.getSource();
        if (form == null) {
            commandContext.getSource().sendFailure(Component.literal("Invalid Form Id!"));
            return 0;
        }
        try {
            boolean success = TransformManager.forceTransform(target, form, true);
            if (!success) {
                commandContext.getSource().sendFailure(failText);
            }
        }
        catch (Exception e){
            // 调试时在此打断点
            ShapeShifterCurseFabric.LOGGER.error("Exception when set form", e);
            throw e;
        }

        return 1;

    }

    private static int transformToForm(CommandContext<CommandSourceStack> commandContext, Component failText) throws CommandSyntaxException {
        // this with transform effect
        ServerPlayer target = EntityArgument.getPlayer(commandContext, "target");
        IForm form = FormArgumentType.getForm(commandContext, "form");
        CommandSourceStack serverCommandSource = commandContext.getSource();
        if (form == null) {
            commandContext.getSource().sendFailure(Component.literal("Invalid Form Id!"));
            return 0;
        }
        try {
            boolean success = TransformManager.forceTransform(target, form, false);
            if (!success) {
                commandContext.getSource().sendFailure(failText);
            }
        }
        catch (Exception e){
            // 调试时在此打断点
            ShapeShifterCurseFabric.LOGGER.error("Exception when transform form", e);
            throw e;
        }
        return 1;
    }

    private static int jumpToNextCursedMoon(CommandContext<CommandSourceStack> commandContext) {
        ServerLevel world = commandContext.getSource().getLevel();
        CursedMoon.forceTriggerCursedMoon(world);
        CommandSourceStack serverCommandSource = commandContext.getSource();
        serverCommandSource.sendSuccess(() -> Component.literal("Set cursed moon to next night!"), true);
        return 1;
    }

    private static int adjustFeralItemLoc(CommandContext<CommandSourceStack> commandContext) {
        Vec3 rotCenter = Vec3Argument.getVec3(commandContext, "rot_center");
        Vec3 posOffset = Vec3Argument.getVec3(commandContext, "pos_offset");
        float eulerX = FloatArgumentType.getFloat(commandContext, "euler_x");
        ShapeShifterCurseFabric.feralItemCenter = rotCenter;
        ShapeShifterCurseFabric.feralItemPosOffset = posOffset;
        ShapeShifterCurseFabric.feralItemEulerX = eulerX;
        CommandSourceStack serverCommandSource = commandContext.getSource();
        serverCommandSource.sendSuccess(() -> Component.literal("Location adjusted! Center : " + rotCenter + " Offset: " + posOffset + " RotationX : " + eulerX), true);
        return 1;
    }

    private static int setPlayerSkin(CommandContext<CommandSourceStack> commandContext) {
        try {
            ServerPlayer player = commandContext.getSource().getPlayer();
            boolean newSetting = BoolArgumentType.getBool(commandContext, "value");
            RegPlayerSkinComponent.SKIN_SETTINGS.get(player).setKeepOriginalSkin(newSetting);
            RegPlayerSkinComponent.SKIN_SETTINGS.sync(player);
            String message = newSetting
                    ? "Successfully set to use your original skin!"
                    : "Successfully set to use built-in skin!";
            player.displayClientMessage(Component.literal(message), false);

            return 1;
        } catch (Exception e) {
            // 处理其他可能的错误
            commandContext.getSource().sendFailure(Component.literal("Error when change player skin: " + e.getMessage()));
            ShapeShifterCurseFabric.LOGGER.error("Error when change player skin: ", e);
            return 0;
        }
    }

    private static String getColorHexFormABGR(int color) {
        int ARGB = FormTextureUtils.ABGR2ARGB(color);
        String String = Integer.toHexString(ARGB);
        if (String.length() < 8) {
            return "00000000".substring(0, 8 - String.length()) + String;
        }
        return String;
    }

    private static int logFormColorSetting(CommandContext<CommandSourceStack> commandContext) {
        try {
            ServerPlayer player = commandContext.getSource().getPlayer();
            if (player == null) {
                commandContext.getSource().sendFailure(Component.literal("Must be a player!"));
                return 0;
            }
            String message = "Form color setting: \n";
            message += "Enable: " + RegPlayerSkinComponent.SKIN_SETTINGS.get(player).isEnableFormColor() + "\n";
            message += "Primary Color ARGB: " + getColorHexFormABGR(RegPlayerSkinComponent.SKIN_SETTINGS.get(player).getFormColor().getPrimaryColor()) + "\n";
            message += "Accent Color 1 ARGB: " + getColorHexFormABGR(RegPlayerSkinComponent.SKIN_SETTINGS.get(player).getFormColor().getAccentColor1()) + "\n";
            message += "Accent Color 2 ARGB: " + getColorHexFormABGR(RegPlayerSkinComponent.SKIN_SETTINGS.get(player).getFormColor().getAccentColor2()) + "\n";
            message += "Eye Color A ARGB: " + getColorHexFormABGR(RegPlayerSkinComponent.SKIN_SETTINGS.get(player).getFormColor().getEyeColorA()) + "\n";
            message += "Eye Color B ARGB: " + getColorHexFormABGR(RegPlayerSkinComponent.SKIN_SETTINGS.get(player).getFormColor().getEyeColorB()) + "\n";
            message += "Primary Grey Reverse: " + RegPlayerSkinComponent.SKIN_SETTINGS.get(player).getFormColor().getPrimaryGreyReverse() + "\n";
            message += "Accent 1 Grey Reverse: " + RegPlayerSkinComponent.SKIN_SETTINGS.get(player).getFormColor().getAccent1GreyReverse() + "\n";
            message += "Accent 2 Grey Reverse: " + RegPlayerSkinComponent.SKIN_SETTINGS.get(player).getFormColor().getAccent2GreyReverse() + "\n";
            player.displayClientMessage(Component.literal(message), false);
            return 1;
        }
        catch (Exception e) {
            // 处理其他可能的错误
            commandContext.getSource().sendFailure(Component.literal("Error when log player form color: " + e.getMessage()));
            ShapeShifterCurseFabric.LOGGER.error("Error when log player form color: ", e);
            return 0;
        }
    }

    private static int setFormColorEnable(CommandContext<CommandSourceStack> commandContext) {
        try {
            ServerPlayer player = commandContext.getSource().getPlayer();
            if (player == null) {
                commandContext.getSource().sendFailure(Component.literal("Must be a player!"));
                return 0;
            }
            boolean enable = BoolArgumentType.getBool(commandContext, "enable");
            RegPlayerSkinComponent.SKIN_SETTINGS.get(player).setEnableFormColor(enable);
            RegPlayerSkinComponent.SKIN_SETTINGS.sync(player);
            return 1;
        } catch (Exception e) {
            // 处理其他可能的错误
            commandContext.getSource().sendFailure(Component.literal("Error when change player form color: " + e.getMessage()));
            ShapeShifterCurseFabric.LOGGER.error("Error when change player form color: ", e);
            return 0;
        }
    }

    private static int setFormColor(CommandContext<CommandSourceStack> commandContext) {
        try {
            ServerPlayer player = commandContext.getSource().getPlayer();
            if (player == null) {
                commandContext.getSource().sendFailure(Component.literal("Must be a player!"));
                return 0;
            }
            boolean enable = BoolArgumentType.getBool(commandContext, "enable");
            String primaryColorRGBA = StringArgumentType.getString(commandContext, "primaryColorRGBA");
            String accentColor1RGBA = StringArgumentType.getString(commandContext, "accentColor1RGBA");
            String accentColor2RGBA = StringArgumentType.getString(commandContext, "accentColor2RGBA");
            String eyeColorA = StringArgumentType.getString(commandContext, "eyeColorA");
            String eyeColorB = StringArgumentType.getString(commandContext, "eyeColorB");
            if (!RegPlayerSkinComponent.SKIN_SETTINGS.get(player).setFormColor(primaryColorRGBA, accentColor1RGBA, accentColor2RGBA, eyeColorA, eyeColorB, BoolArgumentType.getBool(commandContext, "primaryGreyReverse"), BoolArgumentType.getBool(commandContext, "accent1GreyReverse"), BoolArgumentType.getBool(commandContext, "accent2GreyReverse"))) {
                commandContext.getSource().sendFailure(Component.literal("Invalid color format!"));
                return 0;
            }
            RegPlayerSkinComponent.SKIN_SETTINGS.get(player).setEnableFormColor(enable);
            RegPlayerSkinComponent.SKIN_SETTINGS.sync(player);
            return 1;
        } catch (Exception e) {
            // 处理其他可能的错误
            commandContext.getSource().sendFailure(Component.literal("Error when change player form color: " + e.getMessage()));
            ShapeShifterCurseFabric.LOGGER.error("Error when change player form color: ", e);
            return 0;
        }
    }

    private static int logPatronInfo(CommandContext<CommandSourceStack> commandContext) {
        try {
            ServerPlayer player = commandContext.getSource().getPlayer();
            if (player == null) {
                commandContext.getSource().sendFailure(Component.literal("Must be a player!"));
                return 0;
            }
            PatronDataSegment patronDataSegment = PatronDataSegment.getPatronDataSegment(player);
            StringBuilder message = new StringBuilder("Patron Info:\n");
            message.append("UUID: ").append(player.getUUID()).append("\n");
            message.append("Patron Level: ").append(patronDataSegment != null ? patronDataSegment.getLevel() : 0).append("\n");
            if (patronDataSegment != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                long expireTime = patronDataSegment.getExpireTime();
                message.append("Expire Time: ").append(LocalDateTime.ofInstant(Instant.ofEpochSecond(expireTime), ZoneId.systemDefault()).format(formatter)).append("\n");
            }
            // message.append("\n");
            player.displayClientMessage(Component.literal(message.toString()), false);
        } catch (Exception e) {
            // 处理其他可能的错误
            commandContext.getSource().sendFailure(Component.literal("Error when log player patron info: " + e.getMessage()));
            ShapeShifterCurseFabric.LOGGER.error("Error when log player patron info: ", e);
        }
        return 1;
    }

    // private static int logPatronInfo(CommandContext<CommandSourceStack> commandContext) {
    //     if (!PatronUtils.EnablePatronFeature) {
    //         commandContext.getSource().sendFailure(Component.literal("Patron feature is disabled!"));
    //         return 0;
    //     }
    //     try {
    //         ServerPlayer player = commandContext.getSource().getPlayer();
    //         if (player == null) {
    //             commandContext.getSource().sendFailure(Component.literal("Must be a player!"));
    //             return 0;
    //         }
    //         StringBuilder message = new StringBuilder("Patron Info:\n");
    //         message.append("UUID: ").append(player.getUuid()).append("\n");
    //         message.append("Patron Level: ").append(PatronUtils.PatronLevels.getOrDefault(player.getUuid(), 0)).append("\n");
    //         message.append("Available FormID: ");
    //         for (Identifier formID : getAvailableForms(player)) {
    //             message.append(formID.toString()).append(" ");
    //         }
    //         message.append("\n");
    //         player.sendMessage(Component.literal(message.toString()), false);
    //     } catch (Exception e) {
    //         // 处理其他可能的错误
    //         commandContext.getSource().sendFailure(Component.literal("Error when log player patron info: " + e.getMessage()));
    //         ShapeShifterCurseFabric.LOGGER.error("Error when log player patron info: ", e);
    //     }
    //     return 1;
    // }
//
    // // 仅用于logPatronInfo 使用
    // private static List<Identifier> getAvailableForms(ServerPlayer player) {
    //     List<Identifier> availableForms = new ArrayList<>();
    //     for (Identifier formID : RegPlayerForms.dynamicPlayerForms) {
    //         IForm form = RegPlayerForms.getPlayerForm(formID);
    //         if (form instanceof DynamicForm pfd) {
    //             if (pfd.IsPatronForm && pfd.IsPlayerCanUse(player)) {
    //                 if (!availableForms.contains(formID)) {
    //                     availableForms.add(formID);
    //                 }
    //             }
    //         }
    //     }
    //     return availableForms;
    // }

    private static int setWorldTime(CommandContext<CommandSourceStack> commandContext) {
        ServerLevel world = commandContext.getSource().getLevel();
        world.setDayTime(IntegerArgumentType.getInteger(commandContext, "time"));
        commandContext.getSource().sendSuccess(() -> {return Component.literal("World time set to " + commandContext.getSource().getLevel().getDayTime());}, false);
        return 1;
    }

    private static int addWorldTime(CommandContext<CommandSourceStack> commandContext) {
        ServerLevel world = commandContext.getSource().getLevel();
        long TargetTime = world.getDayTime() + IntegerArgumentType.getInteger(commandContext, "time");
        world.setDayTime(TargetTime);
        commandContext.getSource().sendSuccess(() -> {return Component.literal("World time set to " + TargetTime);}, false);
        return 1;
    }

    private static int devCommand(CommandContext<CommandSourceStack> commandContext) {
        ServerPlayer player = commandContext.getSource().getPlayer();
        if (!DebuggerUtils.canExecute(commandContext, player, 1)) {
            commandContext.getSource().sendFailure(Component.literal("Has No Permission!"));
            return 0;
        }
        ServerLevel world = commandContext.getSource().getLevel();
        if (player == null) {
            return 0;
        }
        try {
            WebBullet webBullet = new WebBullet(player, 1);
            webBullet.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 2.25f, 0.5f);
            player.level().addFreshEntity(webBullet);
        } catch (Exception e) {
            ShapeShifterCurseFabric.LOGGER.error("Error Dev Command", e);
            return 0;
        }
        return 1;
    }


    private static int clearPlayerFormData(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        ServerPlayer player = commandContext.getSource().getPlayer();
        if (!DebuggerUtils.canExecute(commandContext, player, 1)) {
            commandContext.getSource().sendFailure(Component.literal("Has No Permission!"));
            return 0;
        }
        ServerPlayer target = EntityArgument.getPlayer(commandContext, "target");
        PlayerFormComponent.COMPONENT.get(target).clear();
        PlayerFormComponent.COMPONENT.sync(target);
        commandContext.getSource().sendSuccess(() -> {return Component.literal("Form Data Cleared!");}, false);
        return 1;
    }

    private static int clearPlayerSkinData(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        ServerPlayer player = commandContext.getSource().getPlayer();
        if (!DebuggerUtils.canExecute(commandContext, player, 1)) {
            commandContext.getSource().sendFailure(Component.literal("Has No Permission!"));
            return 0;
        }
        ServerPlayer target = EntityArgument.getPlayer(commandContext, "target");
        RegPlayerSkinComponent.SKIN_SETTINGS.get(target).clear();
        RegPlayerSkinComponent.SKIN_SETTINGS.sync(target);
        commandContext.getSource().sendSuccess(() -> {return Component.literal("Skin Data Cleared!");}, false);
        return 1;
    }

    private static int clearPlayerMinionData(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        ServerPlayer player = commandContext.getSource().getPlayer();
        if (!DebuggerUtils.canExecute(commandContext, player, 1)) {
            commandContext.getSource().sendFailure(Component.literal("Has No Permission!"));
            return 0;
        }
        ServerPlayer target = EntityArgument.getPlayer(commandContext, "target");
        RegPlayerMinionComponent.PLAYER_MINION_DATA.get(target).clear();
        RegPlayerMinionComponent.PLAYER_MINION_DATA.sync(target);
        commandContext.getSource().sendSuccess(() -> {return Component.literal("Minion Data Cleared!");}, false);
        return 1;
    }

    private static int clearPlayerManaData(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        ServerPlayer player = commandContext.getSource().getPlayer();
        if (!DebuggerUtils.canExecute(commandContext, player, 1)) {
            commandContext.getSource().sendFailure(Component.literal("Has No Permission!"));
            return 0;
        }
        ServerPlayer target = EntityArgument.getPlayer(commandContext, "target");
        RegManaComponent.MANA.get(target).clear();
        RegManaComponent.MANA.sync(target);
        commandContext.getSource().sendSuccess(() -> {return Component.literal("Mana Data Cleared!");}, false);
        return 1;
    }

    private static int FC_Menu(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        ServerPlayer player = commandContext.getSource().getPlayer();
        if (player == null) {
            return 0;
        }
        ModPacketsS2CServer.sendOpenFCSMenu(player);
        return 1;
    }

    private static int FC_Save(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        ServerPlayer player = commandContext.getSource().getPlayer();
        if (player == null) {
            return 0;
        }
        Identifier formID = null;
        try {
            formID = commandContext.getArgument("form", Identifier.class);
        } catch (Exception e) {
            formID = FormUtils.getPlayerForm(player).getFormID();
        }
        String type = commandContext.getArgument("type", String.class);
        String slotName = commandContext.getArgument("slot_name", String.class);
        ModPacketsS2CServer.sendModifyFCDData(player, "save", formID, type, slotName, "", "");
        return 1;
    }

    private static int FC_Load(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        ServerPlayer player = commandContext.getSource().getPlayer();
        if (player == null) {
            return 0;
        }
        Identifier formID = null;
        try {
            formID = commandContext.getArgument("form", Identifier.class);
        } catch (Exception e) {
            formID = FormUtils.getPlayerForm(player).getFormID();
        }
        String type = commandContext.getArgument("type", String.class);
        String slotName = commandContext.getArgument("slot_name", String.class);
        ModPacketsS2CServer.sendModifyFCDData(player, "load", formID, type, slotName, "", "");
        return 1;
    }

    private static int FC_Delete(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        ServerPlayer player = commandContext.getSource().getPlayer();
        if (player == null) {
            return 0;
        }
        Identifier formID = null;
        try {
            formID = commandContext.getArgument("form", Identifier.class);
        } catch (Exception e) {
            formID = FormUtils.getPlayerForm(player).getFormID();
        }
        String type = commandContext.getArgument("type", String.class);
        String slotName = commandContext.getArgument("slot_name", String.class);
        ModPacketsS2CServer.sendModifyFCDData(player, "delete", formID, type, slotName, "", "");
        return 1;
    }

    private static final Identifier NO_ID = ShapeShifterCurseFabric.identifier("empty");

    private static int FC_Config(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        ServerPlayer player = commandContext.getSource().getPlayer();
        if (player == null) {
            return 0;
        }
        String type = commandContext.getArgument("type", String.class);
        ModPacketsS2CServer.sendModifyFCDData(player, "config", NO_ID, type, "", "", "");
        return 1;
    }

    private static int FC_List(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        ServerPlayer player = commandContext.getSource().getPlayer();
        if (player == null) {
            return 0;
        }
        Identifier formID = null;
        try {
            formID = commandContext.getArgument("form", Identifier.class);
        } catch (Exception e) {
            formID = FormUtils.getPlayerForm(player).getFormID();
        }
        String type = commandContext.getArgument("type", String.class);
        ModPacketsS2CServer.sendModifyFCDData(player, "list", formID, type, "", "", "");
        return 1;
    }

    private static int FC_ToChat(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        ServerPlayer player = commandContext.getSource().getPlayer();
        if (player == null) {
            return 0;
        }
        String type = commandContext.getArgument("type", String.class);
        String messageType = commandContext.getArgument("message_type", String.class);
        String encodeType = commandContext.getArgument("encode_type", String.class);
        FormTextureUtils.ColorSetting colorSetting = FormColorData.ABGR2ARGB(RegPlayerSkinComponent.SKIN_SETTINGS.get(player).getFormColor());
        String Data = "";
        switch (encodeType) {
            case "base64" -> {
                Data = FormColorData.ColorSettingtoString(colorSetting, true);
            }
            case "hex" -> {
                Data = FormColorData.ColorSettingtoString(colorSetting, false);
            }
        }
        switch (messageType) {
            case "raw" -> {
            }
            case "command" -> {
                Data = "/shape_shifter_curse form_color set_color_from_string \"" + Data + "\"";
            }
        }
        Component text = Component.translatable("message.shape-shifter-curse.form_color_data", player.getName());
        text = FormColorData.appendCopyableText(text, Data);
        switch (type) {
            case "local" -> {
                player.displayClientMessage(text, false);
            }
            case "server" -> {
                Objects.requireNonNull(player.getServer()).getPlayerList().broadcastSystemMessage(text, false);
            }
        }
        return 1;
    }

    private static int FC_SetColorFromString(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        ServerPlayer player = commandContext.getSource().getPlayer();
        if (player == null) {
            return 0;
        }
        String colorSettingString = commandContext.getArgument("color_string", String.class);
        try {
            FormTextureUtils.ColorSetting colorSetting = FormColorData.ColorSettingFormString(colorSettingString);
            if (colorSetting != null) {
                RegPlayerSkinComponent.SKIN_SETTINGS.get(player).setFormColor(FormColorData.ARGB2ABGR(colorSetting));
                RegPlayerSkinComponent.SKIN_SETTINGS.sync(player);
                return 1;
            }
        } catch (Exception e) {
            player.createCommandSourceStack().sendFailure(Component.literal("Error to apply color setting from string"));
            return 0;
        }
        return 0;
    }

    private static int SU_Command(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        // 需要开启配置后才能使用 毕竟如果还允许权限2 那么就能实现提权了
        ServerPlayer player = commandContext.getSource().getPlayer();
        if (!DebuggerUtils.canExecute(commandContext, player, 2)) {
            commandContext.getSource().sendFailure(Component.literal("Has No Permission!"));
            return 0;
        }
        if (player == null) {
            return 0;
        }
        int level = commandContext.getArgument("level", Integer.class);
        try {
            SuperUserUtils.setSULevel(player, level);
            player.getServer().getPlayerList().sendPlayerPermissionLevel(player);
            player.createCommandSourceStack().sendSuccess(() -> Component.literal("Set SU level to " + level), false);
        } catch (Exception e) {
            player.createCommandSourceStack().sendFailure(Component.literal("Error to set SU level"));
            return 0;
        }
        return 1;
    }

    private static int setDebugForm(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        ServerPlayer player = commandContext.getSource().getPlayer();
        if (!DebuggerUtils.canExecute(commandContext, player, 3)) {
            commandContext.getSource().sendFailure(Component.literal("Has No Permission!"));
            return 0;
        }
        ServerPlayer target = EntityArgument.getPlayer(commandContext, "target");
        IForm form = FormArgumentType.getForm(commandContext, "form");
        CommandSourceStack serverCommandSource = commandContext.getSource();
        if (form == null) {
            commandContext.getSource().sendFailure(Component.literal("Invalid Form Id!"));
            return 0;
        }
        try {
            EffectManager.clearTransformativeEffect(player);
            FormUtils._setForm(player, form);
            FormUtils.updateFormHistory(player, form);
            TransformManager.sendClientFirstPersonReset(player);
        }
        catch (Exception e){
            // 调试时在此打断点
            ShapeShifterCurseFabric.LOGGER.error("Exception when set form", e);
            throw e;
        }
        return 1;
    }

    private static int requestNewAuthData(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        ServerPlayer player = commandContext.getSource().getPlayer();
        if (player == null) {
            return 0;
        }
        try {
            ModPacketsS2CServer.requestPatronAuthFile(player, true);
        } catch (Exception e) {
            player.createCommandSourceStack().sendFailure(Component.literal("Error to request auth file"));
            return 0;
        }
        return 1;
    }
}
