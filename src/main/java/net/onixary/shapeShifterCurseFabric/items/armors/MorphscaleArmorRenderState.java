package net.onixary.shapeShifterCurseFabric.items.armors;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

import java.util.HashMap;
import java.util.Map;

// GeckoLib5 通过 mixin 给原版 EntityRenderState 注入 GeoRenderState；
// 编译期需要显式声明一个同时实现两者的子类供 GeoArmorRenderer 的类型参数使用。
public class MorphscaleArmorRenderState extends HumanoidRenderState implements GeoRenderState {
    private final Map<DataTicket<?>, Object> dataMap = new HashMap<>();

    @Override
    public Map<DataTicket<?>, Object> getDataMap() {
        return this.dataMap;
    }
}
