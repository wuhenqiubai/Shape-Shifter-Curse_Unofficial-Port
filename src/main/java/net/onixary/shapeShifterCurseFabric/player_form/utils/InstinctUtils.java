package net.onixary.shapeShifterCurseFabric.player_form.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.cursed_moon.CursedMoon;
import net.onixary.shapeShifterCurseFabric.data.StaticParams;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.ITransformReason;
import net.onixary.shapeShifterCurseFabric.util.ClientUtils;

import java.util.HashMap;
import java.util.UUID;

public class InstinctUtils {
    public static class InstinctEffect {
        private final ResourceLocation id;
        private final float value;
        private int duration;
        public InstinctEffect(ResourceLocation id, float value, int duration) {
            this.id = id;
            this.value = value;
            this.duration = duration;
        }

        public float getValue(boolean isImmediate) {
            if (isImmediate) {
                return value * duration;
            } else {
                return value;
            }
        }

        public ResourceLocation getId() {
            return id;
        }

        public boolean IsEffectExist() {
            duration--;
            return duration >= 0;
        }

        public void toNBT(CompoundTag nbt) {
            nbt.putString("id", id.toString());
            nbt.putFloat("value", value);
            nbt.putInt("duration", duration);
        }

        public static InstinctEffect fromNBT(CompoundTag nbt) {
            return new InstinctEffect(ResourceLocation.parse(nbt.getString("id")), nbt.getFloat("value"), nbt.getInt("duration"));
        }
    }
    // ServerSide Data
    private static final HashMap<UUID, Float> playerInstinctRate = new HashMap<>();  // 当计算出的值与此表中的不同时触发同步
    public static final HashMap<UUID, Boolean> playerInstinctLock = new HashMap<>();
    // ClientSide Data
    private static float nowInstinctValue = 0.0f;
    private static float nowInstinctRate = 0.0f;
    private static int nowInstinctTick = 0;

    // Server Side
    public static void onServerInit() {
        playerInstinctRate.clear();
        playerInstinctLock.clear();
    }

    // Client Only
    public static void fromInstinctUpdate(float value, float rate) {
        nowInstinctValue = value;
        nowInstinctRate = rate;
        nowInstinctTick = 0;
    }

    // Client Only
    public static float getNowInstinct() {
        return Math.min(Math.max(nowInstinctValue + nowInstinctRate * nowInstinctTick, 0.0f), StaticParams.INSTINCT_MAX);
    }

    // Client Only
    public static void clientTick() {
        nowInstinctTick++;
        float instinctValue = getNowInstinct();
        if (instinctValue >= 80.0f && instinctValue < 99.99f) {
           Player player = ClientUtils.getPlayer();
           if (player != null) {
               player.level().addParticle(
                       StaticParams.PLAYER_TRANSFORM_PARTICLE,
                       player.getX() + (player.getRandom().nextDouble() - 0.5) * 0.5,
                       player.getY() + player.getRandom().nextDouble() * 1,
                       player.getZ() + (player.getRandom().nextDouble() - 0.5) * 0.5,
                       0, 1, 0.5
               );
           }
        }
    }

    // Both Side
    public static float calcRate(HashMap<ResourceLocation, InstinctEffect> effects, boolean checkExist) {
        if (checkExist) {
            effects.entrySet().removeIf(entry -> !entry.getValue().IsEffectExist());
        }
        return (float) effects.values().stream().mapToDouble(effect -> effect.getValue(false)).sum();
    }

    // Both Side
    public static float getBaseInstinctRate(Player player) {
        IForm form = FormUtils.getPlayerForm(player);
        if (FormUtils.NoInstinct.hasFlag(form) || FormUtils.LockInstinct.hasFlag(form)) {
            return -100.0f;
        } else {
            return StaticParams.INSTINCT_INCREASE_RATE;
        }
    }

    // Server Side
    public static void serverTick(MinecraftServer server) {
        boolean isInCursedMoon = CursedMoon.isInCursedMoon(server.overworld());
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PlayerFormComponent component = PlayerFormComponent.COMPONENT.get(player);
            float prevRate = playerInstinctRate.getOrDefault(player.getUUID(), 0.0f);
            float nowRate = getBaseInstinctRate(player);
            if (isInCursedMoon || playerInstinctLock.getOrDefault(player.getUUID(), false)) {
                nowRate = 0.0f;
            } else {
                nowRate += calcRate(component.instinctEffects, true);
            }
            playerInstinctRate.put(player.getUUID(), nowRate);
            component.instinctValue += nowRate;
            component.instinctValue = Math.max(component.instinctValue, 0);
            component.instinctRate = nowRate;
            if (prevRate != nowRate) {
                component.sync();
            }
            InstinctUtils.checkThreshold(player, component.instinctValue);
        }
    }

    public static void clearInstinct(Player player) {
        PlayerFormComponent component = PlayerFormComponent.COMPONENT.get(player);
        component.instinctValue = 0.0f;
        component.instinctEffects.clear();
        component.sync();
    }

    private static void checkThreshold(ServerPlayer player, float instinctValue) {
        if (instinctValue >= StaticParams.INSTINCT_MAX) {
            IForm nowForm = FormUtils.getPlayerForm(player);
            IForm targetForm = nowForm._getNextForm(player, ITransformReason.Instinct);
            if (!nowForm.isEquals(targetForm)) {
                TransformManager.startTransform(player, targetForm, null);
            }
            clearInstinct(player);
        }
    }

    public static void addInstinctEffect(Player player, InstinctEffect effect, boolean isImmediate) {
        boolean isInCursedMoon = CursedMoon.isInCursedMoon(player.level());
        PlayerFormComponent component = PlayerFormComponent.COMPONENT.get(player);
        if (isImmediate) {
            component.instinctValue += (isInCursedMoon || playerInstinctLock.getOrDefault(player.getUUID(), false)) ? 0 : effect.getValue(true);
            component.instinctValue = Math.max(component.instinctValue, 0);
        } else {
            component.instinctEffects.put(effect.getId(), effect);
        }
        component.sync();
    }

    public static void addInstinctEffect(Player player, ResourceLocation id, float value, int duration, boolean isImmediate) {
        addInstinctEffect(player, new InstinctEffect(id, value, duration), isImmediate);
    }
}
