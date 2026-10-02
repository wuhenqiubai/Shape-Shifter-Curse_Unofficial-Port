package net.onixary.shapeShifterCurseFabric.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.blocks.RegCustomBlock;
import net.onixary.shapeShifterCurseFabric.recipes.RecipeUtils;
import net.onixary.shapeShifterCurseFabric.recipes.altar.AltarRecipe;
import net.onixary.shapeShifterCurseFabric.recipes.altar.AltarShapedRecipe;
import net.onixary.shapeShifterCurseFabric.recipes.altar.AltarShapelessRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SSC_JEI_Plugin implements IModPlugin {
    public static final RecipeType<WebComposterRecipe> WEB_COMPOSTING = RecipeType.create(ShapeShifterCurseFabric.MOD_ID, "web_compostable", WebComposterRecipe.class);
    public static final RecipeType<AltarShapedRecipe> ALTAR_SHAPED = RecipeType.create(ShapeShifterCurseFabric.MOD_ID, "altar_shaped", AltarShapedRecipe.class);
    public static final RecipeType<AltarShapelessRecipe> ALTAR_SHAPELESS = RecipeType.create(ShapeShifterCurseFabric.MOD_ID, "altar_shapeless", AltarShapelessRecipe.class);

    @Override
    public @NotNull Identifier getPluginUid() {
        return ShapeShifterCurseFabric.identifier("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new WebComposterCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new AltarShapedCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new AltarShapelessCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        IIngredientManager ingredientManager = registration.getIngredientManager();
        registration.addRecipes(WEB_COMPOSTING, WebComposterRecipe.getRecipes(ingredientManager));

        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.getConnection() == null) {
            return;
        }
        // 1.21.11: 客户端不再同步完整配方表 —— ClientPacketListener.getRecipeManager() 已移除，
        // RecipeManager.getAllRecipesFor(RecipeType) 也不存在（客户端侧 ClientRecipeContainer 只剩
        // 配方属性集与石切机配方）。改为从集成服务器取全量配方再按类型过滤。
        // ⚠ 多人游戏下 getSingleplayerServer() 返回 null —— 届时祭坛配方页为空（功能降级）。
        MinecraftServer server = client.getSingleplayerServer();
        if (server == null) {
            return;
        }
        RecipeManager rm = server.getRecipeManager();
        List<AltarRecipe> all = rm.getRecipes().stream()
                .map(RecipeHolder::value)
                .filter(r -> r instanceof AltarRecipe)
                .map(r -> (AltarRecipe) r)
                .toList();

        List<AltarShapedRecipe> shaped = all.stream()
                .filter(r -> r instanceof AltarShapedRecipe)
                .map(r -> (AltarShapedRecipe) r)
                .toList();
        List<AltarShapelessRecipe> shapeless = all.stream()
                .filter(r -> r instanceof AltarShapelessRecipe)
                .map(r -> (AltarShapelessRecipe) r)
                .toList();

        registration.addRecipes(ALTAR_SHAPED, shaped);
        registration.addRecipes(ALTAR_SHAPELESS, shapeless);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(RegCustomBlock.ALTER_BLOCK), ALTAR_SHAPED, ALTAR_SHAPELESS);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(new AltarShapedTransferHandler(), ALTAR_SHAPED);
        registration.addRecipeTransferHandler(new AltarShapelessTransferHandler(), ALTAR_SHAPELESS);
    }
}