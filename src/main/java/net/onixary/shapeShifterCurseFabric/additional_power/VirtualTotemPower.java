package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.CooldownPower;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.networking.ModPacketsS2CServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

// 由于网络同步问题 仅支持玩家实体 非玩家实体不会触发客户端效果
public class VirtualTotemPower extends CooldownPower {
    public static final HashMap<ResourceLocation, BiConsumer<Player, ItemStack>> virtualTotemTypeMap = new HashMap<>();

    static {
        virtualTotemTypeMap.put(ShapeShifterCurseFabric.identifier("default"), (Player playerEntity, ItemStack totemStack) -> {
            Minecraft client = Minecraft.getInstance();
            if (totemStack == null) {
                totemStack = new ItemStack(Items.TOTEM_OF_UNDYING, 1);
            }
            if (client.level != null) {
                client.particleEngine.createTrackingEmitter(playerEntity, ParticleTypes.TOTEM_OF_UNDYING, 30);
                client.level.playLocalSound(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), SoundEvents.TOTEM_USE, playerEntity.getSoundSource(), 1.0f, 1.0f, false);
                if (playerEntity != client.player) return;
                client.gameRenderer.displayItemActivation(totemStack);
            }
        });
        virtualTotemTypeMap.put(ShapeShifterCurseFabric.identifier("form_anubis_wolf_3_undying"), (Player playerEntity, ItemStack totemStack) -> {
            Minecraft client = Minecraft.getInstance();
            if (client.level != null) {
                client.particleEngine.createTrackingEmitter(playerEntity, ParticleTypes.SMOKE, 30);
                client.particleEngine.createTrackingEmitter(playerEntity, ParticleTypes.TOTEM_OF_UNDYING, 30);
                client.level.playLocalSound(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), SoundEvents.WITHER_DEATH, playerEntity.getSoundSource(), 0.75f, 0.8f, false);
            }
        });
    }

    public ResourceLocation virtualTotemType;  // 用于播放动画
    public ItemStack totemStack;  // 当VirtualTotemPowerID == 0时 模拟原版不死图腾
    private final List<Consumer<Entity>> entityAction;
    private final int totemHealth;
    private final List<MobEffectInstance> totemStatusEffects;

    public VirtualTotemPower(PowerType<?> type, LivingEntity entity, SerializableData.Instance data) {
        super(type, entity, data.get("cooldown"), data.get("hud_render"));
        this.virtualTotemType = data.get("virtual_totem_type");
        this.totemStack = data.get("totem_stack");
        this.entityAction = data.get("entity_actions");
        this.totemHealth = data.get("totem_health");
        this.totemStatusEffects = data.get("totem_status_effects");
    }

    // 应该不用同步配置 Apoli应该会把SerializableData.Instance同步到客户端
    public Tag toTag(HolderLookup.Provider provider) {
        return super.toTag(provider);
    }

    public void fromTag(Tag tag, HolderLookup.Provider provider) {
        super.fromTag(tag, provider);
    }

    public void use() {
        if (this.entity == null) {
            ShapeShifterCurseFabric.LOGGER.error("VirtualTotemPower: entity is null");
            return;
        }
        this.entity.setHealth(this.totemHealth);
        if (this.totemStatusEffects != null) {
            for (MobEffectInstance statusEffectInstance : this.totemStatusEffects) {
                this.entity.addEffect(new MobEffectInstance(statusEffectInstance));
            }
        }
        if (this.entityAction != null) {
            for (Consumer<Entity> consumer : this.entityAction) {
                consumer.accept(this.entity);
            }
        }
        if (!this.entity.level().isClientSide && this.entity instanceof ServerPlayer serverPlayerEntity) {
            ModPacketsS2CServer.sendActiveVirtualTotem(serverPlayerEntity, this);
        }
        super.use();
    }

    public @Nullable FriendlyByteBuf create_packet_byte_buf() {
        if (this.entity instanceof ServerPlayer serverPlayerEntity) {
            RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), serverPlayerEntity.getServer().registryAccess());
            buf.writeUUID(serverPlayerEntity.getUUID());
            buf.writeResourceLocation(this.virtualTotemType);
            buf.writeBoolean(this.totemStack != null && !this.totemStack.isEmpty());
            if (this.totemStack != null && !this.totemStack.isEmpty()) {
                ItemStack.STREAM_CODEC.encode(buf, this.totemStack);
            }
            return buf;
        }
        return null;
    }

    public static void process_virtual_totem_type(@NotNull Player entity, ResourceLocation virtualTotemType, @Nullable ItemStack totemStack) {
        Minecraft client = Minecraft.getInstance();
        if (virtualTotemTypeMap.containsKey(virtualTotemType)) {
            virtualTotemTypeMap.get(virtualTotemType).accept(entity, totemStack);
        } else {
            ShapeShifterCurseFabric.LOGGER.error("VirtualTotemPower: unknown virtualTotemType: {}", virtualTotemType);
        }
    }

    public static PowerFactory<?> createFactory() {
        return new PowerFactory<>(
                ShapeShifterCurseFabric.identifier("virtual_totem"),
                new SerializableData()
                        .add("virtual_totem_type", SerializableDataTypes.IDENTIFIER, ShapeShifterCurseFabric.identifier("default"))
                        .add("totem_stack", SerializableDataTypes.ITEM_STACK, new ItemStack(Items.TOTEM_OF_UNDYING, 1))
                        .add("entity_actions", ApoliDataTypes.ENTITY_ACTIONS, null)
                        .add("totem_health", SerializableDataTypes.INT, 1)  // 默认1
                        .add("totem_status_effects", SerializableDataTypes.STATUS_EFFECT_INSTANCES, null)
                        .add("cooldown", SerializableDataTypes.INT, 1200)  // 默认1分钟
                        .add("hud_render", ApoliDataTypes.HUD_RENDER, HudRender.DONT_RENDER),
                data -> (powerType, entity) -> new VirtualTotemPower(powerType, entity, data)
        ).allowCondition();
    }

}
