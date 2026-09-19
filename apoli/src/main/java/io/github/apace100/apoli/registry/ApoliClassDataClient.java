package io.github.apace100.apoli.registry;

import io.github.apace100.calio.ClassUtil;
import io.github.apace100.calio.data.ClassDataRegistry;
import net.minecraft.client.renderer.entity.layers.*;

public class ApoliClassDataClient {

    public static void registerAll() {
        ClassDataRegistry<RenderLayer<?, ?>> featureRenderer =
            ClassDataRegistry.getOrCreate(ClassUtil.castClass(RenderLayer.class), "FeatureRenderer");
        
        featureRenderer.addMapping("slime_overlay", SlimeOuterLayer.class);
        featureRenderer.addMapping("snowman_pumpkin", SnowGolemHeadLayer.class);
        featureRenderer.addMapping("fox_held_item", FoxHeldItemLayer.class);
        featureRenderer.addMapping("llama_decor", LlamaDecorLayer.class);
        featureRenderer.addMapping("elytra", ElytraLayer.class);
        featureRenderer.addMapping("villager_clothing", VillagerProfessionLayer.class);
        featureRenderer.addMapping("panda_held_item", PandaHoldsItemLayer.class);
        featureRenderer.addMapping("drowned_overlay", DrownedOuterLayer.class);
        featureRenderer.addMapping("saddle", SaddleLayer.class);
        featureRenderer.addMapping("shoulder_parrot", ParrotOnShoulderLayer.class);
        featureRenderer.addMapping("horse_armor", HorseArmorLayer.class);
        featureRenderer.addMapping("wolf_collar", WolfCollarLayer.class);
        featureRenderer.addMapping("energy_swirl_overlay", EnergySwirlLayer.class);
        featureRenderer.addMapping("held_item", ItemInHandLayer.class);
        featureRenderer.addMapping("sheep_wool", SheepFurLayer.class);
        featureRenderer.addMapping("iron_golem_flower", IronGolemFlowerLayer.class);
        featureRenderer.addMapping("cape", CapeLayer.class);
        featureRenderer.addMapping("eyes", EyesLayer.class);
        featureRenderer.addMapping("dolphin_held_item", DolphinCarryingItemLayer.class);
        featureRenderer.addMapping("horse_marking", HorseMarkingLayer.class);
        featureRenderer.addMapping("deadmau5", Deadmau5EarsLayer.class);
        featureRenderer.addMapping("armor", HumanoidArmorLayer.class);
        featureRenderer.addMapping("stray_overlay", SkeletonClothingLayer.class);
        featureRenderer.addMapping("enderman_block", CarriedBlockLayer.class);
        featureRenderer.addMapping("mooshroom_mushroom", MushroomCowMushroomLayer.class);
        featureRenderer.addMapping("iron_golem_crack", IronGolemCrackinessLayer.class);
        featureRenderer.addMapping("villager_held_item", CrossedArmsItemLayer.class);
        featureRenderer.addMapping("trident_riptide", SpinAttackEffectLayer.class);
        featureRenderer.addMapping("head", CustomHeadLayer.class);
        featureRenderer.addMapping("cat_collar", CatCollarLayer.class);
        featureRenderer.addMapping("tropical_fish_color", TropicalFishPatternLayer.class);
        //featureRenderer.addMapping("shulker_head", ShulkerHeadLayer.class); // TODO O-L: this doesn't exist anymore
        featureRenderer.addMapping("stuck_objects", StuckInBodyLayer.class);
        featureRenderer.addMapping("stuck_stingers", BeeStingerLayer.class);
        featureRenderer.addMapping("stuck_arrows", ArrowLayer.class);
    }
}
