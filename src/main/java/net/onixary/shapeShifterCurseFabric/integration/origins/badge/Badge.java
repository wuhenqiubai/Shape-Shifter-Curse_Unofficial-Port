package net.onixary.shapeShifterCurseFabric.integration.origins.badge;

import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.registry.DataObject;
import io.github.apace100.calio.registry.DataObjectFactory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;

public interface Badge extends DataObject<Badge> {

    Identifier spriteId();

    boolean hasTooltip();

    // ⚠ 这里**不能**声明 getTooltipComponents(…, Font)：
    // 其签名含客户端类（Font / ClientTooltipComponent），而本接口在专用服务端也会被加载
    // —— BadgeManager 的静态字段引用了 Badge.class，且 Origins#registerResourceListeners
    // 在服务端同样执行 BadgeManager.init()；JVM 链接期解析方法签名就会去加载那些客户端类，
    // 报 "Cannot load class ... in environment type SERVER" → 服务端启动失败。
    // tooltip 构建已整体移到 @Environment(CLIENT) 的 BadgeTooltipRenderers。

    SerializableData.Instance toData(SerializableData.Instance instance);

    BadgeFactory getBadgeFactory();

    @Override
    default DataObjectFactory<Badge> getFactory() {
        return this.getBadgeFactory();
    }

    default void writeBuf(FriendlyByteBuf buf) {
        DataObjectFactory<Badge> factory = this.getFactory();
        buf.writeIdentifier(this.getBadgeFactory().id());
        factory.getData().write((RegistryFriendlyByteBuf) buf, factory.toData(this));
    }

}
