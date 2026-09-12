package net.onixary.shapeShifterCurseFabric.perk;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;

import java.util.ArrayList;
import java.util.List;

public class NormalPerk implements IPerk {
    public final ResourceLocation perkID;
    public final List<ResourceLocation> powerAdd = new ArrayList<>();
    public final List<ResourceLocation> powerRemove = new ArrayList<>();

    public NormalPerk(ResourceLocation perkID) {
        this.perkID = perkID;
    }

    public NormalPerk addPower(ResourceLocation... powerIDs) {
        for (ResourceLocation powerID : powerIDs) {
            if (!powerAdd.contains(powerID)) {
                powerAdd.add(powerID);
            }
        }
        return this;
    }

    public NormalPerk removePower(ResourceLocation... powerIDs) {
        for (ResourceLocation powerID : powerIDs) {
            if (!powerRemove.contains(powerID)) {
                powerRemove.add(powerID);
            }
        }
        return this;
    }

    @Override
    public ResourceLocation getID() {
        return this.perkID;
    }

    @Override
    public void onLoad(Player player, IForm form) {
        ResourceLocation powerSource = form.getFormLayer().getB();
        for (ResourceLocation powerID : powerAdd) {
            FormUtils.applyPower(player, powerID, powerSource);
        }
        for (ResourceLocation powerID : powerRemove) {
            FormUtils.removePower(player, powerID, powerSource);
        }
    }
}
