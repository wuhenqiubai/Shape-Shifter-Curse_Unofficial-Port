package net.onixary.shapeShifterCurseFabric.util.util.cost;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

public class BaseCost implements ICost {
    public static final Identifier id = ShapeShifterCurseFabric.identifier("base");
    private ICostType<?> type;
    private int amount;

    public BaseCost() {
        this(RegCostType.NO_COST, 0);
    }

    public BaseCost(CompoundTag nbt, HolderLookup.Provider registries) {
        this();
        readFromNBT(nbt, registries);
    }

    public BaseCost(ICostType<?> type, int amount) {
        this.type = type;
        this.amount = amount;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public ICostType<?> getType() {
        return type;
    }


    @Override
    public int getAmount() {
        return amount;
    }


    @Override
    public void writeToNBT(CompoundTag nbt, HolderLookup.Provider registries) {
        nbt.putString("type", type.getID().toString());
        nbt.putInt("amount", amount);
    }


    @Override
    public void readFromNBT(CompoundTag nbt, HolderLookup.Provider registries) {
        try {
            type = RegCostType.getCostType(Identifier.tryParse(nbt.getStringOr("type", "")));
            amount = nbt.getIntOr("amount", 0);
        } catch (Exception e) {
            type = RegCostType.NO_COST;
            amount = 0;
        }
    }
}
