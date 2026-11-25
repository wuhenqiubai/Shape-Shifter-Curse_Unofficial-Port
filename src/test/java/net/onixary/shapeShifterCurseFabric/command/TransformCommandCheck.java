package net.onixary.shapeShifterCurseFabric.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;
import net.onixary.shapeShifterCurseFabric.player_form.utils.TransformManager;

import java.util.function.ToIntFunction;

public class TransformCommandCheck {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 200)
    public void commandsWaitForTheWholeTransformation(GameTestHelper test) {
        var player = test.makeMockServerPlayerInLevel();
        var source = player.createCommandSourceStack().withPermission(2);
        var commands = test.getLevel().getServer().getCommands();
        ToIntFunction<String> run = command -> {
            try {
                return commands.getDispatcher().execute("shape_shifter_curse " + command, source);
            } catch (CommandSyntaxException e) {
                return 0;
            }
        };
        var target = RegPlayerForms.BAT_1.getFormID();
        var alternate = RegPlayerForms.BAT_2.getFormID();
        test.assertTrue(run.applyAsInt("transform_to_form @s " + target) == 1, "Idle player can start transforming");
        test.assertTrue(TransformManager.isTransforming(player), "Transformation is running");
        test.assertTrue(run.applyAsInt("set_form @s " + alternate) == 0, "Cannot interrupt the current transformation");
        test.assertTrue(run.applyAsInt("transform_to_form @s " + alternate) == 0, "Cannot restart the animation");
        TransformManager.immediatelyTransform(player, RegPlayerForms.BAT_1);
        test.assertTrue(!TransformManager.isTransforming(player), "Transformation finished");
        test.assertTrue(FormUtils.getPlayerForm(player) == RegPlayerForms.BAT_1, "Original target reached");
        test.assertTrue(run.applyAsInt("set_form @s " + alternate) == 1, "Command unlocked after the animation");
        test.assertTrue(FormUtils.getPlayerForm(player) == RegPlayerForms.BAT_2, "New form applied");
        test.succeed();
    }
}
