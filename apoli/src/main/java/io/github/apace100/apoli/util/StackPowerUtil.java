package io.github.apace100.apoli.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.component.StackPowerComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class StackPowerUtil {

    public static void addPower(ItemStack stack, EquipmentSlot slot, ResourceLocation powerId) {
        addPower(stack, slot, powerId, false, false);
    }

    public static void addPower(ItemStack stack, EquipmentSlot slot, ResourceLocation powerId, boolean isHidden, boolean isNegative) {
        StackPower stackPower = new StackPower();
        stackPower.slot = slot;
        stackPower.powerId = powerId;
        stackPower.isHidden = isHidden;
        stackPower.isNegative = isNegative;
        addPower(stack, stackPower);
    }

    public static void addPower(ItemStack stack, StackPower stackPower) {
        var powers = new ArrayList<>(stack.getOrDefault(StackPowerComponent.TYPE, new StackPowerComponent(List.of())).powers());
        powers.add(stackPower);

        stack.set(StackPowerComponent.TYPE, new StackPowerComponent(powers));
    }

    public static void removePower(ItemStack stack, EquipmentSlot slot, ResourceLocation powerId) {
        if (stack.has(StackPowerComponent.TYPE)) {
            var powers = new ArrayList<>(stack.getOrDefault(StackPowerComponent.TYPE, new StackPowerComponent(List.of())).powers());
            powers.removeIf(power -> power.powerId.equals(powerId) && power.slot.equals(slot));

            stack.set(StackPowerComponent.TYPE, new StackPowerComponent(powers));
        }
    }

    public static List<StackPower> getPowers(ItemStack stack, EquipmentSlot slot) {
        return stack.getOrDefault(StackPowerComponent.TYPE, new StackPowerComponent(List.of())).powers().stream().filter(power -> power.slot.equals(slot)).toList();
    }

    public static class StackPower {
        public static final Codec<StackPower> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                EquipmentSlot.CODEC
                    .fieldOf("Slot")
                    .forGetter(power -> power.slot),
                ResourceLocation.CODEC
                    .fieldOf("Power")
                    .forGetter(power -> power.powerId),
                Codec.BOOL
                    .optionalFieldOf("Hidden", false)
                    .forGetter(power -> power.isHidden),
                Codec.BOOL
                    .optionalFieldOf("Negative", false)
                    .forGetter(power -> power.isNegative)
            )
                .apply(instance, StackPower::new)
        );

        public static final StreamCodec<FriendlyByteBuf, StackPower> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(EquipmentSlot.CODEC), power -> power.slot,
            ResourceLocation.STREAM_CODEC, power -> power.powerId,
            ByteBufCodecs.BOOL, power -> power.isHidden,
            ByteBufCodecs.BOOL, power -> power.isNegative,
            StackPower::new
        );

        public EquipmentSlot slot;
        public ResourceLocation powerId;
        public boolean isHidden;
        public boolean isNegative;

        public StackPower() {}
        public StackPower(EquipmentSlot slot, ResourceLocation powerId, boolean isHidden, boolean isNegative) {
            this.slot = slot;
            this.powerId = powerId;
            this.isHidden = isHidden;
            this.isNegative = isNegative;
        }

        public CompoundTag toNbt() {
            CompoundTag nbt = new CompoundTag();
            nbt.putString("Slot", slot.getName());
            nbt.putString("Power", powerId.toString());
            nbt.putBoolean("Hidden", isHidden);
            nbt.putBoolean("Negative", isNegative);
            return nbt;
        }

        public static StackPower fromNbt(CompoundTag nbt) {
            StackPower stackPower = new StackPower();
            stackPower.slot = EquipmentSlot.byName(nbt.getString("Slot"));
            stackPower.powerId = ResourceLocation.parse(nbt.getString("Power"));
            stackPower.isHidden = nbt.contains("Hidden") && nbt.getBoolean("Hidden");
            stackPower.isNegative = nbt.contains("Negative") && nbt.getBoolean("Negative");
            return stackPower;
        }
    }
}
