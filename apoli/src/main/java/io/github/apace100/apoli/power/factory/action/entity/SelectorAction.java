package io.github.apace100.apoli.power.factory.action.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.util.ArgumentWrapper;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class SelectorAction {

    public static void action(SerializableData.Instance data, Entity entity) {

        MinecraftServer server = entity.level().getServer();
        if (server == null) return;

        EntitySelector selector = data.<ArgumentWrapper<EntitySelector>>get("selector").get();
        Predicate<Tuple<Entity, Entity>> biEntityCondition = data.get("bientity_condition");
        Consumer<Tuple<Entity, Entity>> biEntityAction = data.get("bientity_action");

        // [移植 b7a79a9] source 用实体（ServerPlayer.commandSource()）而非 CommandSource.NULL，让 EntitySelector 正确解析 @s/位置。
        CommandSourceStack source = new CommandSourceStack(
            entity instanceof ServerPlayer serverPlayer ? serverPlayer.commandSource() : CommandSource.NULL,
            entity.position(),
            entity.getRotationVector(),
            (ServerLevel) entity.level(),
            LevelBasedPermissionSet.GAMEMASTER,
            entity.getScoreboardName(),
            entity.getName(),
            server,
            entity
        );

        try {
            selector.findEntities(source)
                .stream()
                .filter(e -> biEntityCondition == null || biEntityCondition.test(new Tuple<>(entity, e)))
                .forEach(e -> biEntityAction.accept(new Tuple<>(entity, e)));
        }

        catch (CommandSyntaxException ignored) {}

    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("selector_action"),
            new SerializableData()
                .add("selector", ApoliDataTypes.ENTITIES_SELECTOR)
                .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null),
            SelectorAction::action
        );
    }

}
