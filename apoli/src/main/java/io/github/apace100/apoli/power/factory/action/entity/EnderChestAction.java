package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;

public class EnderChestAction {
    private static final Component TITLE = Component.translatable("container.enderchest");

    public static void action(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof Player player)) return;

        PlayerEnderChestContainer enderChestContainer = player.getEnderChestInventory();

        player.openMenu(
                new SimpleMenuProvider((i, inventory, _player) ->
                        ChestMenu.threeRows(i, inventory, enderChestContainer),
                        TITLE
                )
        );

        player.awardStat(Stats.OPEN_ENDERCHEST);
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Apoli.identifier("ender_chest"),
                new SerializableData(),
                EnderChestAction::action
        );
    }
}

