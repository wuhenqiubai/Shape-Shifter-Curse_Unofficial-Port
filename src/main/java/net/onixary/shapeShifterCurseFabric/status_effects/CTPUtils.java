package net.onixary.shapeShifterCurseFabric.status_effects;

import com.google.common.base.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.utils.PlayerFormComponent;
import net.onixary.shapeShifterCurseFabric.player_form.utils.RegPlayerFormComponent;

// Custom Transformative Potion Utils (CTP)
public class CTPUtils {
    public interface CTPFormIDHolder {
        ResourceLocation getCTPFormID();
        void setCTPFormID(ResourceLocation formID);
    }

    public static IForm getTransformativePotionForm(Player player) {
        if (player == null) {
            ShapeShifterCurseFabric.LOGGER.error("CustomTransformativeStatue PlayerEntity is null");
            return RegPlayerForms.ORIGINAL_BEFORE_ENABLE;
        }
        PlayerFormComponent playerFormComponent = RegPlayerFormComponent.PLAYER_FORM.get(player);
        if (playerFormComponent == null) {
            ShapeShifterCurseFabric.LOGGER.error("CustomTransformativeStatue PlayerFormComponent is null");
            return RegPlayerForms.ORIGINAL_BEFORE_ENABLE;
        }
        return RegPlayerForms.getPlayerFormOrDefault(playerFormComponent.customPotionFormID, RegPlayerForms.ORIGINAL_BEFORE_ENABLE);
    }

    public static void setTransformativePotionForm(Player player, ResourceLocation formID) {
        if (player == null) {
            ShapeShifterCurseFabric.LOGGER.error("CustomTransformativeStatue PlayerEntity is null");
            return;
        }
        PlayerFormComponent playerFormComponent = RegPlayerFormComponent.PLAYER_FORM.get(player);
        if (playerFormComponent == null) {
            ShapeShifterCurseFabric.LOGGER.error("CustomTransformativeStatue PlayerFormComponent is null");
            return;
        }
        if (!Objects.equal(playerFormComponent.customPotionFormID, formID)) {
            playerFormComponent.customPotionFormID = formID;
            RegPlayerFormComponent.PLAYER_FORM.sync(player);
        }
    }

    public static void resetTransformativePotionForm(Player player) {
        setTransformativePotionForm(player, RegPlayerForms.ORIGINAL_BEFORE_ENABLE.getFormID());
    }

    public static ResourceLocation getCTPFormIDFromNBT(CompoundTag nbtCompound) {
        if (nbtCompound == null) {
            return null;
        }
        if (nbtCompound.contains("targetForm")) {
            return ResourceLocation.tryParse(nbtCompound.getString("targetForm"));
        }
        return null;
    }

    public static void setCTPFormIDToNBT(CompoundTag nbtCompound, ResourceLocation formID) {
        if (nbtCompound == null) {
            return;
        }
        nbtCompound.putString("targetForm", formID.toString());
    }
}
