package io.github.apace100.apoli.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.CooldownPower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.VariableIntPower;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.scores.ScoreAccess;

import java.util.Optional;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ResourceCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            literal("resource").requires(cs -> cs.hasPermission(2))
                .then(literal("has")
                    .then(argument("target", EntityArgument.entity())
                        .then(argument("power", PowerTypeArgumentType.power())
                            .executes((command) -> resource(command, SubCommand.HAS))))
                )
                .then(literal("get")
                    .then(argument("target", EntityArgument.entity())
                        .then(argument("power", PowerTypeArgumentType.power())
                            .executes((command) -> resource(command, SubCommand.GET))))
                )
                .then(literal("set")
                    .then(argument("target", EntityArgument.entity())
                        .then(argument("power", PowerTypeArgumentType.power())
                            .then(argument("value", IntegerArgumentType.integer())
                                .executes((command) -> resource(command, SubCommand.SET)))))
                )
                .then(literal("change")
                    .then(argument("target", EntityArgument.entity())
                        .then(argument("power", PowerTypeArgumentType.power())
                            .then(argument("value", IntegerArgumentType.integer())
                                .executes((command) -> resource(command, SubCommand.CHANGE)))))
                )
                .then(literal("operation")
                    .then(argument("target", EntityArgument.entity())
                        .then(argument("power", PowerTypeArgumentType.power())
                            .then(argument("operation", PowerOperation.operation())
                                .then(argument("entity", ScoreHolderArgument.scoreHolder())
                                    .then(argument("objective", ObjectiveArgument.objective())
                                        .executes((command) -> resource(command, SubCommand.OPERATION)))))))
                )
        );
    }

    public enum SubCommand {
        HAS, GET, SET, CHANGE, OPERATION
    }

    // This is a cleaner method than sticking it into every subcommand
    private static int resource(CommandContext<CommandSourceStack> command, SubCommand sub) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(command, "target");
        if(!(player instanceof LivingEntity)) {
        }
        PowerType<?> powerType = PowerTypeArgumentType.getPower(command, "power");
        Optional<PowerHolderComponent> phc = PowerHolderComponent.KEY.maybeGet(player);
        if(phc.isEmpty()) {
            command.getSource().sendFailure(Component.translatable("commands.apoli.resource.invalid_entity"));
            return 0;
        }
        Power power = PowerHolderComponent.KEY.get(player).getPower(powerType);

        if (power instanceof VariableIntPower vIntPower) {
            switch (sub)
            {
                case HAS ->
                {
                    command.getSource().sendSuccess(() -> Component.translatable("commands.execute.conditional.pass"), true);
                    return 1;
                }
                case GET ->
                {
                    int i = vIntPower.getValue();
                    command.getSource().sendSuccess(() -> Component.translatable("commands.scoreboard.players.get.success", player.getScoreboardName(), i, powerType.getIdentifier()), true);
                    return i;
                }
                case SET ->
                {
                    int i = IntegerArgumentType.getInteger(command, "value");
                    vIntPower.setValue(i);
                    PowerHolderComponent.syncPower(player, powerType);
                    command.getSource().sendSuccess(() -> Component.translatable("commands.scoreboard.players.set.success.single", powerType.getIdentifier(), player.getScoreboardName(), i), true);
                    return 1;
                }
                case CHANGE ->
                {
                    int i = IntegerArgumentType.getInteger(command, "value");
                    int total = vIntPower.getValue() + i;
                    vIntPower.setValue(total);
                    PowerHolderComponent.syncPower(player, powerType);
                    command.getSource().sendSuccess(() -> Component.translatable("commands.scoreboard.players.add.success.single", i, powerType.getIdentifier(), player.getScoreboardName(), total), true);
                    return 1;
                }
                case OPERATION ->
                {
                    ScoreAccess score = command.getSource().getServer().getScoreboard().getOrCreatePlayerScore(ScoreHolderArgument.getName(command, "entity"), ObjectiveArgument.getObjective(command, "objective"));
                    command.getArgument("operation", PowerOperation.Operation.class).apply(vIntPower, score);
                    PowerHolderComponent.syncPower(player, powerType);
                    command.getSource().sendSuccess(() -> Component.translatable("commands.scoreboard.players.operation.success.single", powerType.getIdentifier(), player.getScoreboardName(), vIntPower.getValue()), true);
                    return 1;
                }
            }
        } else if(power instanceof CooldownPower cooldownPower) {
            switch (sub)
            {
                case HAS ->
                {
                    command.getSource().sendSuccess(() -> Component.translatable("commands.execute.conditional.pass"), true);
                    return 1;
                }
                case GET ->
                {
                    int i = cooldownPower.getRemainingTicks();
                    command.getSource().sendSuccess(() -> Component.translatable("commands.scoreboard.players.get.success", player.getScoreboardName(), i, powerType.getIdentifier()), true);
                    return i;
                }
                case SET ->
                {
                    int i = IntegerArgumentType.getInteger(command, "value");
                    cooldownPower.setCooldown(i);
                    PowerHolderComponent.syncPower(player, powerType);
                    command.getSource().sendSuccess(() -> Component.translatable("commands.scoreboard.players.set.success.single", powerType.getIdentifier(), player.getScoreboardName(), i), true);
                    return 1;
                }
                case CHANGE ->
                {
                    int i = IntegerArgumentType.getInteger(command, "value");
                    cooldownPower.modify(i);
                    PowerHolderComponent.syncPower(player, powerType);
                    command.getSource().sendSuccess(() -> Component.translatable("commands.scoreboard.players.add.success.single", i, powerType.getIdentifier(), player.getScoreboardName(), cooldownPower.getRemainingTicks()), true);
                    return 1;
                }
                case OPERATION ->
                {
                    ScoreAccess score = command.getSource().getServer().getScoreboard().getOrCreatePlayerScore(ScoreHolderArgument.getName(command, "entity"), ObjectiveArgument.getObjective(command, "objective"));
                    command.getArgument("operation", PowerOperation.Operation.class).apply(cooldownPower, score);
                    PowerHolderComponent.syncPower(player, powerType);
                    command.getSource().sendSuccess(() -> Component.translatable("commands.scoreboard.players.operation.success.single", powerType.getIdentifier(), player.getScoreboardName(), cooldownPower.getRemainingTicks()), true);
                    return 1;
                }
            }
        } else {
            switch (sub)
            {
                case HAS ->
                {
                    command.getSource().sendFailure(Component.translatable("commands.execute.conditional.fail"));
                    return 0;
                }
                case GET ->
                {
                    command.getSource().sendFailure(Component.translatable("commands.scoreboard.players.get.null", powerType.getIdentifier(), player.getScoreboardName()));
                    return 0;
                }
                case SET, CHANGE, OPERATION ->
                {
                    // This translation is a bit of a stretch, as it reads "No relevant score holders could be found"
                    command.getSource().sendFailure(Component.translatable("argument.scoreHolder.empty"));
                    return 0;
                }
            }
        }
        return 0;
    }
}