package net.onixary.shapeShifterCurseFabric.minion;

import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    public void readData(ValueInput input) {
        input.read("data", CompoundTag.CODEC).ifPresent(this::readFromNbt);
    }

    @Override
    public void writeData(ValueOutput output) {
        CompoundTag tag = new CompoundTag();
        this.writeToNbt(tag);
        output.store("data", CompoundTag.CODEC, tag);
    }

    public void readFromNbt(@NotNull CompoundTag nbtCompound) {
        try {
            CompoundTag minionsNbt = nbtCompound.getCompoundOrEmpty("minions");
            for (String key : minionsNbt.keySet()) {
                ListTag uuidList = minionsNbt.getListOrEmpty(key);
                ArrayList<UUID> uuids = new ArrayList<>();
                for (net.minecraft.nbt.Tag nbtElement : uuidList) {
                    uuids.add(UUIDUtil.uuidFromIntArray(((IntArrayTag)nbtElement).getAsIntArray()));
                }
                this.minions.put(Identifier.parse(key), uuids);
            }
            CompoundTag minionsCooldownNbt = nbtCompound.getCompoundOrEmpty("minionsCooldown");
            for (String key : minionsCooldownNbt.keySet()) {
                this.minionsCooldown.put(Identifier.parse(key), minionsCooldownNbt.getLongOr(key, 0L));
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

    public void writeToNbt(@NotNull CompoundTag nbtCompound) {
        CompoundTag minionsNbt = new CompoundTag();
        for (Identifier key : this.minions.keySet()) {
            ListTag uuidList = new ListTag();
            for (UUID uuid : this.minions.get(key)) {
                uuidList.add(new IntArrayTag(UUIDUtil.uuidToIntArray(uuid)));
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
