package net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.spider;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.data.StaticParams;
import net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.ITMob;
import net.onixary.shapeShifterCurseFabric.status_effects.BaseTransformativeStatusEffect;
import org.jspecify.annotations.NonNull;

import static net.onixary.shapeShifterCurseFabric.status_effects.RegTStatusEffect.TO_SPIDER_0_EFFECT;

public class TransformativeSpiderEntity extends Spider implements ITMob {
    public TransformativeSpiderEntity(EntityType<? extends Spider> entityType, Level world) {
        super(entityType, world);
    }

    public static AttributeSupplier.@NonNull Builder createAttributes() {
        // 1.21.11: 用 Monster.createMonsterAttributes()（Spider 是 Monster，含对应属性）
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 8.0f)
                .add(Attributes.ATTACK_DAMAGE, StaticParams.CUSTOM_MOB_DEFAULT_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, 0.3f);
    }

    @Override
    public float getStatusChance() {
        return 0.5f;
    }

    @Override
    public BaseTransformativeStatusEffect getStatusEffect() {
        return TO_SPIDER_0_EFFECT;
    }

    @Override
    public void tick() {
        super.tick();
        this.TMob_Tick(this);
    }

    public void applyDamageEffects(LivingEntity attacker, Entity target) {
        // 在applyStatusByChance里面已经判断形态了 无需在外面判断
        if (target instanceof Player player) {
            ITMob.applyStatusByChance(this.getStatusChance(), player, this.getStatusEffect());
        }
    }

    @Override
    public @NonNull EntityDimensions getDefaultDimensions(@NonNull Pose pose) {
        return EntityDimensions.fixed(0.7f, 0.45f);
    }


    // 1.21.11: 掉落表默认按 entities/<entity_id> 解析，数据包 data/shape-shifter-curse/loot_table/entities/t_spider.json 自动生效，无需 override


}