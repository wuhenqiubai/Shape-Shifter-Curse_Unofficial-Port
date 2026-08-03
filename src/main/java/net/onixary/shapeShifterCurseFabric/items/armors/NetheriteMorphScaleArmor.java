package net.onixary.shapeShifterCurseFabric.items.armors;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorType;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class NetheriteMorphScaleArmor extends Item implements GeoItem {
    public NetheriteMorphScaleArmor(ArmorType type) {
        this(type, new Item.Properties());
    }
    // 1.21.11: Item 构造即需 Properties.id，注册时通过工厂注入已 setId 的 Properties
    public NetheriteMorphScaleArmor(ArmorType type, Item.Properties properties) {
        super(properties.humanoidArmor(NetheriteMorphscaleArmorMaterial.INSTANCE, type).stacksTo(1).fireResistant());
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag type) {
        consumer.accept(Component.translatable("item.shape-shifter-curse.morphscale_armor.tooltip").withStyle(ChatFormatting.YELLOW));
    }


    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GeoArmorRenderer<NetheriteMorphScaleArmor, MorphscaleArmorRenderState> renderer;

            @Override
            public @org.jetbrains.annotations.Nullable GeoArmorRenderer<?, ?> getGeoArmorRenderer(ItemStack itemStack, EquipmentSlot equipmentSlot) {
                if (this.renderer == null) {
                    this.renderer = new NetheriteMorphscaleArmorRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}