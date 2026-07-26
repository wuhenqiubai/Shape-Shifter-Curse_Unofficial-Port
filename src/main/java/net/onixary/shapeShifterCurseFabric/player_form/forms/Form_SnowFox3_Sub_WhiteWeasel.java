package net.onixary.shapeShifterCurseFabric.player_form.forms;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.player_form.NormalSubForm;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.utils.IPatronForm;
import net.onixary.shapeShifterCurseFabric.util.Verify.PatronDataSegment;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class Form_SnowFox3_Sub_WhiteWeasel extends NormalSubForm implements IPatronForm {
    public Form_SnowFox3_Sub_WhiteWeasel(ResourceLocation formID) {
        super(formID, RegPlayerForms.SNOW_FOX_3);
    }

    @Override
    public @Nullable Tuple<ResourceLocation, ResourceLocation> getRenderLayerOverride() {
        return new Tuple<>(ResourceLocation.fromNamespaceAndPath("origins", "origin"), ResourceLocation.fromNamespaceAndPath(this.getFormID().getNamespace(), "form_" + this.getFormID().getPath()));
    }

    @Override
    public boolean checkCanUse(@Nullable Player player, @Nullable UUID playerUUID, @Nullable PatronDataSegment patronData) {
        if (patronData == null || player == null) {
            return false;
        }
        return patronData.getLevel() >= 5;
    }
}
