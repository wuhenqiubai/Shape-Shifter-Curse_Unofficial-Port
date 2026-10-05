package net.onixary.shapeShifterCurseFabric.util.util.cost;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

public class ItemCost implements ICost {
    public static final Identifier id = ShapeShifterCurseFabric.identifier("item");
    private ICostType<?> type;
    private ItemStack exampleStack;
    private int amount;

    public ItemCost() {
        this(RegCostType.NO_COST, ItemStack.EMPTY, 0);
    }

    public ItemCost(CompoundTag nbt, HolderLookup.Provider registries) {
        this();
        readFromNBT(nbt, registries);
    }

    public ItemCost(ICostType<?> type, ItemStack exampleStack, int amount) {
        this.type = type;
        exampleStack.setCount(1);
        this.exampleStack = exampleStack;
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

    public ItemStack getExampleStack() {
        return exampleStack;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public void writeToNBT(CompoundTag nbt, HolderLookup.Provider registries) {
        nbt.putString("type", type.getID().toString());
        // 1.21.1：ItemStack.writeNbt 已移除，改用 Codec + registry 上下文（与 ItemStorePower 同一写法）。
        ItemStack.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), exampleStack)
                .result()
                .ifPresent(tag -> nbt.put("exampleStack", tag));
        nbt.putInt("amount", amount);
    }

    @Override
    public void readFromNBT(CompoundTag nbt, HolderLookup.Provider registries) {
        // 1.21.11：CompoundTag 的 getString/getInt 改返回 Optional，用 getStringOr/getIntOr。
        type = RegCostType.getCostType(Identifier.parse(nbt.getStringOr("type", "")));
        // 解析失败时保留构造函数给的 ItemStack.EMPTY，不要静默留下 null。
        Tag stackTag = nbt.get("exampleStack");
        if (stackTag != null) {
            ItemStack.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), stackTag)
                    .result()
                    .ifPresent(stack -> exampleStack = stack);
        }
        amount = nbt.getIntOr("amount", 0);
    }
}
