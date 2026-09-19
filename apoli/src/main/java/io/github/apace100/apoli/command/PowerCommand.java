package io.github.apace100.apoli.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class PowerCommand {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
			literal("power").requires(scs -> scs.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
				.then(literal("grant")
					.then(argument("targets", EntityArgument.entities())
						.then(argument("power", PowerTypeArgumentType.power())
							.executes(context -> grantPower(context, false))
                            .then(argument("source", IdentifierArgument.id())
                                .executes(context -> grantPower(context, true)))))
				)
				.then(literal("revoke")
					.then(argument("targets", EntityArgument.entities())
						.then(argument("power", PowerTypeArgumentType.power())
							.executes(context -> revokePower(context, false))
							.then(argument("source", IdentifierArgument.id())
								.executes(context -> revokePower(context, true)))))
				)
				.then(literal("revokeall")
					.then(argument("targets", EntityArgument.entities())
						.then(argument("source", IdentifierArgument.id())
							.executes(PowerCommand::revokeAllPowers)))
				)
				.then(literal("list")
					.then(argument("target", EntityArgument.entity())
						.executes(context -> listPowers(context, false))
						.then(argument("subpowers", BoolArgumentType.bool())
							.executes(context -> listPowers(context, true))))
				)
				.then(literal("has")
					.then(argument("targets", EntityArgument.entities())
						.then(argument("power", PowerTypeArgumentType.power())
							.executes(PowerCommand::hasPower)))
				)
				.then(literal("sources")
					.then(argument("target", EntityArgument.entity())
						.then(argument("power", PowerTypeArgumentType.power())
							.executes(PowerCommand::getSourcesFromPower)))
				)
				.then(literal("remove")
					.then(argument("targets", EntityArgument.entities())
						.then(argument("power", PowerTypeArgumentType.power())
							.executes(PowerCommand::removePower)))
				)
				.then(literal("clear")
					.then(argument("targets", EntityArgument.entities())
						.executes(PowerCommand::clearAllPowers))
				)
		);
	}

	private static int grantPower(CommandContext<CommandSourceStack> context, boolean isSourceSpecified) throws CommandSyntaxException {

		CommandSourceStack source = context.getSource();
		Collection<? extends Entity> targets = EntityArgument.getEntities(context, "targets");
		PowerType<?> powerType = PowerTypeArgumentType.getPower(context, "power");
		Identifier powerSource = isSourceSpecified ? IdentifierArgument.getId(context, "source") : Apoli.identifier("command");

		LinkedList<Entity> nonLivingTargets = new LinkedList<>();
		LinkedList<LivingEntity> livingTargets = new LinkedList<>();
		LinkedList<LivingEntity> processedLivingTargets = new LinkedList<>();

		for (Entity target : targets) {

			if (!(target instanceof LivingEntity livingTarget)) {
				nonLivingTargets.add(target);
				continue;
			}

			livingTargets.add(livingTarget);
			PowerHolderComponent powerHolderComponent = PowerHolderComponent.KEY.get(livingTarget);

			if (!powerHolderComponent.addPower(powerType, powerSource)) continue;

			powerHolderComponent.sync();
			processedLivingTargets.add(livingTarget);

		}

		if (!processedLivingTargets.isEmpty()) {
			if (isSourceSpecified) {
				if (processedLivingTargets.size() == 1) source.sendSuccess(() -> Component.translatable("commands.apoli.grant.success.single", processedLivingTargets.getFirst().getDisplayName(), powerType.getName()), true);
				else source.sendSuccess(() -> Component.translatable("commands.apoli.grant.success.multiple", processedLivingTargets.size(), powerType.getName()), true);
			}
			else {
				if (processedLivingTargets.size() == 1) source.sendSuccess(() -> Component.translatable("commands.apoli.grant_from_source.success.single", processedLivingTargets.getFirst().getDisplayName(), powerType.getName(), powerSource), true);
				else source.sendSuccess(() -> Component.translatable("commands.apoli.grant_from_source.success.multiple", processedLivingTargets.size(), powerType.getName(), powerSource), true);
			}
		}

		else if (!livingTargets.isEmpty()) {
			if (livingTargets.size() == 1) source.sendFailure(Component.translatable("commands.apoli.grant.fail.single", livingTargets.getFirst().getDisplayName(), powerType.getName(), powerSource));
			else source.sendFailure(Component.translatable("commands.apoli.grant.fail.multiple", livingTargets.size(), powerType.getName(), powerSource));
		}

		else if (!nonLivingTargets.isEmpty()) {
			if (nonLivingTargets.size() == 1) source.sendFailure(Component.translatable("commands.apoli.grant.invalid_entity", nonLivingTargets.getFirst().getDisplayName()));
			else source.sendFailure(Component.translatable("commands.apoli.grant.invalid_entities", nonLivingTargets.size()));
		}

		return processedLivingTargets.size();

	}

	private static int revokePower(CommandContext<CommandSourceStack> context, boolean isSourceSpecified) throws CommandSyntaxException {

		CommandSourceStack source = context.getSource();
		Collection<? extends Entity> targets = EntityArgument.getEntities(context, "targets");
		PowerType<?> powerType = PowerTypeArgumentType.getPower(context, "power");
		Identifier powerSource = isSourceSpecified ? IdentifierArgument.getId(context, "source") : Apoli.identifier("command");

		LinkedList<Entity> nonLivingTargets = new LinkedList<>();
		LinkedList<LivingEntity> livingTargets = new LinkedList<>();
		LinkedList<LivingEntity> processedLivingTargets = new LinkedList<>();

		for (Entity target : targets) {

			if (!(target instanceof LivingEntity livingTarget)) {
				nonLivingTargets.add(target);
				continue;
			}

			livingTargets.add(livingTarget);
			PowerHolderComponent powerHolderComponent = PowerHolderComponent.KEY.get(livingTarget);

			if (!(powerHolderComponent.hasPower(powerType, powerSource))) continue;

			powerHolderComponent.removePower(powerType, powerSource);
			powerHolderComponent.sync();
			processedLivingTargets.add(livingTarget);

		}

		if (!processedLivingTargets.isEmpty()) {
			if (!isSourceSpecified) {
				if (processedLivingTargets.size() == 1) source.sendSuccess(() -> Component.translatable("commands.apoli.revoke.success.single", processedLivingTargets.getFirst().getDisplayName(), powerType.getName()), true);
				else source.sendSuccess(() -> Component.translatable("commands.apoli.revoke.success.multiple", processedLivingTargets.size(), powerType.getName()), true);
			}
			else {
				if (processedLivingTargets.size() == 1) source.sendSuccess(() -> Component.translatable("commands.apoli.revoke_from_source.success.single", processedLivingTargets.getFirst().getDisplayName(), powerType.getName(), powerSource), true);
				else source.sendSuccess(() -> Component.translatable("commands.apoli.revoke_from_source.success.multiple", processedLivingTargets.size(), powerType.getName(), powerSource), true);
			}
		}

		else if (!livingTargets.isEmpty()) {
			if (livingTargets.size() == 1) source.sendFailure(Component.translatable("commands.apoli.revoke.fail.single", livingTargets.getFirst().getDisplayName(), powerType.getName(), powerSource));
			else source.sendFailure(Component.translatable("commands.apoli.revoke.fail.multiple", powerType.getName(), powerSource));
		}

		else if (!nonLivingTargets.isEmpty()) {
			if (nonLivingTargets.size() == 1) source.sendFailure(Component.translatable("commands.apoli.revoke.invalid_entity", nonLivingTargets.getFirst().getDisplayName(), powerSource));
			else source.sendFailure(Component.translatable("commands.apoli.revoke.invalid_entities", nonLivingTargets.size(), powerSource));
		}

		return processedLivingTargets.size();

	}

	private static int revokeAllPowers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

		CommandSourceStack source = context.getSource();
		Collection<? extends Entity> targets = EntityArgument.getEntities(context, "targets");
		Identifier powerSource = IdentifierArgument.getId(context, "source");

		int revokedPowers = 0;
		LinkedList<Entity> nonLivingTargets = new LinkedList<>();
		LinkedList<LivingEntity> livingTargets = new LinkedList<>();
		LinkedList<LivingEntity> processedLivingTargets = new LinkedList<>();

		for (Entity target : targets) {

			if (!(target instanceof LivingEntity livingTarget)) {
				nonLivingTargets.add(target);
				continue;
			}

			livingTargets.add(livingTarget);
			PowerHolderComponent powerHolderComponent = PowerHolderComponent.KEY.get(target);
			int i = powerHolderComponent.removeAllPowersFromSource(powerSource);
			if (i <= 0) continue;

			powerHolderComponent.sync();
			revokedPowers += i;
			processedLivingTargets.add(livingTarget);

		}

		if (!processedLivingTargets.isEmpty()) {
			final int currentRevokedPowers = revokedPowers;
			if (processedLivingTargets.size() == 1) source.sendSuccess(() -> Component.translatable("commands.apoli.revoke_all.success.single", processedLivingTargets.getFirst().getDisplayName(), currentRevokedPowers, powerSource), true);
			else source.sendSuccess(() -> Component.translatable("commands.apoli.revoke_all.success.multiple", processedLivingTargets.size(), currentRevokedPowers, powerSource), true);
		}

		else if (!livingTargets.isEmpty()) {
			if (livingTargets.size() == 1) source.sendFailure(Component.translatable("commands.apoli.revoke_all.fail.single", livingTargets.getFirst().getDisplayName(), powerSource));
			else source.sendFailure(Component.translatable("commands.apoli.revoke_all.fail.multiple", powerSource));
		}

		else if (!nonLivingTargets.isEmpty()) {
			if (nonLivingTargets.size() == 1) source.sendFailure(Component.translatable("commands.apoli.revoke_all.invalid_entity", nonLivingTargets.getFirst().getDisplayName(), powerSource));
			else source.sendFailure(Component.translatable("commands.apoli.revoke_all.invalid_entities", nonLivingTargets.size(), powerSource));
		}

		return processedLivingTargets.size();

	}

	private static int listPowers(CommandContext<CommandSourceStack> context, boolean includeSubpowers) throws CommandSyntaxException {

		CommandSourceStack source = context.getSource();
		Entity target = EntityArgument.getEntity(context, "target");
		List<Component> powers = new LinkedList<>();

		int powerCount = 0;

		if (!(target instanceof LivingEntity livingTarget)) {
			source.sendFailure(Component.translatable("commands.apoli.list.fail", target.getDisplayName()));
			return powerCount;
		}

		PowerHolderComponent powerHolderComponent = PowerHolderComponent.KEY.get(livingTarget);
		for (PowerType<?> powerType : powerHolderComponent.getPowerTypes(includeSubpowers)) {

			List<Component> powerSources = new LinkedList<>();
            powerHolderComponent.getSources(powerType).forEach(powerSource -> powerSources.add(Component.nullToEmpty(powerSource.toString())));

            HoverEvent powerSourcesOnHover = new HoverEvent.ShowText(
                Component.translatable(powerSources.size() == 1 ? "commands.apoli.list.source" : "commands.apoli.list.sources", ComponentUtils.formatList(powerSources, Component.nullToEmpty(", ")))
            );

			Component power = Component.literal(powerType.getIdentifier().toString()).setStyle(Style.EMPTY.withHoverEvent(powerSourcesOnHover));
			powers.add(power);
			powerCount++;

		}

		if (powerCount > 0)
		{
			final int currentPowerCount = powerCount;
			source.sendSuccess(() -> Component.translatable("commands.apoli.list.pass", livingTarget.getDisplayName(), currentPowerCount, ComponentUtils.formatList(powers, Component.nullToEmpty(", "))), true);
		}
		else source.sendFailure(Component.translatable("commands.apoli.list.fail", livingTarget.getDisplayName()));

		return powerCount;

	}

	private static int hasPower(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

		CommandSourceStack source = context.getSource();
		Collection<? extends Entity> targets = EntityArgument.getEntities(context, "targets");
		PowerType<?> powerType = PowerTypeArgumentType.getPower(context, "power");

		List<LivingEntity> processedLivingTargets = new LinkedList<>();
		for (Entity target : targets) {

			if (!(target instanceof LivingEntity livingTarget)) continue;
			PowerHolderComponent powerHolderComponent = PowerHolderComponent.KEY.get(livingTarget);
			
			if (!powerHolderComponent.hasPower(powerType)) continue;
			processedLivingTargets.add(livingTarget);

		}
		
		if (!processedLivingTargets.isEmpty()) {
			if (processedLivingTargets.size() == 1) source.sendSuccess(() -> Component.translatable("commands.execute.conditional.pass"), true);
			else source.sendSuccess(() -> Component.translatable("commands.execute.conditional.pass_count", processedLivingTargets.size()), true);
		}
		
		else {
			if (targets.size() == 1) source.sendFailure(Component.translatable("commands.execute.conditional.fail"));
			else source.sendFailure(Component.translatable("commands.execute.conditional.fail_count", targets.size()));
		}

		return processedLivingTargets.size();

	}

	private static int getSourcesFromPower(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

		CommandSourceStack source = context.getSource();
		Entity target = EntityArgument.getEntity(context, "target");
		PowerType<?> powerType = PowerTypeArgumentType.getPower(context, "power");
		StringBuilder powerSources = new StringBuilder();

		int powerSourceCount = 0;

		if (!(target instanceof LivingEntity livingTarget)) {
			source.sendFailure(Component.translatable("commands.apoli.sources.fail", target.getDisplayName(), powerType.getName()));
			return powerSourceCount;
		}

		PowerHolderComponent powerHolderComponent = PowerHolderComponent.KEY.get(livingTarget);
		for (Identifier powerSource : powerHolderComponent.getSources(powerType)) {
			if (powerSourceCount > 0) powerSources.append(", ");
			powerSources.append(powerSource.toString());
			powerSourceCount++;
		}

		if (powerSourceCount > 0)
		{
			final int currentPowerSourceCount = powerSourceCount;
			source.sendSuccess(() -> Component.translatable("commands.apoli.sources.pass", livingTarget.getDisplayName(), currentPowerSourceCount, powerType.getName(), powerSources), true);
		}
		else source.sendFailure(Component.translatable("commands.apoli.sources.fail", livingTarget.getDisplayName(), powerType.getName()));
			
		return powerSourceCount;

	}

	private static int removePower(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

		CommandSourceStack source = context.getSource();
		Collection<? extends Entity> targets = EntityArgument.getEntities(context, "targets");
		PowerType<?> powerType = PowerTypeArgumentType.getPower(context, "power");

		LinkedList<Entity> nonLivingTargets = new LinkedList<>();
		LinkedList<LivingEntity> livingTargets = new LinkedList<>();
		LinkedList<LivingEntity> processedLivingTargets = new LinkedList<>();

		for (Entity target : targets) {

			if (!(target instanceof LivingEntity livingTarget)) {
				nonLivingTargets.add(target);
				continue;
			}

			livingTargets.add(livingTarget);
			PowerHolderComponent powerHolderComponent = PowerHolderComponent.KEY.get(livingTarget);
			List<Identifier> powerSources = powerHolderComponent.getSources(powerType);
			if (powerSources.isEmpty()) continue;
			
			for (Identifier powerSource : powerSources) {
				powerHolderComponent.removePower(powerType, powerSource);
			}
			
			powerHolderComponent.sync();
			processedLivingTargets.add(livingTarget);

		}

		if (!processedLivingTargets.isEmpty()) {
			if (processedLivingTargets.size() == 1) source.sendSuccess(() -> Component.translatable("commands.apoli.remove.success.single", processedLivingTargets.getFirst().getDisplayName(), powerType.getName()), true);
			else source.sendSuccess(() -> Component.translatable("commands.apoli.remove.success.multiple", processedLivingTargets.size(), powerType.getName()), true);
		}

		else if (!livingTargets.isEmpty()) {
			if (livingTargets.size() == 1) source.sendFailure(Component.translatable("commands.apoli.remove.fail.single", livingTargets.getFirst().getDisplayName(), powerType.getName()));
			else source.sendFailure(Component.translatable("commands.apoli.remove.fail.multiple", powerType.getName()));
		}

		else if (!nonLivingTargets.isEmpty()) {
			if (nonLivingTargets.size() == 1) source.sendFailure(Component.translatable("commands.apoli.remove.invalid_entity", nonLivingTargets.getFirst().getDisplayName()));
			else source.sendFailure(Component.translatable("commands.apoli.remove.invalid_entities", nonLivingTargets.size()));
		}

		return processedLivingTargets.size();

	}

	private static int clearAllPowers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

		CommandSourceStack source = context.getSource();
		Collection<? extends Entity> targets = EntityArgument.getEntities(context, "targets");
		LinkedList<Entity> nonLivingTargets = new LinkedList<>();
		LinkedList<LivingEntity> livingTargets = new LinkedList<>();
		LinkedList<LivingEntity> processedLivingTargets = new LinkedList<>();

		int clearedPowers = 0;
		for (Entity target : targets) {

			if (!(target instanceof LivingEntity livingTarget)) {
				nonLivingTargets.add(target);
				continue;
			}

			livingTargets.add(livingTarget);
			PowerHolderComponent powerHolderComponent = PowerHolderComponent.KEY.get(livingTarget);
			Set<PowerType<?>> powerTypes = powerHolderComponent.getPowerTypes(false);
			if (powerTypes.isEmpty()) continue;
			
			for (PowerType<?> powerType : powerTypes) {
				List<Identifier> powerSources = powerHolderComponent.getSources(powerType);
				powerSources.forEach(powerHolderComponent::removeAllPowersFromSource);
			}
			
			powerHolderComponent.sync();
			clearedPowers += powerTypes.size();
			processedLivingTargets.add(livingTarget);

		}

		if (!processedLivingTargets.isEmpty()) {
			final int currentClearedPowers = clearedPowers;
			if (processedLivingTargets.size() == 1) source.sendSuccess(() -> Component.translatable("commands.apoli.clear.success.single", processedLivingTargets.getFirst().getDisplayName(), currentClearedPowers), true);
			else source.sendSuccess(() -> Component.translatable("commands.apoli.clear.success.multiple", processedLivingTargets.size(), currentClearedPowers), true);
		}

		else if (!livingTargets.isEmpty()) {
			if (livingTargets.size() == 1) source.sendFailure(Component.translatable("commands.apoli.clear.fail.single", livingTargets.getFirst().getDisplayName()));
			else source.sendFailure(Component.translatable("commands.apoli.clear.fail.multiple"));
		}

		else if (!nonLivingTargets.isEmpty()) {
			if (nonLivingTargets.size() == 1) source.sendFailure(Component.translatable("commands.apoli.clear.invalid_entity", nonLivingTargets.getFirst().getDisplayName()));
			else source.sendFailure(Component.translatable("commands.apoli.clear.invalid_entities", nonLivingTargets.size()));
		}

		return clearedPowers;

	}

}
