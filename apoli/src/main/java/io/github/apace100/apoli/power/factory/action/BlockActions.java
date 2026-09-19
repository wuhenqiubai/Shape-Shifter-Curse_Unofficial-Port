package io.github.apace100.apoli.power.factory.action;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.block.AreaOfEffectAction;
import io.github.apace100.apoli.power.factory.action.block.BonemealAction;
import io.github.apace100.apoli.power.factory.action.block.ExplodeAction;
import io.github.apace100.apoli.power.factory.action.block.ModifyBlockStateAction;
import io.github.apace100.apoli.power.factory.action.meta.*;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Triple;

public class BlockActions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(AndAction.getFactory(ApoliDataTypes.BLOCK_ACTIONS));
        register(ChanceAction.getFactory(ApoliDataTypes.BLOCK_ACTION));
        register(IfElseAction.getFactory(ApoliDataTypes.BLOCK_ACTION, ApoliDataTypes.BLOCK_CONDITION,
            t -> new BlockInWorld(t.getLeft(), t.getMiddle(), true)));
        register(ChoiceAction.getFactory(ApoliDataTypes.BLOCK_ACTION));
        register(IfElseListAction.getFactory(ApoliDataTypes.BLOCK_ACTION, ApoliDataTypes.BLOCK_CONDITION,
            t -> new BlockInWorld(t.getLeft(), t.getMiddle(), true)));
        register(DelayAction.getFactory(ApoliDataTypes.BLOCK_ACTION));
        register(NothingAction.getFactory());
        register(SideAction.getFactory(ApoliDataTypes.BLOCK_ACTION, block -> !block.getLeft().isClientSide()));

        register(new ActionFactory<>(Apoli.identifier("offset"), new SerializableData()
            .add("action", ApoliDataTypes.BLOCK_ACTION)
            .add("x", SerializableDataTypes.INT, 0)
            .add("y", SerializableDataTypes.INT, 0)
            .add("z", SerializableDataTypes.INT, 0),
            (data, block) -> ((ActionFactory<Triple<Level, BlockPos, Direction>>.Instance)data.get("action")).accept(Triple.of(
                block.getLeft(),
                block.getMiddle().offset(data.getInt("x"), data.getInt("y"), data.getInt("z")),
                block.getRight())
            )));

        register(new ActionFactory<>(Apoli.identifier("set_block"), new SerializableData()
            .add("block", SerializableDataTypes.BLOCK_STATE),
            (data, block) -> {
                BlockState actualState = data.get("block");
                //actualState = Block.postProcessState(actualState, block.getLeft(), block.getMiddle());
                block.getLeft().setBlockAndUpdate(block.getMiddle(), actualState);
            }));
        register(new ActionFactory<>(Apoli.identifier("add_block"), new SerializableData()
            .add("block", SerializableDataTypes.BLOCK_STATE),
            (data, block) -> {
                BlockState actualState = data.get("block");
                BlockPos pos = block.getMiddle().relative(block.getRight());
                //actualState = Block.postProcessState(actualState, block.getLeft(), pos);
                block.getLeft().setBlockAndUpdate(pos, actualState);
            }));
        register(new ActionFactory<>(Apoli.identifier("execute_command"), new SerializableData()
            .add("command", SerializableDataTypes.STRING),
            (data, block) -> {
                MinecraftServer server = block.getLeft().getServer();
                if(server != null) {
                    String blockName = block.getLeft().getBlockState(block.getMiddle()).getBlock().getDescriptionId();
                    // [移植 b7a79a9] source 恒用 server，避免 CommandSource.NULL 抑制 /say 输出及使 /give 等命令失效。
                    CommandSourceStack source = new CommandSourceStack(
                        server,
                        new Vec3(block.getMiddle().getX() + 0.5, block.getMiddle().getY() + 0.5, block.getMiddle().getZ() + 0.5),
                        new Vec2(0, 0),
                        (ServerLevel)block.getLeft(),
                        Apoli.config.executeCommand.getPermissionHandler(),
                        blockName,
                        Component.translatable(blockName),
                        server,
                        null);
                    if(!Apoli.config.executeCommand.showOutput) {
                        source = source.withSuppressedOutput();
                    }
                    try {
                        String execCommand = data.getString("command").trim();
                        if(execCommand.startsWith("/")) {
                            execCommand = execCommand.substring(1);
                        }
                        server.getCommands().getDispatcher().execute(execCommand, source);
                    } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
                    }
                }
            }));
        register(BonemealAction.getFactory());
        register(ModifyBlockStateAction.getFactory());
        register(ExplodeAction.getFactory());
        register(AreaOfEffectAction.getFactory());
    }

    private static void register(ActionFactory<Triple<Level, BlockPos, Direction>> actionFactory) {
        Registry.register(ApoliRegistries.BLOCK_ACTION, actionFactory.getSerializerId(), actionFactory);
    }
}
