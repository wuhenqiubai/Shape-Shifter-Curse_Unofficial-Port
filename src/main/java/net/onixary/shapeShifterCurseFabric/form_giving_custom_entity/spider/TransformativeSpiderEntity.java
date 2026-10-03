package net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.spider;

import net.minecraft.server.level.ServerLevel;
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

    // ⚠ 必须显式覆写 doHurtTarget 才会有触发点：applyDamageEffects 是 Yarn 时代 MobEntity.tryAttack 的钩子名，
    //   移植到 Mojmap 时 @Override 被去掉后它就成了**没有任何调用者**的死代码（方法本身不报错、也不报未使用）。
    //   26.1 的近战命中钩子是 Mob#doHurtTarget(ServerLevel, Entity)，照豹猫的写法补上。
    //   缺了这段 = 蜘蛛攻击玩家时 100% 不给变形效果。
    @Override
    public boolean doHurtTarget(ServerLevel serverLevel, Entity target) {
        boolean hit = super.doHurtTarget(serverLevel, target);
        if (hit) {
            this.applyDamageEffects(this, target);
        }
        return hit;
    }

    @Override
    public @NonNull EntityDimensions getDefaultDimensions(@NonNull Pose pose) {
        return EntityDimensions.fixed(0.7f, 0.45f);
    }


    // 1.21.11: 掉落表默认按 entities/<entity_id> 解析，数据包 data/shape-shifter-curse/loot_table/entities/t_spider.json 自动生效，无需 override


}