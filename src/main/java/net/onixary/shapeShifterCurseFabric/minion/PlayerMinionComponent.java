package net.onixary.shapeShifterCurseFabric.minion;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.Identifier;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerMinionComponent implements Component, AutoSyncedComponent {
    public ConcurrentHashMap<Identifier, ArrayList<UUID>> minions = new ConcurrentHashMap<>();
    public ConcurrentHashMap<Identifier, Long> minionsCooldown = new ConcurrentHashMap<>();

    @Override
    public void readFromNbt(@NotNull CompoundTag nbtCompound, HolderLookup.@NotNull Provider registryLookup) {
        try {
            CompoundTag minionsNbt = nbtCompound.getCompound("minions");
            for (String key : minionsNbt.getAllKeys()) {
                ListTag uuidList = minionsNbt.getList(key, 11);
                ArrayList<UUID> uuids = new ArrayList<>();
                for (net.minecraft.nbt.Tag nbtElement : uuidList) {
                    uuids.add(NbtUtils.loadUUID(nbtElement));
                }
                this.minions.put(Identifier.parse(key), uuids);
            }
            CompoundTag minionsCooldownNbt = nbtCompound.getCompound("minionsCooldown");
            for (String key : minionsCooldownNbt.getAllKeys()) {
                this.minionsCooldown.put(Identifier.parse(key), minionsCooldownNbt.getLong(key));
            }
        } catch (IllegalArgumentException e) {
            this.minions = new ConcurrentHashMap<>();
            this.minionsCooldown = new ConcurrentHashMap<>();
        } catch (Exception e) {
            ShapeShifterCurseFabric.LOGGER.error("Error reading minions from NBT", e);
            this.minions = new ConcurrentHashMap<>();
        }
    }

    public void clear() {
        this.minions.clear();
        this.minionsCooldown.clear();
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag nbtCompound, HolderLookup.@NotNull Provider registryLookup) {
        CompoundTag minionsNbt = new CompoundTag();
        for (Identifier key : this.minions.keySet()) {
            ListTag uuidList = new ListTag();
            for (UUID uuid : this.minions.get(key)) {
                IntArrayTag uuidNBT = NbtUtils.createUUID(uuid);
                uuidList.add(uuidNBT);
            }
            minionsNbt.put(key.toString(), uuidList);
        }
        nbtCompound.put("minions", minionsNbt);
        CompoundTag minionsCooldownNbt = new CompoundTag();
        for (Identifier key : this.minionsCooldown.keySet()) {
            minionsCooldownNbt.putLong(key.toString(), this.minionsCooldown.get(key));
        }
        nbtCompound.put("minionsCooldown", minionsCooldownNbt);
    }
}