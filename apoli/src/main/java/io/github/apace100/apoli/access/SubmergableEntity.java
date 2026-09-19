package io.github.apace100.apoli.access;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public interface SubmergableEntity {

    boolean isSubmergedInLoosely(TagKey<Fluid> fluidTag);

    double getFluidHeightLoosely(TagKey<Fluid> fluidTag);
}
