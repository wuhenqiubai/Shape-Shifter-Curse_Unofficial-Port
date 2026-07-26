package net.onixary.shapeShifterCurseFabric.player_form;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NormalGroup implements IFormGroup {
    public final ResourceLocation id;
    public Map<Integer, List<Tuple<Integer, IForm>>> groupData = new HashMap<>();

    public NormalGroup(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public @NotNull ResourceLocation getGroupID() {
        return this.id;
    }

    @Override
    public @NotNull Map<Integer, List<Tuple<Integer, IForm>>> getGroupData() {
        return groupData;
    }
}