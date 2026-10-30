package net.onixary.shapeShifterCurseFabric.items.armors;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

import java.util.HashMap;
import java.util.Map;

public class MorphscaleArmorRenderState extends HumanoidRenderState implements GeoRenderState {
    private final Map<DataTicket<?>, Object> dataMap = new HashMap<>();

    @Override
    public Map<DataTicket<?>, Object> getDataMap() {
        return this.dataMap;
    }
}
