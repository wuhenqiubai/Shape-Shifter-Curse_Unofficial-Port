package net.onixary.shapeShifterCurseFabric.util;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.Identifier;
import net.onixary.shapeShifterCurseFabric.items.RegCustomItem;

import java.util.*;
import java.util.function.Consumer;

public class AdvancementUtils {
    // === Upstream-compatible API ===
    public static final HashMap<Identifier, List<Consumer<Advancement>>> advancementAddedCallbacks = new HashMap<>();

    public static void registerAdvancementAddedCallback(Identifier id, Consumer<Advancement> callback) {
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
    public static AdvancementEntry onAdvancementAdded(AdvancementEntry entry) {
        Advancement advancement = entry.value();
        Identifier id = entry.id();

        // Run registered callbacks (for side effects: they register into pendingPatches)
        List<Consumer<Advancement>> callbacks = advancementAddedCallbacks.get(id);
        if (callbacks != null) {
            callbacks.forEach(cb -> cb.accept(advancement));
        }

        // Apply pending patches and return result
        return applyPendingPatches(entry);
    }

    // === 1.21.1 patch infrastructure (immutable Advancement, RegistryEntryList) ===
    private static final Map<Identifier, List<ItemPatch>> pendingPatches = new HashMap<>();

    private static void addItemPatch(Identifier id, Item original, Item custom) {
        pendingPatches.computeIfAbsent(id, k -> new ArrayList<>()).add(new ItemPatch(original, custom));
    }

    private static AdvancementEntry applyPendingPatches(AdvancementEntry entry) {
        List<ItemPatch> patches = pendingPatches.get(entry.id());
        if (patches == null || patches.isEmpty()) return entry;

        Advancement advancement = entry.value();
        Map<String, AdvancementCriterion<?>> criteria = new HashMap<>(advancement.criteria());
        boolean changed = false;

        for (Map.Entry<String, AdvancementCriterion<?>> criterionEntry : criteria.entrySet()) {
            AdvancementCriterion<?> criterion = criterionEntry.getValue();
            if (criterion.conditions() instanceof InventoryChangedCriterion.Conditions(
                    Optional<net.minecraft.predicate.entity.LootContextPredicate> player,
                    InventoryChangedCriterion.Conditions.Slots slots,
                    List<ItemPredicate> items
            )) {
                List<ItemPredicate> newPredicates = patchPredicateList(items, patches);
                if (newPredicates != null) {
                    criterionEntry.setValue(Criteria.INVENTORY_CHANGED.create(
                            new InventoryChangedCriterion.Conditions(player, slots, newPredicates)
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
            return new AdvancementEntry(entry.id(), patched);
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

        RegistryEntryList<Item> items = predicate.items().get();
        List<RegistryEntry<Item>> entries = new ArrayList<>();
        boolean modified = false;
        items.forEach(entries::add);

        for (ItemPatch patch : patches) {
            if (items.contains(patch.originalItem().getRegistryEntry())) {
                entries.add(patch.customItem().getRegistryEntry());
                modified = true;
            }
        }

        if (!modified) return predicate;

        return new ItemPredicate(
                Optional.of(RegistryEntryList.of(entries)),
                predicate.count(),
                predicate.components(),
                predicate.subPredicates()
        );
    }

    // === Patches — follows upstream structure adapted for 1.21.1 ===
    static {
        addItemPatch(Identifier.of("minecraft", "nether/netherite_armor"), Items.NETHERITE_HELMET, RegCustomItem.NETHERITE_MORPHSCALE_HEADRING);
        addItemPatch(Identifier.of("minecraft", "nether/netherite_armor"), Items.NETHERITE_CHESTPLATE, RegCustomItem.NETHERITE_MORPHSCALE_VEST);
        addItemPatch(Identifier.of("minecraft", "nether/netherite_armor"), Items.NETHERITE_LEGGINGS, RegCustomItem.NETHERITE_MORPHSCALE_CUISH);
        addItemPatch(Identifier.of("minecraft", "nether/netherite_armor"), Items.NETHERITE_BOOTS, RegCustomItem.NETHERITE_MORPHSCALE_ANKLET);
        addItemPatch(Identifier.of("minecraft", "story/shiny_gear"), Items.DIAMOND_HELMET, RegCustomItem.MORPHSCALE_HEADRING);
        addItemPatch(Identifier.of("minecraft", "story/shiny_gear"), Items.DIAMOND_CHESTPLATE, RegCustomItem.MORPHSCALE_VEST);
        addItemPatch(Identifier.of("minecraft", "story/shiny_gear"), Items.DIAMOND_LEGGINGS, RegCustomItem.MORPHSCALE_CUISH);
        addItemPatch(Identifier.of("minecraft", "story/shiny_gear"), Items.DIAMOND_BOOTS, RegCustomItem.MORPHSCALE_ANKLET);
    }

    private record ItemPatch(Item originalItem, Item customItem) {
    }
}