package net.onixary.shapeShifterCurseFabric.items.accessory;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 饰品物品基类。提供饰品生命周期钩子（tick/装备/卸下/破坏）和掉落规则。
 * <p>
 * 所有 SSC 中的饰品（项目符号、特殊效果物品等）都继承此类，通过 Trinkets 模组槽位系统集成。
 * 子类通过覆写各生命周期方法实现自定义行为。
 * <p>
 * 掉落规则通过 {@link DropRule} 枚举控制：KEEP 保留、DROP 掉落、DESTROY 销毁、DEFAULT 默认行为。
 *
 * @see net.onixary.shapeShifterCurseFabric.util.TrinketUtils
 * @see SlotData
 */
public abstract class AccessoryItem extends Item {

	/**
	 * 饰品槽位数据。包含槽位 ID 和索引。
	 *
	 * @param slot  槽位 ID（如 {@code trinkets:head}）
	 * @param index 槽位内的索引
	 */
	public record SlotData(Identifier slot, int index) { }

	/**
	 * 饰品掉落规则。
	 */
	public enum DropRule {
		/**
		 * 死亡时保留
		 */
		KEEP,
		/**
		 * 死亡时掉落
		 */
		DROP,
		/**
		 * 死亡时销毁
		 */
		DESTROY,
		/**
		 * 使用默认规则
		 */
		DEFAULT
	}

    public AccessoryItem(Properties settings) {
	    super(settings);
	    this.accessoryInit(settings);
    }

    /** 初始化钩子，在构造器中调用。子类可在此覆写初始设置。 */
    public void accessoryInit(Properties settings) {
    }

    /** 每 tick 调用，用于处理饰品持续效果。 */
    public void accessoryTick(ItemStack stack, LivingEntity accessoryOwner, SlotData slotData) {
    }

    /** 装备时调用。 */
    public void onEquip(ItemStack stack, LivingEntity accessoryOwner, SlotData slotData) {
    }

    /** 卸下时调用。 */
    public void onUnequip(ItemStack stack, LivingEntity accessoryOwner, SlotData slotData) {
    }

    /** @return 是否允许装备此饰品 */
    public boolean canEquip(ItemStack stack, LivingEntity entity, SlotData slotData) {
        return true;
    }

    /** @return 是否允许卸下此饰品 */
    public boolean canUnequip(ItemStack stack, LivingEntity entity, SlotData slotData) {
        return true;
    }

    /** 饰品被破坏时调用。 */
    public void onBreak(ItemStack stack, LivingEntity entity, SlotData slotData) {
    }

    /** @return 此饰品的掉落规则 */
    public DropRule getDropRule(ItemStack stack, LivingEntity entity, SlotData slotData) {
        return DropRule.DEFAULT;
    }
}