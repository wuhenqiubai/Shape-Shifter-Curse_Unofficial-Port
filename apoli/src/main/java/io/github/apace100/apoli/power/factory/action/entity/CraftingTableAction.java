package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;

public class CraftingTableAction {
    private static final Component TITLE = Component.translatable("container.crafting");

    public static void action(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof Player player)) return;

        player.openMenu(new SimpleMenuProvider((syncId, inventory, _player) ->
            new CraftingMenu(syncId, inventory, ContainerLevelAccess.create(_player.level(), _player.blockPosition())), TITLE));

        player.awardStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Apoli.identifier("crafting_table"),
                new SerializableData(),
                CraftingTableAction::action
        );
    }
}
