package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.ClassUtil;
import io.github.apace100.calio.data.ClassDataRegistry;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class PreventFeatureRenderPower extends Power {

    private final List<String> classStrings = new LinkedList<>();

    public PreventFeatureRenderPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }

    public void addClass(String cls) {
        classStrings.add(cls);
    }

    @Environment(EnvType.CLIENT)
    public boolean doesApply(Class<? extends RenderLayer<?, ?>> cls) {
        Optional<ClassDataRegistry> optionalCdr = ClassDataRegistry.get(ClassUtil.castClass(RenderLayer.class));
        if(optionalCdr.isPresent()) {
            ClassDataRegistry<? extends RenderLayer<?, ?>> cdr = optionalCdr.get();
            return classStrings.stream()
                .map(cdr::mapStringToClass)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .anyMatch(c -> c.isAssignableFrom(cls));
        }
        return false;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("prevent_feature_render"),
            new SerializableData()
                .add("feature", SerializableDataTypes.STRING, null)
                .add("features", SerializableDataTypes.STRINGS, null),
            data ->
                (type, entity) -> {
                    PreventFeatureRenderPower power = new PreventFeatureRenderPower(type, entity);
                    data.ifPresent("feature", power::addClass);
                    data.<List<String>>ifPresent("features",
                        list -> list.forEach(power::addClass));
                    return power;
                })
            .allowCondition();
    }
}
