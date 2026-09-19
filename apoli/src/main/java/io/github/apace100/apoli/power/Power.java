package io.github.apace100.apoli.power;

import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class Power {

    protected LivingEntity entity;
    protected PowerType<?> type;

    private boolean shouldTick = false;
    private boolean shouldTickWhenInactive = false;

    protected List<Predicate<Entity>> conditions;

    public Power(PowerType<?> type, LivingEntity entity) {
        this.type = type;
        this.entity = entity;
        this.conditions = new LinkedList<>();
    }

    public Power addCondition(Predicate<Entity> condition) {
        this.conditions.add(condition);
        return this;
    }

    protected void setTicking() {
        this.setTicking(false);
    }

    protected void setTicking(boolean evenWhenInactive) {
        this.shouldTick = true;
        this.shouldTickWhenInactive = evenWhenInactive;
    }

    public boolean shouldTick() {
        return shouldTick;
    }

    public boolean shouldTickWhenInactive() {
        return shouldTickWhenInactive;
    }

    public void tick() {

    }

    public void onGained() {

    }

    public void onLost() {

    }

    public void onAdded() {

    }

    public void onRemoved() {

    }

    public void onRespawn() {

    }

    public boolean isActive() {
        return conditions.stream().allMatch(condition -> condition.test(entity));
    }

    public Tag toTag(HolderLookup.Provider provider) {
        return new CompoundTag();
    }

    public void fromTag(Tag tag, HolderLookup.Provider provider) {

    }

    public PowerType<?> getType() {
        return type;
    }

    public static PowerFactory createSimpleFactory(BiFunction<PowerType, LivingEntity, Power> powerConstructor, ResourceLocation identifier) {
        return new PowerFactory<>(identifier,
            new SerializableData(), data -> powerConstructor::apply).allowCondition();
    }
}
