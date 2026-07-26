package net.onixary.shapeShifterCurseFabric.additional_power;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.mana.ManaUtils;
import org.jetbrains.annotations.Nullable;

public class ManaTypePower extends Power {
    private @Nullable ResourceLocation manaType = null;
    private @Nullable ResourceLocation manaSource = null;

    public ManaTypePower(PowerType<?> type, LivingEntity entity, @Nullable ResourceLocation manaType, @Nullable ResourceLocation manaSource) {
        super(type, entity);
        this.manaType = manaType;
        if (manaSource == null) {
            this.manaSource = type.getIdentifier();
        } else {
            this.manaSource = manaSource;
        }
    }


    @Override
    public void onAdded() {
        // 写个保底 治标不治本
        if (this.entity instanceof ServerPlayer playerEntity && manaType != null) {
            if (!ManaUtils.isManaTypeExists(playerEntity, manaType, manaSource)) {
                ManaUtils.gainManaTypeID(playerEntity, manaType, manaSource);
            }
        }
    }

    // 在能力获取
    @Override
    public void onGained() {
        if (this.entity instanceof ServerPlayer playerEntity && manaType != null) {
            if (!ManaUtils.isManaTypeExists(playerEntity, manaType, manaSource)) {
                ManaUtils.gainManaTypeID(playerEntity, manaType, manaSource);
            }
            // 获得 Power 时补满魔力
            ManaUtils.gainPlayerMana(playerEntity, Double.MAX_VALUE / 8);
        }
    }

    // 在能力移除
    @Override
    public void onLost() {
        // 不知道为什么有时Apoli会在玩家死亡时调用onLost 而且在我(XuHaoNan)电脑上复现概率极低 没法测具体原因 先写个治标不治本的解决方案
        if (this.entity instanceof ServerPlayer playerEntity && manaType != null) {
            if (ManaUtils.isManaTypeExists(playerEntity, manaType, manaSource)) {
                ManaUtils.loseManaTypeID(playerEntity, manaType, manaSource);
            }
        }
    }

    @Override
    public void onRespawn() {
        // 写个保底 治标不治本
        if (this.entity instanceof ServerPlayer playerEntity && manaType != null) {
            if (!ManaUtils.isManaTypeExists(playerEntity, manaType, manaSource)) {
                ManaUtils.gainManaTypeID(playerEntity, manaType, manaSource);
            }
            // 调整：复活时也会补满魔力值
            ManaUtils.gainPlayerMana(playerEntity, Double.MAX_VALUE / 8);
        }
    }

    public static PowerFactory<?> createFactory() {
        return new PowerFactory<>(
                ShapeShifterCurseFabric.identifier("mana_type_power"),
                new SerializableData()
                        .add("mana_type", SerializableDataTypes.IDENTIFIER, null)
                        .add("mana_source", SerializableDataTypes.IDENTIFIER, null),
                (data) -> (type, entity) -> new ManaTypePower(type, entity, data.get("mana_type"), data.get("mana_source"))
        );
    }
}
