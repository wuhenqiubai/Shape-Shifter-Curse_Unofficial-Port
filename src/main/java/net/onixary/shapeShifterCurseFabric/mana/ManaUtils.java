package net.onixary.shapeShifterCurseFabric.mana;

import com.google.gson.JsonObject;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ManaUtils {
    public record Modifier(double add, double multiply, double add_total) {
        public double applyAdd(double mana) {
            return mana + add;
        }

        public double applyMultiply(double mana) {
            return mana * multiply;
        }

        public double applyAddTotal(double mana) {
            return mana + add_total;
        }

        public static Modifier of(@Nullable Double add, @Nullable Double multiply, @Nullable Double add_total) {
            return new Modifier(add == null ? 0.0d : add, multiply == null ? 1.0d : multiply, add_total == null ? 0.0d : add_total);
        }

        public static Modifier readFromNbt(CompoundTag nbtCompound) {
            return of(nbtCompound.getDouble("add"), nbtCompound.getDouble("multiply"), nbtCompound.getDouble("add_total"));
        }

        public void writeToNbt(CompoundTag nbtCompound) {
            nbtCompound.putDouble("add", add);
            nbtCompound.putDouble("multiply", multiply);
            nbtCompound.putDouble("add_total", add_total);
        }
    }

    public static final SerializableDataType<Modifier> SDT_ManaModifier = SerializableDataType.compound(
            Modifier.class,
            new SerializableData()
                    .add("add", SerializableDataTypes.DOUBLE, 0.0d)
                    .add("multiply", SerializableDataTypes.DOUBLE, 1.0d)
                    .add("add_total", SerializableDataTypes.DOUBLE, 0.0d),
            (serializableData) -> Modifier.of(serializableData.get("add"), serializableData.get("multiply"), serializableData.get("add_total")),
            (serializableData, modifier) -> {
                JsonObject jsonData = new JsonObject();
                jsonData.addProperty("add", modifier.add);
                jsonData.addProperty("multiply", modifier.multiply);
                jsonData.addProperty("add_total", modifier.add_total);
                return serializableData.read(jsonData);
            }
    );

    public static final SerializableDataType<List<Modifier>> SDT_ManaModifierList = SerializableDataType.list(SDT_ManaModifier);

    public static class ModifierList {
        public double lastValue = 0.0d;
        public boolean needSync = false;
        private final LinkedHashMap<ResourceLocation, Tuple<ResourceLocation, Modifier>> modifiers;

        @SafeVarargs
        public ModifierList(Tuple<ResourceLocation, Tuple<ResourceLocation, Modifier>>... modifier) {
            modifiers = new LinkedHashMap<>();
            if (modifier != null) {
                for (Tuple<ResourceLocation, Tuple<ResourceLocation, Modifier>> modifierEntry : modifier) {
                    modifiers.put(modifierEntry.getA(), modifierEntry.getB());
                }
            }
        }

        public ModifierList(ModifierList other) {
            modifiers = new LinkedHashMap<>(other.getModifiers());
        }

        public LinkedHashMap<ResourceLocation, Tuple<ResourceLocation, Modifier>> getModifiers() {
            return modifiers;
        }

        public ModifierList copy() {
            return new ModifierList(this);
        }

        public void add(ResourceLocation identifier, ResourceLocation conditionID, Modifier modifier) {
            modifiers.put(identifier, new Tuple<>(conditionID, modifier));
        }

        public void remove(ResourceLocation identifier) {
            modifiers.remove(identifier);
        }

        private double applyAdd(Player player, double value) {
            for(Map.Entry<ResourceLocation, Tuple<ResourceLocation, Modifier>> modifierEntry : modifiers.entrySet()) {
                ResourceLocation conditionID = modifierEntry.getValue().getA();
                if(ManaRegistries.ManaConditionCheck(conditionID, player)) {
                    value = modifierEntry.getValue().getB().applyAdd(value);
                }
            }
            return value;
        }

        private double applyMultiply(Player player, double value) {
            for(Map.Entry<ResourceLocation, Tuple<ResourceLocation, Modifier>> modifierEntry : modifiers.entrySet()) {
                ResourceLocation conditionID = modifierEntry.getValue().getA();
                if(ManaRegistries.ManaConditionCheck(conditionID, player)) {
                    value = modifierEntry.getValue().getB().applyMultiply(value);
                }
            }
            return value;
        }

        private double applyAddTotal(Player player, double value) {
            for(Map.Entry<ResourceLocation, Tuple<ResourceLocation, Modifier>> modifierEntry : modifiers.entrySet()) {
                ResourceLocation conditionID = modifierEntry.getValue().getA();
                if(ManaRegistries.ManaConditionCheck(conditionID, player)) {
                    value = modifierEntry.getValue().getB().applyAddTotal(value);
                }
            }
            return value;
        }

        public double apply(Player player, double value, ModifierList... otherModifiers) {
            value = this.applyAdd(player, value);
            for (ModifierList otherModifier : otherModifiers) {
                value = otherModifier.applyAdd(player, value);
            }
            value = this.applyMultiply(player, value);
            for (ModifierList otherModifier : otherModifiers) {
                value = otherModifier.applyMultiply(player, value);
            }
            value = this.applyAddTotal(player, value);
            for (ModifierList otherModifier : otherModifiers) {
                value = otherModifier.applyAddTotal(player, value);
            }
            if (value != lastValue) {
                needSync = true;
            }
            lastValue = value;
            return value;
        }

        public void clear() {
            lastValue = 0.0d;
            needSync = false;
            modifiers.clear();
        }

        public void readFromNbt(CompoundTag nbtCompound) {
            modifiers.clear();
            if (nbtCompound.contains("modifiers")) {
                ListTag nbtList = nbtCompound.getList("modifiers", Tag.TAG_COMPOUND);
                for (Tag nbtElement : nbtList) {
                    CompoundTag modifierEntryNbt = (CompoundTag) nbtElement;
                    ResourceLocation identifier = ResourceLocation.parse(modifierEntryNbt.getString("identifier"));
                    ResourceLocation conditionID = ResourceLocation.parse(modifierEntryNbt.getString("conditionID"));
                    Modifier modifier = Modifier.readFromNbt(modifierEntryNbt.getCompound("modifier"));
                    modifiers.put(identifier, new Tuple<>(conditionID, modifier));
                }
            }
        }

        public void writeToNbt(CompoundTag nbtCompound) {
            ListTag nbtList = new ListTag();
            for (Map.Entry<ResourceLocation, Tuple<ResourceLocation, Modifier>> modifierEntry : modifiers.entrySet()) {
                CompoundTag modifierEntryNbt = new CompoundTag();
                modifierEntryNbt.putString("identifier", modifierEntry.getKey().toString());
                modifierEntryNbt.putString("conditionID", modifierEntry.getValue().getA().toString());
                CompoundTag modifierNbt = new CompoundTag();
                modifierEntry.getValue().getB().writeToNbt(modifierNbt);
                modifierEntryNbt.put("modifier", modifierNbt);
                nbtList.add(modifierEntryNbt);
            }
            nbtCompound.put("modifiers", nbtList);
        }
    }

    public static ManaComponent getManaComponent(Player player) {
        return RegManaComponent.MANA.get(player);
    }

    public static void addMaxManaModifier(Player player, ResourceLocation identifier, ResourceLocation conditionID, Modifier modifier, boolean playerSide) {
        if (playerSide) {
            getManaComponent(player).MaxManaModifierPlayerSide.add(identifier, conditionID, modifier);
        } else {
            getManaComponent(player).MaxManaModifier.add(identifier, conditionID, modifier);
        }
    }

    public static void addMaxManaModifier(Player player, ResourceLocation identifier, Modifier modifier, boolean playerSide) {
        addMaxManaModifier(player, identifier, ManaRegistries.MC_AlwaysTrue, modifier, playerSide);
    }

    public static void removeMaxManaModifier(Player player, ResourceLocation identifier, boolean playerSide) {
        if (playerSide) {
            getManaComponent(player).MaxManaModifierPlayerSide.remove(identifier);
        } else {
            getManaComponent(player).MaxManaModifier.remove(identifier);
        }
    }

    public static void addRegenManaModifier(Player player, ResourceLocation identifier, ResourceLocation conditionID, Modifier modifier, boolean playerSide) {
        if (playerSide) {
            getManaComponent(player).ManaRegenModifierPlayerSide.add(identifier, conditionID, modifier);
        } else {
            getManaComponent(player).ManaRegenModifier.add(identifier, conditionID, modifier);
        }
    }

    public static void addRegenManaModifier(Player player, ResourceLocation identifier, Modifier modifier, boolean playerSide) {
        addRegenManaModifier(player, identifier, ManaRegistries.MC_AlwaysTrue, modifier, playerSide);
    }

    public static void removeRegenManaModifier(Player player, ResourceLocation identifier, boolean playerSide) {
        if (playerSide) {
            getManaComponent(player).ManaRegenModifierPlayerSide.remove(identifier);
        } else {
            getManaComponent(player).ManaRegenModifier.remove(identifier);
        }
    }

    public static double getPlayerMana(Player player) {
        return getManaComponent(player).getMana();
    }

    public static double getPlayerMaxMana(Player player) {
        return getManaComponent(player).getMaxMana();
    }

    public static double getPlayerManaRegen(Player player) {
        return getManaComponent(player).getManaRegen();
    }

    public static double getManaPercent(double mana, double maxMana, double if0Result) {
        if (maxMana == 0.0d) {
            return if0Result;
        }
        return mana / maxMana;
    }

    public static double getPlayerManaPercent(Player player, double if0Result) {
        ManaComponent manaComponent = getManaComponent(player);
        return getManaPercent(manaComponent.getMana(), manaComponent.getMaxMana(), if0Result);
    }

    public static ResourceLocation getPlayerManaTypeID(Player player) {
        return getManaComponent(player).getManaTypeID();
    }

    public static double setPlayerMana(Player player, double mana) {
        return getManaComponent(player).setMana(mana);
    }

    public static double gainPlayerMana(Player player, double mana) {
        return getManaComponent(player).gainMana( mana);
    }

    public static double consumePlayerMana(Player player, double mana) {
        return getManaComponent(player).consumeMana(mana);
    }

    public static void gainPlayerManaWithTime(Player player, double mana, int time) {
        if (time <= 0) {
            getManaComponent(player).gainMana(mana * time);
        } else {
            getManaComponent(player).gainManaWithTime(mana, time);
        }
    }

    public static boolean isPlayerManaAbove(Player player, double mana) {
        return getManaComponent(player).isManaAbove(mana);
    }

    // 用于Power系统
    public static void gainManaTypeID(Player player, ResourceLocation manaTypeID, ResourceLocation sourceID) {
        getManaComponent(player).gainManaTypeID(manaTypeID, sourceID);
    }

    public static void loseManaTypeID(Player player, ResourceLocation manaTypeID, ResourceLocation sourceID) {
        getManaComponent(player).loseManaTypeID(manaTypeID, sourceID);
    }

    public static boolean isManaTypeExists(Player player, @NotNull ResourceLocation manaTypeID, @Nullable ResourceLocation sourceID) {
        return getManaComponent(player).isManaTypeExists(manaTypeID, sourceID);
    }

    // 用于其他非Power系统
    public static void setManaTypeID(Player player, ResourceLocation manaTypeID) {
        getManaComponent(player).setManaTypeID(manaTypeID);
    }

    public static void manaTick(Player player) {
        ManaComponent manaComponent = getManaComponent(player);
        manaComponent.tick();
        if (!player.level().isClientSide && manaComponent.isNeedSync()) {
            RegManaComponent.MANA.sync(player);
        }
    }
}