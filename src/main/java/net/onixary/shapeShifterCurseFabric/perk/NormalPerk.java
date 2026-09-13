package net.onixary.shapeShifterCurseFabric.perk;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class NormalPerk implements IPerk {
    public final Identifier perkID;
    public final List<Identifier> powerAdd = new ArrayList<>();
    public final List<Identifier> powerRemove = new ArrayList<>();

    public boolean repeatable = false;
    public BiConsumer<Player, IForm> onGainFunc = null;

    public NormalPerk(Identifier perkID) {
        this.perkID = perkID;
    }

    public NormalPerk addPower(Identifier... powerIDs) {
        for (Identifier powerID : powerIDs) {
            if (!powerAdd.contains(powerID)) {
                powerAdd.add(powerID);
            }
        }
        return this;
    }

    public NormalPerk removePower(Identifier... powerIDs) {
        for (Identifier powerID : powerIDs) {
            if (!powerRemove.contains(powerID)) {
                powerRemove.add(powerID);
            }
        }
        return this;
    }

    @Override
    public void onGain(Player player, IForm form) {
        if (onGainFunc != null) {
            onGainFunc.accept(player, form);
        } else {
            IPerk.super.onGain(player, form);
        }
    }

    @Override
    public boolean canRepeat() {
        return repeatable;
    }

    public NormalPerk Repeat(BiConsumer<Player, IForm> onGainFunc) {
        if (onGainFunc == null) {
            repeatable = false;
        } else {
            repeatable = true;
        }
        this.onGainFunc = onGainFunc;
        return this;
    }

    @Override
    public Identifier getID() {
        return this.perkID;
    }

    @Override
    public void onLoad(Player player, IForm form) {
        Identifier powerSource = form.getFormLayer().getB();
        for (Identifier powerID : powerAdd) {
            FormUtils.applyPower(player, powerID, powerSource);
        }
        for (Identifier powerID : powerRemove) {
            FormUtils.removePower(player, powerID, powerSource);
        }
    }
}
