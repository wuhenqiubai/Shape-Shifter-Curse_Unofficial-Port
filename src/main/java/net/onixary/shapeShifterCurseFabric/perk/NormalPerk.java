package net.onixary.shapeShifterCurseFabric.perk;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;
import net.onixary.shapeShifterCurseFabric.util.util.ISprite;
import net.onixary.shapeShifterCurseFabric.util.util.cost.BaseCost;
import net.onixary.shapeShifterCurseFabric.util.util.cost.ICost;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class NormalPerk implements IPerk, IPerkClient {
    public final ResourceLocation perkID;
    public final List<ResourceLocation> powerAdd = new ArrayList<>();
    public final List<ResourceLocation> powerRemove = new ArrayList<>();

    public boolean repeatable = false;
    public BiConsumer<Player, IForm> onGainFunc = null;
    public BiPredicate<Player, IForm> canGainCondition = null;

    public ICost cost = new BaseCost();

    public @Nullable ISprite Icon = null;
    public @Nullable Component Name = null;
    public @Nullable Component Desc = null;

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

    @Override
    public boolean canGain(Player player, IForm form) {
        return canGainCondition == null || canGainCondition.test(player, form);
    }

    @Override
    public ICost getCost() {
        return cost;
    }

    public NormalPerk cost(ICost cost) {
        this.cost = cost;
        return this;
    }

    public NormalPerk canGain(BiPredicate<Player, IForm> canGainCondition) {
        this.canGainCondition = canGainCondition;
        return this;
    }

    public NormalPerk setIcon(ISprite icon) {
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
    public @Nullable ISprite getIcon() {
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
