package net.onixary.shapeShifterCurseFabric.util.util.cost;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;

import java.util.function.Supplier;

public class ItemCost implements ICost {
    public static final Identifier id = ShapeShifterCurseFabric.identifier("item");
    private ICostType<?> type;
    private ItemStack exampleStack;
    /** 静态定义时只登记惰性来源，首次取用时才真正构造，见下面的 Supplier 构造。 */
    private Supplier<ItemStack> exampleStackSupplier;
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

    /**
     * ⚠ 26.1 惰性构造：静态定义 perk 时不能立刻 {@code new ItemStack(...)} ——
     * 26.1 的 {@code ItemStack} 构造会解析物品的默认 data components
     * （{@code Holder.Reference.components()}），而 {@code RegPerks} 在 {@code onInitialize}
     * 早期（{@code StudioGenerated.register()}）就被类加载，那时注册表组件尚未绑定，
     * 会抛 {@code NullPointerException: Components not bound yet} 直接炸掉服务端启动。
     * 故这里只登记一个惰性来源，等首次真正取用示例栈时才构造。
     */
    public ItemCost(ICostType<?> type, Supplier<ItemStack> exampleStackSupplier, int amount) {
        this.type = type;
        this.exampleStackSupplier = exampleStackSupplier;
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
        if (exampleStack == null) {
            exampleStack = exampleStackSupplier != null ? exampleStackSupplier.get() : ItemStack.EMPTY;
            exampleStack.setCount(1);
        }
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
        ItemStack.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), getExampleStack())
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
