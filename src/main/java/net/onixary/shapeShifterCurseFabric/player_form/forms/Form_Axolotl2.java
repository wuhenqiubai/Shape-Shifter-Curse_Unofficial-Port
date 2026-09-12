package net.onixary.shapeShifterCurseFabric.player_form.forms;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AbstractAnimStateController;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateControllerDP.SwimAnimController;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateControllerDP.WithSneakAnimController;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateEnum;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimSystem;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimUtils;
import net.onixary.shapeShifterCurseFabric.player_form.NormalForm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Form_Axolotl2 extends NormalForm {
    public Form_Axolotl2(Identifier formID) {
        super(formID);
    }

    public static final AbstractAnimStateController SWIM_CONTROLLER = new SwimAnimController(new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("axolotl_2_swimming_idle")), new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("axolotl_2_swimming")));
    public static final AbstractAnimStateController IDLE_CONTROLLER = new WithSneakAnimController(null, new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("axolotl_2_crawling_idle_new")));
    public static final AbstractAnimStateController WALK_CONTROLLER = new WithSneakAnimController(null, new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("axolotl_2_crawling_new")));
    public static final AbstractAnimStateController JUMP_CONTROLLER = new WithSneakAnimController(null, new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("axolotl_2_crawling_jump")));
    public static final AbstractAnimStateController ATTACK_CONTROLLER = new WithSneakAnimController(null, new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("axolotl_2_crawling_attack_once")));
    public static final AbstractAnimStateController MINING_CONTROLLER = new WithSneakAnimController(null, new AnimUtils.AnimationHolderData(ShapeShifterCurseFabric.identifier("axolotl_2_crawling_tool_swing")));

    @Override
    public @Nullable AbstractAnimStateController getAnimStateController(Player player, AnimSystem.AnimSystemData animSystemData, @NotNull Identifier animStateID) {
        @Nullable AnimStateEnum animStateEnum = AnimStateEnum.getStateEnum(animStateID);
        if (animStateEnum != null) {
            return switch (animStateEnum) {
                case ANIM_STATE_SWIM -> SWIM_CONTROLLER;
                case ANIM_STATE_IDLE -> IDLE_CONTROLLER;
                case ANIM_STATE_WALK -> WALK_CONTROLLER;
                case ANIM_STATE_JUMP -> JUMP_CONTROLLER;
                case ANIM_STATE_ATTACK -> ATTACK_CONTROLLER;
                case ANIM_STATE_MINING -> MINING_CONTROLLER;
                default -> null;
            };
        }
        return super.getAnimStateController(player, animSystemData, animStateID);
    }
}