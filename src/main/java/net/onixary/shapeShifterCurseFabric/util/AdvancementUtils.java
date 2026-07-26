package net.onixary.shapeShifterCurseFabric.util;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.onixary.shapeShifterCurseFabric.items.RegCustomItem;

import java.util.*;
import java.util.function.Consumer;

public class AdvancementUtils {
    // === Upstream-compatible API ===
    public static final HashMap<ResourceLocation, List<Consumer<Advancement>>> advancementAddedCallbacks = new HashMap<>();

    public static void registerAdvancementAddedCallback(ResourceLocation id, Consumer<Advancement> callback) {
        if (advancementAddedCallbacks.containsKey(id)) {
            advancementAddedCallbacks.get(id).add(callback);
        } else {
            advancementAddedCallbacks.put(id, new ArrayList<>(List.of(callback)));
        }
    }

    /**
     * Called from ServerAdvancementMixin for each loaded advancement.
     * Runs registered callbacks then applies pending item patches.
     * Returns the (possibly patched) AdvancementEntry — adaptation for 1.21.1 immutability.
     */
    public static AdvancementHolder onAdvancementAdded(AdvancementHolder entry) {
        Advancement advancement = entry.value();
        ResourceLocation id = entry.id();

        // Run registered callbacks (for side effects: they register into pendingPatches)
        List<Consumer<Advancement>> callbacks = advancementAddedCallbacks.get(id);
        if (callbacks != null) {
            callbacks.forEach(cb -> cb.accept(advancement));
        }

        // Apply pending patches and return result
        return applyPendingPatches(entry);
    }

    // === 1.21.1 patch infrastructure (immutable Advancement, RegistryEntryList) ===
    private static final Map<ResourceLocation, List<ItemPatch>> pendingPatches = new HashMap<>();

    private static void addItemPatch(ResourceLocation id, Item original, Item custom) {
        pendingPatches.computeIfAbsent(id, k -> new ArrayList<>()).add(new ItemPatch(original, custom));
    }

    private static AdvancementHolder applyPendingPatches(AdvancementHolder entry) {
        List<ItemPatch> patches = pendingPatches.get(entry.id());
        if (patches == null || patches.isEmpty()) return entry;

        Advancement advancement = entry.value();
        Map<String, Criterion<?>> criteria = new HashMap<>(advancement.criteria());
        boolean changed = false;

        for (Map.Entry<String, Criterion<?>> criterionEntry : criteria.entrySet()) {
            Criterion<?> criterion = criterionEntry.getValue();
            if (criterion.triggerInstance() instanceof InventoryChangeTrigger.TriggerInstance(
                    Optional<net.minecraft.advancements.critereon.ContextAwarePredicate> player,
                    InventoryChangeTrigger.TriggerInstance.Slots slots,
                    List<ItemPredicate> items
            )) {
                List<ItemPredicate> newPredicates = patchPredicateList(items, patches);
                if (newPredicates != null) {
                    criterionEntry.setValue(CriteriaTriggers.INVENTORY_CHANGED.createCriterion(
                            new InventoryChangeTrigger.TriggerInstance(player, slots, newPredicates)
                    ));
                    changed = true;
                }
            }
        }

        if (changed) {
            Advancement patched = new Advancement(
                    advancement.parent(), advancement.display(), advancement.rewards(),
                    criteria, advancement.requirements(), advancement.sendsTelemetryEvent()
            );
            return new AdvancementHolder(entry.id(), patched);
        }
        return entry;
    }

    private static List<ItemPredicate> patchPredicateList(List<ItemPredicate> predicates, List<ItemPatch> patches) {
        List<ItemPredicate> result = null;
        for (int i = 0; i < predicates.size(); i++) {
            ItemPredicate predicate = predicates.get(i);
            ItemPredicate patched = addPatchedItems(predicate, patches);
            if (patched != predicate) {
                if (result == null) result = new ArrayList<>(predicates);
                result.set(i, patched);
            }
        }
        return result;
    }

    @SuppressWarnings("deprecation")
    private static ItemPredicate addPatchedItems(ItemPredicate predicate, List<ItemPatch> patches) {
        if (predicate.items().isEmpty()) return predicate;

        HolderSet<Item> items = predicate.items().get();
        List<Holder<Item>> entries = new ArrayList<>();
        boolean modified = false;
        items.forEach(entries::add);

        for (ItemPatch patch : patches) {
            if (items.contains(patch.originalItem().builtInRegistryHolder())) {
                entries.add(patch.customItem().builtInRegistryHolder());
                modified = true;
            }
        }

        if (!modified) return predicate;

        return new ItemPredicate(
                Optional.of(HolderSet.direct(entries)),
                predicate.count(),
                predicate.components(),
                predicate.subPredicates()
        );
    }

    // === Patches — follows upstream structure adapted for 1.21.1 ===
    static {
        addItemPatch(ResourceLocation.fromNamespaceAndPath("minecraft", "nether/netherite_armor"), Items.NETHERITE_HELMET, RegCustomItem.NETHERITE_MORPHSCALE_HEADRING);
        addItemPatch(ResourceLocation.fromNamespaceAndPath("minecraft", "nether/netherite_armor"), Items.NETHERITE_CHESTPLATE, RegCustomItem.NETHERITE_MORPHSCALE_VEST);
        addItemPatch(ResourceLocation.fromNamespaceAndPath("minecraft", "nether/netherite_armor"), Items.NETHERITE_LEGGINGS, RegCustomItem.NETHERITE_MORPHSCALE_CUISH);
        addItemPatch(ResourceLocation.fromNamespaceAndPath("minecraft", "nether/netherite_armor"), Items.NETHERITE_BOOTS, RegCustomItem.NETHERITE_MORPHSCALE_ANKLET);
        addItemPatch(ResourceLocation.fromNamespaceAndPath("minecraft", "story/shiny_gear"), Items.DIAMOND_HELMET, RegCustomItem.MORPHSCALE_HEADRING);
        addItemPatch(ResourceLocation.fromNamespaceAndPath("minecraft", "story/shiny_gear"), Items.DIAMOND_CHESTPLATE, RegCustomItem.MORPHSCALE_VEST);
        addItemPatch(ResourceLocation.fromNamespaceAndPath("minecraft", "story/shiny_gear"), Items.DIAMOND_LEGGINGS, RegCustomItem.MORPHSCALE_CUISH);
        addItemPatch(ResourceLocation.fromNamespaceAndPath("minecraft", "story/shiny_gear"), Items.DIAMOND_BOOTS, RegCustomItem.MORPHSCALE_ANKLET);
    }

    private record ItemPatch(Item originalItem, Item customItem) {
    }
}