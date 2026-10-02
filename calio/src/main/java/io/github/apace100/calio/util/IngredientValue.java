package io.github.apace100.calio.util;

import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public interface IngredientValue {
    HolderSet<Item> getItems(HolderLookup.Provider provider);
    JsonObject serialize();

    class TagValue implements IngredientValue {
        private final TagKey<Item> tag;

        public TagValue(TagKey<Item> tag) {
            this.tag = tag;
        }

        @Override
        public HolderSet<Item> getItems(HolderLookup.Provider provider) {
            return provider.lookupOrThrow(Registries.ITEM).getOrThrow(tag);
        }

        @Override
        public JsonObject serialize() {
            var json = new JsonObject();
            json.addProperty("tag", this.tag.location().toString());

            return json;
        }
    }

    class ItemValue implements IngredientValue {
        private final Item item;

        public ItemValue(Item stack) {
            this.item = stack;
        }

        @Override
        public HolderSet<Item> getItems(HolderLookup.Provider provider) {
            return HolderSet.direct(BuiltInRegistries.ITEM.wrapAsHolder(item));
        }

        @Override
        public JsonObject serialize() {
            var json = new JsonObject();
            json.addProperty("item", BuiltInRegistries.ITEM.getKey(this.item).toString());
            return json;
        }
    }
}
