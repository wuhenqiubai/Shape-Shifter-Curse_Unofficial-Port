package io.github.apace100.apoli.power.factory.action;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.MutableItemStack;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.item.HolderAction;
import io.github.apace100.apoli.power.factory.action.meta.*;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.util.UpgradeUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ItemActions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(AndAction.getFactory(ApoliDataTypes.ITEM_ACTIONS));
        register(ChanceAction.getFactory(ApoliDataTypes.ITEM_ACTION));
        register(IfElseAction.getFactory(ApoliDataTypes.ITEM_ACTION, ApoliDataTypes.ITEM_CONDITION,
            Pair::getSecond));
        register(ChoiceAction.getFactory(ApoliDataTypes.ITEM_ACTION));
        register(IfElseListAction.getFactory(ApoliDataTypes.ITEM_ACTION, ApoliDataTypes.ITEM_CONDITION,
            Pair::getSecond));
        register(DelayAction.getFactory(ApoliDataTypes.ITEM_ACTION));
        register(NothingAction.getFactory());
        register(SideAction.getFactory(ApoliDataTypes.ITEM_ACTION, worldAndStack -> !worldAndStack.getFirst().isClientSide()));

        register(new ActionFactory<>(Apoli.identifier("consume"), new SerializableData()
            .add("amount", SerializableDataTypes.INT, 1),
            (data, worldAndStack) -> {
                worldAndStack.getSecond().shrink(data.getInt("amount"));
            }));
        register(new ActionFactory<>(Apoli.identifier("modify"), new SerializableData()
            .add("modifier", SerializableDataTypes.IDENTIFIER),
            (data, worldAndStack) -> {
                MinecraftServer server = worldAndStack.getFirst().getServer();
                if(server != null) {
                    Identifier id = data.getId("modifier");
                    LootItemFunction lootFunction = server.registryAccess().lookupOrThrow(Registries.ITEM_MODIFIER).getValue(id);
                    if (lootFunction == null) {
                        Apoli.LOGGER.info("Unknown item modifier used in `modify` action: " + id);
                        return;
                    }
                    ServerLevel serverWorld = server.overworld();
                    ItemStack stack = worldAndStack.getSecond();
                    LootParams lootContextParameterSet = new LootParams.Builder(serverWorld).withParameter(LootContextParams.ORIGIN, new Vec3(0, 0,0)).create(LootContextParamSets.COMMAND);
                    LootContext lootContext = new LootContext.Builder(lootContextParameterSet).create(Optional.empty());
                    ItemStack newStack = lootFunction.apply(stack, lootContext);
                    ((MutableItemStack)stack).setFrom(newStack);
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("damage"), new SerializableData()
            .add("amount", SerializableDataTypes.INT, 1)
            .add("ignore_unbreaking", SerializableDataTypes.BOOLEAN, false),
            (data, worldAndStack) -> {
                if (worldAndStack.getSecond().isDamageableItem()) {
                    int amount = data.getInt("amount");
                    int i;
                    if (amount > 0 && !data.getBoolean("ignore_unbreaking")) {
                        i = EnchantmentHelper.getItemEnchantmentLevel(worldAndStack.getFirst().getServer().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING), worldAndStack.getSecond());
                        int j = EnchantmentHelper.processDurabilityChange((ServerLevel) worldAndStack.getFirst(), worldAndStack.getSecond(), i);

                        amount -= j;
                        if (amount <= 0) {
                            return;
                        }
                    }

                    i = worldAndStack.getSecond().getDamageValue() + amount;
                    worldAndStack.getSecond().setDamageValue(i);
                    if(i >= worldAndStack.getSecond().getMaxDamage()) {
                        worldAndStack.getSecond().shrink(1);
                        worldAndStack.getSecond().setDamageValue(0);
                    }
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("merge_nbt"), new SerializableData()
            .add("nbt", SerializableDataTypes.STRING),
            (data, worldAndStack) -> {
                String nbtString = data.get("nbt");
                try {
                    CompoundTag oldTag = TagParser.create(NbtOps.INSTANCE).parseFully(new StringReader(nbtString)).asCompound().get();
                    CompoundTag recreatedTag = new CompoundTag();
                    recreatedTag.putString("id", BuiltInRegistries.ITEM.getKey(worldAndStack.getSecond().getItem()).toString());
                    recreatedTag.putInt("Count", worldAndStack.getSecond().getCount());
                    recreatedTag.put("tag", oldTag);

                    var convertedStackNbt = UpgradeUtils.upgradeStack(recreatedTag);
                    var convertedStack = ItemStack.CODEC.decode(NbtOps.INSTANCE, convertedStackNbt).getOrThrow().getFirst();
                    worldAndStack.getSecond().applyComponents(convertedStack.getComponents());
                } catch (CommandSyntaxException e) {
                    Apoli.LOGGER.error("Failed `merge_nbt` item action due to malformed nbt string: \"" + nbtString + "\"");
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("remove_enchantment"), new SerializableData()
            .add("enchantment", SerializableDataTypes.ENCHANTMENT, null)
            .add("enchantments", SerializableDataType.list(SerializableDataTypes.ENCHANTMENT), null)
            .add("levels", SerializableDataTypes.INT, null)
            .add("reset_repair_cost", SerializableDataTypes.BOOLEAN, false),
            (data, worldAndStack) -> {
                ItemStack stack = worldAndStack.getSecond();
                if(stack.isEmpty()) {
                    return;
                }
                List<ResourceKey<Enchantment>> enchs = new LinkedList<>();
                data.<ResourceKey<Enchantment>>ifPresent("enchantment", enchs::add);
                data.<List<ResourceKey<Enchantment>>>ifPresent("enchantments", enchs::addAll);
                int levels = -1;
                if(data.isPresent("levels")) {
                    levels = data.getInt("levels");
                }
                if (!stack.has(DataComponents.ENCHANTMENTS))
                    return;

                var enchantments = stack.get(DataComponents.ENCHANTMENTS);
                var enchants = enchantments.keySet();
                var modifiableEnchants = new ItemEnchantments.Mutable(enchantments);
                if(enchs.size() > 0) {
                    for(ResourceKey<Enchantment> ench : enchs) {
                        var enchant = worldAndStack.getFirst().getServer().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ench);
                        if(enchants.contains(enchant)) {
                            int newLevel = levels == -1 ? 0 : enchantments.getLevel(enchant) - data.getInt("levels");
                            if(newLevel <= 0) {
                                modifiableEnchants.removeIf(e -> e.equals(enchant));
                            } else {
                                modifiableEnchants.set(enchant, newLevel);
                            }
                        }
                    }
                } else {
                    for(Holder<Enchantment> e : enchants) {
                        int newLevel = levels == -1 ? 0 : enchantments.getLevel(e) - data.getInt("levels");
                        if(newLevel > 0) {
                            modifiableEnchants.set(e, newLevel);
                        }
                    }
                }
                EnchantmentHelper.setEnchantments(stack, modifiableEnchants.toImmutable());
                if(data.getBoolean("reset_repair_cost") && !stack.isEnchanted()) {
                    stack.set(DataComponents.REPAIR_COST, 0);
                }
            }));
        register(HolderAction.getFactory());
    }

    private static void register(ActionFactory<Pair<Level, ItemStack>> actionFactory) {
        Registry.register(ApoliRegistries.ITEM_ACTION, actionFactory.getSerializerId(), actionFactory);
    }
}
