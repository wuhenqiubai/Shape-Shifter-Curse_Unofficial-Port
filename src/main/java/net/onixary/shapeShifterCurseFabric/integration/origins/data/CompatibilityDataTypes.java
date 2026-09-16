package net.onixary.shapeShifterCurseFabric.integration.origins.data;

import com.google.gson.JsonPrimitive;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.util.LazyItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class CompatibilityDataTypes {

    // Calio 1.11.4: SerializableDataTypes.ITEM_STACK 的类型由 SerializableDataType<ItemStack>
    // 变成了 SerializableDataType<LazyItemStack>（26.1 起世界加载前不能创建 ItemStack，
    // 故 Calio 引入 LazyItemStack 延迟到用时再解析）。
    // 因此原来的 `ITEM_STACK::send / ::receive` 方法引用类型不匹配、diamond 无法推断。
    //
    // 这里保留对外类型 SerializableDataType<ItemStack>，并**显式在边界处做 ItemStack <-> LazyItemStack 转换**，
    // 而不是改用 SerializableDataType.wrap(...) —— 后者会丢掉下面那段「JSON 字符串 → ITEM」的回退分支。
    public static final SerializableDataType<ItemStack> ITEM_OR_ITEM_STACK = new SerializableDataType<>(
        ItemStack.class,
        (buf, stack) -> SerializableDataTypes.ITEM_STACK.send(buf, LazyItemStack.fromItem(stack)),
        buf -> SerializableDataTypes.ITEM_STACK.receive(buf).getStack(),
        (jsonElement, provider) -> {
            if (!(jsonElement instanceof JsonPrimitive jsonPrimitive) || !jsonPrimitive.isString()) {
                return SerializableDataTypes.ITEM_STACK.read(jsonElement, provider).getStack();
            }
            Item item = SerializableDataTypes.ITEM.read(jsonPrimitive, provider);
            return new ItemStack(item);
        }
    );

    /**
     * {@link #ITEM_OR_ITEM_STACK} 的列表版 —— 对应 Calio 的 {@code SerializableDataTypes.ITEM_STACKS}
     * （其元素同样已是 LazyItemStack），这里把每个元素解包回 ItemStack。
     * <p>
     * ⚠ 必须声明在上面那个单数版**之后** —— 静态初始化器里引用后声明的静态字段是「非法前向引用」，编译不过。
     */
    public static final SerializableDataType<List<ItemStack>> ITEM_OR_ITEM_STACK_LIST =
        SerializableDataType.list(ITEM_OR_ITEM_STACK);
}
