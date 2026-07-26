package net.onixary.shapeShifterCurseFabric.recipes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.additional_power.IsMorphScaleItemCondition;
import net.onixary.shapeShifterCurseFabric.items.RegCustomItem;
import net.onixary.shapeShifterCurseFabric.items.tools.SuperMorphScaleCore;

import static net.onixary.shapeShifterCurseFabric.recipes.RecipeSerializerRegister.MORPH_SCALE_UPGRADE;

public class MorphScaleUpgradeRecipe extends UpgradeRecipe {
    @Override
    public net.minecraft.world.item.crafting.RecipeType<?> getType() {
        return net.minecraft.world.item.crafting.RecipeType.SMITHING;
    }
    public final Ingredient template;
    public final Ingredient addition;

    public boolean isUpgradeAll() {
        return ShapeShifterCurseFabric.commonConfig.enableFullStackUpgrade;
    }

    public MorphScaleUpgradeRecipe(ResourceLocation id, Ingredient template, Ingredient addition) {
        super(id, template, (itemStack -> {
            if (itemStack.isEmpty()) {
                return false;
            }
	        var customData = itemStack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
	        if (customData == null) {
                return true;
            }
	        CompoundTag nbtCompound = customData.copyTag();
            return !(nbtCompound.contains(IsMorphScaleItemCondition.IsMorphScaleArmorTagName) && nbtCompound.getBoolean(IsMorphScaleItemCondition.IsMorphScaleArmorTagName));
        }), addition, itemStack -> {
	        // 使用 Component 系统设置标记
	        CompoundTag nbt = itemStack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
	        nbt.putBoolean(IsMorphScaleItemCondition.IsMorphScaleArmorTagName, true);
	        itemStack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.of(nbt));
            return itemStack;
        });
        this.template = template;
        this.addition = addition;
    }

    @Override
    public ItemStack assemble(net.minecraft.world.item.crafting.SmithingRecipeInput input, net.minecraft.core.HolderLookup.Provider lookup) {
	    ItemStack coreStack = input.template();
        if (coreStack.is(RegCustomItem.SUPER_MORPHSCALE_CORE)) {
	        ItemStack itemStack = input.base();
            int multiplier = SuperMorphScaleCore.getUpgradeDamageMultiplier(itemStack);
            int canCraftCount = SuperMorphScaleCore.getMaxUseCount(coreStack, multiplier);
            if (this.base.test(itemStack) && canCraftCount > 0) {
                ItemStack outputStack = itemStack.copy();
                outputStack.setCount(1);
                return this.upgradeResult.apply(outputStack);
            }
            return ItemStack.EMPTY;
        }
	    return super.assemble(input, lookup);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MORPH_SCALE_UPGRADE;
    }

    @Override
    public boolean overrideVanillaOnTakeOutput() {
        return true;
    }

    @Override
    public void onTakeOutput(SmithingMenu screenHandler, Player player, ItemStack stack) {
        ItemStack coreStack = screenHandler.inputSlots.getItem(0);
        if (coreStack.is(RegCustomItem.SUPER_MORPHSCALE_CORE)) {
            ItemStack baseStack = screenHandler.inputSlots.getItem(1);
            int multiplier = SuperMorphScaleCore.getUpgradeDamageMultiplier(baseStack);
            SuperMorphScaleCore.damageItemAfterUpgrade(coreStack, multiplier);
            screenHandler.shrinkStackInSlot(1);
            screenHandler.shrinkStackInSlot(2);
        }
        else {
            screenHandler.shrinkStackInSlot(0);
            if (this.isUpgradeAll()) {
                screenHandler.inputSlots.setItem(1, ItemStack.EMPTY);
            } else {
                screenHandler.shrinkStackInSlot(1);
            }
            screenHandler.shrinkStackInSlot(2);
        }
    }

    public static class Serializer implements RecipeSerializer<MorphScaleUpgradeRecipe> {

        private static final MapCodec<MorphScaleUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("template").forGetter(r -> r.template),
            Ingredient.CODEC_NONEMPTY.fieldOf("addition").forGetter(r -> r.addition)
        ).apply(instance, (template, addition) -> new MorphScaleUpgradeRecipe(ResourceLocation.parse("morph_scale_upgrade"), template, addition)));

        private static final StreamCodec<RegistryFriendlyByteBuf, MorphScaleUpgradeRecipe> PACKET_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.template,
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.addition,
            (template, addition) -> new MorphScaleUpgradeRecipe(ResourceLocation.parse("morph_scale_upgrade"), template, addition)
        );

        @Override
        public MapCodec<MorphScaleUpgradeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MorphScaleUpgradeRecipe> streamCodec() {
            return PACKET_CODEC;
        }
    }
}