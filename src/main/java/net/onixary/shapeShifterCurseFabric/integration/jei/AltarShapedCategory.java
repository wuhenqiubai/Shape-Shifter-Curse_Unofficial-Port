package net.onixary.shapeShifterCurseFabric.integration.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.blocks.RegCustomBlock;
import net.onixary.shapeShifterCurseFabric.items.RegCustomItem;
import net.onixary.shapeShifterCurseFabric.recipes.altar.AltarShapedRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class AltarShapedCategory extends AbstractRecipeCategory<AltarShapedRecipe> {
    private static final ResourceLocation TEXTURE = ShapeShifterCurseFabric.identifier("textures/gui/altar_craft_ui.png");

    private final IDrawable background;
    private final IDrawable arrow;

    public AltarShapedCategory(IGuiHelper guiHelper) {
        super(SSC_JEI_Plugin.ALTAR_SHAPED,
                Component.translatable("gui.shape_shifter_curse.category.altar_shaped"),
                guiHelper.createDrawableItemLike(RegCustomBlock.ALTER_BLOCK),
                174, 79);
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 174, 79);
        this.arrow = guiHelper.createDrawable(TEXTURE, 174, 0, 43, 9);
    }

    @Override
    public boolean needsRecipeBorder() {
        return false;
    }

    @Override
    public void draw(@NotNull AltarShapedRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics drawContext, double mouseX, double mouseY) {
        background.draw(drawContext, 0, 0);
        arrow.draw(drawContext, 84, 39);
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull AltarShapedRecipe recipe, @NotNull IFocusGroup focuses) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int x = 26 + col * 18;
                int y = 17 + row * 18;
                if (row < recipe.pattern.height() && col < recipe.pattern.width()) {
                    Ingredient ing = recipe.pattern.ingredients().get(col + row * recipe.pattern.width());
                    if (!ing.isEmpty()) {
                        builder.addInputSlot(x, y).addIngredients(ing);
                        continue;
                    }
                }
                builder.addInputSlot(x, y);
            }
        }

        if (recipe.catalyst != null) {
            builder.addInputSlot(97, 22).addIngredients(recipe.catalyst);
        } else {
            builder.addInputSlot(97, 22);
        }

        if (recipe.fuelUsage() > 0) {
            builder.addInputSlot(84, 53).addItemStack(new ItemStack(RegCustomItem.UNTREATED_MOONDUST));
        } else {
            builder.addInputSlot(84, 53);
        }

        RegistryAccess drm = Minecraft.getInstance().level != null
                ? Minecraft.getInstance().level.registryAccess()
                : RegistryAccess.EMPTY;
        builder.addOutputSlot(134, 35).addItemStack(recipe.getResultItem(drm));
    }

    @Override
    public void getTooltip(@NotNull ITooltipBuilder tooltip, @NotNull AltarShapedRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (mouseX >= 84 && mouseX <= 127 && mouseY >= 39 && mouseY <= 48) {
            tooltip.add(Component.translatable("gui.shape_shifter_curse.jei.altar.recipe_id", recipe.getSerializer().toString()));
            if (recipe.requireAdvancement != null) {
                tooltip.add(Component.translatable("gui.shape_shifter_curse.jei.altar.requires_advancement", getAdvancementName(recipe.requireAdvancement)));
            }
        }
    }

    private Component getAdvancementName(ResourceLocation id) {
        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() != null) {
            // 1.21.1: Advancement 是 record，display() 返回 Optional<DisplayInfo>；进度未加载时 get(id) 返回 null
            return Optional.ofNullable(client.getConnection().getAdvancements().getTree().get(id))
                    .flatMap(node -> node.advancement().display())
                    .map(DisplayInfo::getTitle)
                    .orElse(Component.literal(id.toString()));
        }
        return Component.literal(id.toString());
    }
}