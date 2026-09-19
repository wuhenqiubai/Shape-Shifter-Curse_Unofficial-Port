package net.onixary.shapeShifterCurseFabric.perk;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class NormalPerk implements IPerk, IPerkClient {
    public final Identifier perkID;
    public final List<Identifier> powerAdd = new ArrayList<>();
    public final List<Identifier> powerRemove = new ArrayList<>();

    public boolean repeatable = false;
    public BiConsumer<Player, IForm> onGainFunc = null;
    public BiPredicate<Player, IForm> canGainCondition = null;

    public int xpCost = 0;

    public @Nullable Identifier Icon = null;
    public @Nullable Component Name = null;
    public @Nullable Component Desc = null;

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

    @Override
    public boolean canGain(Player player, IForm form) {
        return canGainCondition == null || canGainCondition.test(player, form);
    }

    @Override
    public int getXpCost() {
        return xpCost;
    }

    public NormalPerk XpCost(int xpCost) {
        this.xpCost = xpCost;
        return this;
    }

    public NormalPerk canGain(BiPredicate<Player, IForm> canGainCondition) {
        this.canGainCondition = canGainCondition;
        return this;
    }

    public NormalPerk setIcon(Identifier icon) {
        this.Icon = icon;
        return this;
    }

    public NormalPerk setName(Component name) {
        this.Name = name;
        return this;
    }

    public NormalPerk setDesc(Component desc) {
        this.Desc = desc;
        return this;
    }

    @Override
    public @Nullable Identifier getIcon() {
        if (Icon != null) {
            return Icon;
        }
        return IPerkClient.super.getIcon();
    }

    @Override
    public @Nullable Component getName() {
        if (Name != null) {
            return Name;
        }
        return IPerkClient.super.getName();
    }

    @Override
    public @Nullable Component getDesc() {
        if (Desc != null) {
            return Desc;
        }
        return IPerkClient.super.getDesc();
    }
}
