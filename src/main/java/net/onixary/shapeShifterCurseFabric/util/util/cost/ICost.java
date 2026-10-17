package net.onixary.shapeShifterCurseFabric.util.util.cost;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public interface ICost {
    public Identifier getId();

    public ICostType<?> getType();

    public int getAmount();


    public void writeToNBT(CompoundTag nbt, HolderLookup.Provider registries);

    public void readFromNBT(CompoundTag nbt, HolderLookup.Provider registries);

    public default void __writeToNBT(CompoundTag nbt, HolderLookup.Provider registries) {
        nbt.putString("COST_ID", getId().toString());
        this.writeToNBT(nbt, registries);
    }

    public static @NotNull ICost fromNBT(CompoundTag nbt, HolderLookup.Provider registries) {
        if (!nbt.contains("COST_ID")) {
            throw new IllegalArgumentException("NBT does not contain a cost id");
        }
        Identifier id = Identifier.parse(nbt.getStringOr("COST_ID", ""));
        BiFunction<CompoundTag, HolderLookup.Provider, ICost> factory = RegCostType.costs.get(id);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown cost type: " + id);
        }
        return factory.apply(nbt, registries);
    }
}
