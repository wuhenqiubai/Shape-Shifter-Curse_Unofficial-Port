package io.github.apace100.calio.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;

public class UpgradeUtils {
    private static final int LAST_ORIGINS_TAG_VERSION = 3465; // 1.20.1

    public static JsonElement upgradeStack(JsonElement json) {
        return upgradeStack(new Dynamic<>(JsonOps.INSTANCE, json)).getValue();
    }

    public static Tag upgradeStack(Tag tag) {
        return upgradeStack(new Dynamic<>(NbtOps.INSTANCE, tag)).getValue();
    }

    private static <T> Dynamic<T> upgradeStack(Dynamic<T> dynamic) {
        var fixer = DataFixers.getDataFixer();

        // Map "item" -> "id"
        dynamic = (Dynamic<T>) Dynamic.copyField(dynamic, "item", dynamic, "id");
        dynamic = (Dynamic<T>) Dynamic.copyField(dynamic, "Item", dynamic, "id");

        return fixer.update(References.ITEM_STACK, dynamic, LAST_ORIGINS_TAG_VERSION, SharedConstants.WORLD_VERSION);
    }

    public static JsonObject upgradeRecipe(JsonObject json) {
        /*if (json.has("ingredients")) {
            var ingredients = json.get("ingredients");

            if (ingredients.isJsonArray()) {
                var array = ingredients.getAsJsonArray();
                var newArray = new JsonArray();

                for (JsonElement element : array) {
                    if (element.isJsonObject()) {
                        var obj = element.getAsJsonObject();

                        if (obj.has("item")) {
                            newArray.add(obj.get("item").getAsString());
                        } else if (obj.has("tag")) {
                            newArray.add("#" + obj.get("tag").getAsString());
                        }
                    } else if (element.isJsonArray()) {
                        for (JsonElement ele2 : element.getAsJsonArray()) {
                            if (ele2.isJsonObject()) {
                                var obj = ele2.getAsJsonObject();

                                if (obj.has("item")){
                                    newArray.add(obj.get("item").getAsString());
                                } else if (obj.has("tag")) {
                                    newArray.add("#" + obj.get("tag").getAsString());
                                }
                            } else {
                                newArray.add(ele2);
                            }
                        }
                    }
                }

                json.remove("ingredients");
                json.add("ingredients", newArray);
            }
        }*/

        if (json.has("result") && json.get("result").isJsonObject()) {
            var result = json.getAsJsonObject("result");

            if (result.has("item")) {
                result.addProperty("id", result.get("item").getAsString());
            }
        }

        return json;
    }
}
