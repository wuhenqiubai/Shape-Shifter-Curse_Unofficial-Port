package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.render.tech.ItemStorePowerRender;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ItemStorePower extends Power implements ItemStorePowerRender.itemStorePowerRenderInterface {
    public ItemStack storedItem = ItemStack.EMPTY;
    public final @Nullable Identifier powerID;
    public int bobbingAnimationTime = 0;
    public final int Slot;
    public final int VanillaSlotStart = 2800;

    public ItemStorePower(PowerType<?> type, LivingEntity entity, @Nullable Identifier powerID, int Slot) {
        super(type, entity);
        this.powerID = powerID;
        this.Slot = Slot;
        this.setTicking();
    }

    public void clientTick() {
        if (this.bobbingAnimationTime > 0) {
            this.bobbingAnimationTime -= 1;
        }
    }

    @Override
    public void tick() {
        if (this.bobbingAnimationTime > 0) {
            this.bobbingAnimationTime -= 1;
        }
        this.storedItem.inventoryTick(this.entity.level(), this.entity, null);
    }

    public void SetItem(ItemStack stack) {
        if (this.entity.level().isClientSide()) {
            return;
        }
        this.storedItem = stack.copy();
        this.bobbingAnimationTime = 5;
        PowerHolderComponent.syncPower(this.entity, this.getType());
    }

    public void GainItem(ItemStack stack) {
        if (this.entity.level().isClientSide()) {
            return;
        }
        if (!this.storedItem.isEmpty()) {
            this.DropItem();
        }
        this.SetItem(stack);
    }

    public void DropItem() {
        if (this.entity.level().isClientSide()) {
            return;
        }
        if (!storedItem.isEmpty()) {
            this.entity.level().addFreshEntity(
                    new ItemEntity(
                            this.entity.level(),
                            this.entity.getX(),
                            this.entity.getY(),
                            this.entity.getZ(),
                            this.storedItem
                    )
            );
            this.SetItem(ItemStack.EMPTY);
        }
    }

    public void SwapItem(EquipmentSlot slot) {
        if (this.entity.level().isClientSide()) {
            return;
        }
        ItemStack item = this.entity.getItemBySlot(slot);
        ItemStack stored = this.storedItem;
        this.SetItem(item);
        this.entity.setItemSlot(slot, stored);
    }

    public void InvokeItemAction(ActionFactory<Tuple<Level, ItemStack>>.Instance action) {
        if (this.entity.level().isClientSide()) {
            return;
        }
        if (action != null) {
            action.accept(new Tuple<>(this.entity.level(), this.storedItem));
        }
        PowerHolderComponent.syncPower(this.entity, this.getType());
    }

    @Override
    public void onLost() {
        super.onLost();
        this.DropItem();
    }


    @Override
    public Tag toTag() {
        CompoundTag tag = new CompoundTag();
        HolderLookup.Provider registries = this.entity.registryAccess();
        ItemStack.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), this.storedItem)
                .result()
                .ifPresent(nbtElement -> tag.put("stored_item", nbtElement));
        tag.putInt("bobbing_animation_time", this.bobbingAnimationTime);
        return tag;
    }

    @Override
    public void fromTag(Tag tag) {
        if (tag instanceof CompoundTag compound) {
            HolderLookup.Provider registries = this.entity.registryAccess();
            Tag itemNbt = compound.get("stored_item");
            if (itemNbt != null) {
                ItemStack.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), itemNbt)
                        .result()
                        .ifPresent(stack -> this.storedItem = stack);
            }
            this.bobbingAnimationTime = compound.getInt("bobbing_animation_time").orElse(0);
        }
    }

    public static PowerFactory<?> createFactory() {
        return new PowerFactory<>(
                ShapeShifterCurseFabric.identifier("item_store"),
                new SerializableData()
                        .add("id", SerializableDataTypes.IDENTIFIER, null)
                        .add("slot", SerializableDataTypes.INT, 0),
                data -> (type, entity) -> new ItemStorePower(type, entity, data.get("id"), data.getInt("slot"))
        ).allowCondition();
    }

    public static @Nullable ItemStorePower findPower(Entity entity, @Nullable Identifier powerID) {
        if (powerID == null) return null;
        if (entity instanceof LivingEntity livingEntity) {
            return PowerHolderComponent.getPowers(livingEntity, ItemStorePower.class).stream()
                    .filter(power -> power.powerID != null && power.powerID.equals(powerID))
                    .findFirst().orElse(null);
        }
        return null;
    }

    public static void registerCondition(Consumer<ConditionFactory<Entity>> registerFunc) {
        registerFunc.accept(new ConditionFactory<>(
                ShapeShifterCurseFabric.identifier("check_stored_item"),
                new SerializableData()
                        .add("id", SerializableDataTypes.IDENTIFIER, null)
                        .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                        .add("default", SerializableDataTypes.BOOLEAN, false),
                (data, entity) -> {
                    ItemStorePower itemStorePower = findPower(entity, data.get("id"));
                    if (itemStorePower == null) return data.getBoolean("default");
                    ConditionFactory<ItemStack>.Instance condition = data.get("item_condition");
                    if (condition == null) return data.getBoolean("default");
                    return condition.test(itemStorePower.storedItem);
                }
        ));
    }

    public static void registerAction(Consumer<ActionFactory<Entity>> ActionRegister, Consumer<ActionFactory<Tuple<Entity, Entity>>> BIActionRegister) {
        ActionRegister.accept(new ActionFactory<>(
                ShapeShifterCurseFabric.identifier("gain_store_power_item"),
                new SerializableData()
                        .add("id", SerializableDataTypes.IDENTIFIER, null)
                        .add("item", SerializableDataTypes.ITEM_STACK, null)
                        .add("if_no_power_drop", SerializableDataTypes.BOOLEAN, true),
                (data, entity) -> {
                    ItemStorePower itemStorePower = findPower(entity, data.get("id"));
                    if (itemStorePower != null) {
                        itemStorePower.GainItem(data.get("item"));
                    }
                    else if (data.getBoolean("if_no_power_drop")) {
                        if (entity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                            entity.spawnAtLocation(serverLevel, (net.minecraft.world.level.ItemLike) data.get("item"));
                        }
                    }
                }
        ));

        ActionRegister.accept(new ActionFactory<>(
                ShapeShifterCurseFabric.identifier("drop_store_power_item"),
                new SerializableData()
                        .add("id", SerializableDataTypes.IDENTIFIER, null)
                        .add("remove_item", SerializableDataTypes.BOOLEAN, false),
                (data, entity) -> {
                    ItemStorePower itemStorePower = findPower(entity, data.get("id"));
                    boolean removeItem = data.getBoolean("remove_item");
                    if (itemStorePower != null) {
                        if (removeItem) {
                            itemStorePower.storedItem = ItemStack.EMPTY;
                        } else {
                            itemStorePower.DropItem();
                        }
                    }
                }
        ));

        ActionRegister.accept(new ActionFactory<>(
                ShapeShifterCurseFabric.identifier("swap_store_power_item"),
                new SerializableData()
                        .add("id", SerializableDataTypes.IDENTIFIER, null)
                        .add("slot", SerializableDataTypes.EQUIPMENT_SLOT, EquipmentSlot.MAINHAND),
                (data, entity) -> {
                    ItemStorePower itemStorePower = findPower(entity, data.get("id"));
                    if (itemStorePower != null) {
                        itemStorePower.SwapItem(data.get("slot"));
                    }
                }
        ));

        ActionRegister.accept(new ActionFactory<>(
                ShapeShifterCurseFabric.identifier("invoke_store_power_item"),
                new SerializableData()
                        .add("id", SerializableDataTypes.IDENTIFIER, null)
                        .add("action", ApoliDataTypes.ITEM_ACTION, null),
                (data, entity) -> {
                    ItemStorePower itemStorePower = findPower(entity, data.get("id"));
                    if (itemStorePower != null) {
                        itemStorePower.InvokeItemAction(data.get("action"));
                    }
                }
        ));
    }

    @Override
    public int getSlot() {
        return this.Slot;
    }

    @Override
    public ItemStack getStack() {
        return this.storedItem;
    }

    @Override
    public float getBobbingAnimationTime() {
        return this.bobbingAnimationTime;
    }
}