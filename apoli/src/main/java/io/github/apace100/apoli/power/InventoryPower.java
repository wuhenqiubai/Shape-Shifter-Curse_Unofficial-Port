package io.github.apace100.apoli.power;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.util.UpgradeUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.function.Predicate;
import java.util.stream.Stream;

public class InventoryPower extends Power implements Active, Container {

    private final NonNullList<ItemStack> container;
    private final MutableComponent containerTitle;
    private final MenuConstructor containerScreen;
    private final Predicate<ItemStack> dropOnDeathFilter;

    private final boolean shouldDropOnDeath;
    private final boolean recoverable;
    private final int containerSize;

    public InventoryPower(PowerType<?> type, LivingEntity entity, String containerTitle, ContainerType containerType, boolean shouldDropOnDeath, Predicate<ItemStack> dropOnDeathFilter, boolean recoverable) {
        super(type, entity);
        switch (containerType) {
            case DOUBLE_CHEST:
                containerSize = 54;
                this.containerScreen = (i, playerInventory, playerEntity) -> new ChestMenu(MenuType.GENERIC_9x6, i,
                    playerInventory, this, 6);
                break;
            case CHEST:
                containerSize = 27;
                this.containerScreen = (i, playerInventory, playerEntity) -> new ChestMenu(MenuType.GENERIC_9x3, i,
                    playerInventory, this, 3);
                break;
            case HOPPER:
                containerSize = 5;
                this.containerScreen = (i, playerInventory, playerEntity) -> new HopperMenu(i, playerInventory, this);
                break;
            case DROPPER, DISPENSER:
            default:
                containerSize = 9;
                this.containerScreen = (i, playerInventory, playerEntity) -> new DispenserMenu(i, playerInventory, this);
                break;
        }
        this.container = NonNullList.withSize(containerSize, ItemStack.EMPTY);
        this.containerTitle = Component.translatable(containerTitle);
        this.shouldDropOnDeath = shouldDropOnDeath;
        this.dropOnDeathFilter = dropOnDeathFilter;
        this.recoverable = recoverable;
    }

    public enum ContainerType {
        CHEST,
        DOUBLE_CHEST,
        DROPPER,
        DISPENSER,
        HOPPER
    }

    @Override
    public void onLost() {
        if (recoverable) dropItemsOnLost();
    }

    @Override
    public void onUse() {
        if(!isActive()) {
            return;
        }
        if(!entity.level().isClientSide() && entity instanceof Player playerEntity) {
            playerEntity.openMenu(new SimpleMenuProvider(containerScreen, containerTitle));
        }
    }

    @Override
    public void toValue(ValueOutput output) {
        ContainerHelper.saveAllItems(output, container, true);
    }

    private static final Codec<ItemStackWithSlot> DATA_FIXED_ITEM_STACK_WITH_SLOT_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ExtraCodecs.UNSIGNED_BYTE.fieldOf("Slot")
                .orElse(0)
                .forGetter(ItemStackWithSlot::slot),
            Codec.mapEither(ItemStack.MAP_CODEC,
                MapCodec.of(ItemStack.MAP_CODEC, new MapDecoder<>() {
                    @Override
                    public <T> Stream<T> keys(DynamicOps<T> ops) {
                        return ItemStack.MAP_CODEC.keys(ops);
                    }

                    @Override
                    public <T> DataResult<ItemStack> decode(DynamicOps<T> ops, MapLike<T> input) {
                        try {
                            Dynamic<T> dynamic = new Dynamic<>(ops);
                            input.entries().forEach(pair -> {
                                dynamic.set(ops.getStringValue(pair.getFirst()).getOrThrow(), new Dynamic<>(ops, pair.getSecond()));
                            });

                            Dynamic<T> upgraded = UpgradeUtils.upgradeStack(dynamic);
                            return upgraded.decode(ItemStack.MAP_CODEC.decoder()).map(Pair::getFirst);
                        } catch (Exception e) {
                            return DataResult.error(() -> "Failed to decode with data fixer: " + e.getMessage());
                        }
                    }

                    @Override
                    public <T> KeyCompressor<T> compressor(DynamicOps<T> ops) {
                        return ItemStack.MAP_CODEC.compressor(ops);
                    }
                })
            )
                .xmap(Either::unwrap, Either::left)
                .forGetter(ItemStackWithSlot::stack)
        )
            .apply(instance, ItemStackWithSlot::new)
    );

    @Override
    public void fromValue(ValueInput input) {
        for (ItemStackWithSlot itemStackWithSlot : input.listOrEmpty("Items", DATA_FIXED_ITEM_STACK_WITH_SLOT_CODEC)) {
            if (itemStackWithSlot.isValidInContainer(container.size())) {
                container.set(itemStackWithSlot.slot(), itemStackWithSlot.stack());
            }
        }
    }

    @Override
    public int getContainerSize() {
        return containerSize;
    }

    @Override
    public boolean isEmpty() {
        return container.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return container.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return container.get(slot).split(amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = container.get(slot);
        setItem(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        container.set(slot, stack);
    }

    @Override
    public void setChanged() {}

    @Override
    public boolean stillValid(Player player) {
        return player == this.entity;
    }

    @Override
    public void clearContent() {
        for(int i = 0; i < containerSize; i++) {
            setItem(i, ItemStack.EMPTY);
        }
    }

    public NonNullList<ItemStack> getContainer() {
        return container;
    }

    public MutableComponent getContainerTitle() {
        return containerTitle;
    }

    public MenuConstructor getContainerScreen() {
        return containerScreen;
    }

    public boolean shouldDropOnDeath() {
        return shouldDropOnDeath;
    }

    public boolean shouldDropOnDeath(ItemStack stack) {
        return shouldDropOnDeath && dropOnDeathFilter.test(stack);
    }

    public void dropItemsOnDeath() {
        Player playerEntity = (Player) entity;
        for (int i = 0; i < containerSize; ++i) {
            ItemStack currentItemStack = getItem(i);
            if (shouldDropOnDeath(currentItemStack)) {
                if (!currentItemStack.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(playerEntity.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.VANISHING_CURSE), currentItemStack) > 0) removeItemNoUpdate(i);
                else {
                    playerEntity.drop(currentItemStack, true, false);
                    setItem(i, ItemStack.EMPTY);
                }
            }
        }
    }

    public void dropItemsOnLost() {
        Player playerEntity = (Player) entity;
        for (int i = 0; i < containerSize; ++i) {
            ItemStack currentItemStack = getItem(i);
            playerEntity.getInventory().placeItemBackInInventory(currentItemStack);
        }
    }

    private Key key;

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public void setKey(Key key) {
        this.key = key;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("inventory"),
            new SerializableData()
                .add("title", SerializableDataTypes.STRING, "container.inventory")
                .add("container_type", SerializableDataType.enumValue(ContainerType.class), ContainerType.DROPPER)
                .add("drop_on_death", SerializableDataTypes.BOOLEAN, false)
                .add("drop_on_death_filter", ApoliDataTypes.ITEM_CONDITION, null)
                .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Active.Key())
                .add("recoverable", SerializableDataTypes.BOOLEAN, true),
            data ->
                (powerType, livingEntity) -> {
                    InventoryPower inventoryPower = new InventoryPower(
                        powerType,
                        livingEntity,
                        data.getString("title"),
                        data.get("container_type"),
                        data.getBoolean("drop_on_death"),
                        data.isPresent("drop_on_death_filter") ? data.get("drop_on_death_filter") : itemStack -> true,
                        data.getBoolean("recoverable")
                    );
                    inventoryPower.setKey(data.get("key"));
                    return inventoryPower;
                })
            .allowCondition();
    }
}
