package net.onixary.shapeShifterCurseFabric.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.onixary.shapeShifterCurseFabric.perk.*;
import net.onixary.shapeShifterCurseFabric.player_form.utils.*;
import io.github.apace100.apoli.component.PowerHolderComponent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static net.minecraft.commands.Commands.*;

public final class PerkCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> builder() {
        return literal("perk").requires(source -> source.hasPermission(2))
                .then(literal("list").executes(c -> execute(c, "list")))
                .then(literal("toggle_free").then(argument("enabled", BoolArgumentType.bool())
                        .executes(c -> execute(c, "toggle_free"))))
                .then(literal("unlock_all").executes(c -> execute(c, "unlock_all")))
                .then(literal("reset_all").executes(c -> execute(c, "reset_all")));
    }

    private static int execute(CommandContext<CommandSourceStack> context, String action) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        PlayerFormComponent component = PlayerFormComponent.COMPONENT.get(player);
        // nowPerkTree就是当前的PerkTree devCommand和名字一样 是调试命令 实际上就是修改当前的PerkTree而不是预览 根本就没做预览这个功能
        ResourceLocation treeID = component.nowPerkTree;
        PerkTree tree = RegPerks.getPerkTree(treeID);
        if (tree == null || tree.getAllNodes().isEmpty()) {
            source.sendFailure(Component.translatable("command.shape_shifter_curse.perk.empty"));
            return 0;
        }
        if (action.equals("toggle_free")) {
            boolean enabled = BoolArgumentType.getBool(context, "enabled");
            if (enabled) component.freePerkForms.add(component.nowForm.getFormID());
            else component.freePerkForms.remove(component.nowForm.getFormID());
            component.sync();
            source.sendSuccess(() -> Component.translatable("command.shape_shifter_curse.perk.free", component.nowForm.getFormID(), enabled), false);
            return 1;
        }
        List<ResourceLocation> unlocked = component.formPerkMap.computeIfAbsent(treeID, ignored -> new ArrayList<>());
        if (action.equals("list")) {
            source.sendSuccess(() -> Component.translatable("command.shape_shifter_curse.perk.tree", treeID, unlocked.size(), tree.getAllNodes().size()), false);
            for (PerkTree.PerkNode node : tree.getAllNodes()) {
                source.sendSuccess(() -> Component.literal(unlocked.contains(node.perkID) ? "[+] " : "[ ] ")
                        .append(RegPerks.getPerkName(node.perkID)).append(" (" + node.perkID + ")"), false);
            }
            return tree.getAllNodes().size();
        }
        int changed = 0;
        if (action.equals("reset_all")) {
            changed = unlocked.size();
            component.formPerkMap.remove(treeID);
        }
        if (action.equals("unlock_all")) {
            List<PerkTree.PerkNode> pending = tree.getAllNodes();
            boolean progress = true;
            while (!pending.isEmpty() && progress) {
                progress = false;
                Iterator<PerkTree.PerkNode> iterator = pending.iterator();
                while (iterator.hasNext()) {
                    PerkTree.PerkNode node = iterator.next();
                    if (unlocked.contains(node.perkID)) {
                        iterator.remove();
                        progress = true;
                        continue;
                    }
                    IPerk perk = RegPerks.getPerk(node.perkID);
                    if (perk != null && !perk.canRepeat() && unlocked.containsAll(node.dependents)) {
                        unlocked.add(node.perkID);
                        perk.onGain(player, component.nowForm);
                        changed++;
                        iterator.remove();
                        progress = true;
                    }
                }
            }
        }
        FormUtils.reApplyPower(player);
        component.sync();
        PowerHolderComponent.KEY.sync(player);
        int count = changed;
        source.sendSuccess(() -> Component.translatable("command.shape_shifter_curse.perk." + action, treeID, count), false);
        return changed;
    }
}
