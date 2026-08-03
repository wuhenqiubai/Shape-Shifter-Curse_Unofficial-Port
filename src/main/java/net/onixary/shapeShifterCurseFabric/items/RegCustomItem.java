package net.onixary.shapeShifterCurseFabric.items;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.material.Fluids;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.items.armors.MorphScaleArmor;
import net.onixary.shapeShifterCurseFabric.items.armors.NetheriteMorphScaleArmor;
import net.onixary.shapeShifterCurseFabric.items.tools.*;
import net.onixary.shapeShifterCurseFabric.items.trinkets.*;
import net.onixary.shapeShifterCurseFabric.util.PatronUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static net.onixary.shapeShifterCurseFabric.blocks.RegCustomBlock.*;
import java.util.function.Function;

public class RegCustomItem {
    private RegCustomItem(){}

    //public static final Item CURSED_BOOK_OF_SHAPE_SHIFTER = register("cursed_book_of_shape_shifter", StartBook::new);
    public static final Item BOOK_OF_SHAPE_SHIFTER = register("book_of_shape_shifter", BookOfShapeShifter::new);
    public static final Item UNTREATED_MOONDUST = register("untreated_moondust", UntreatedMoonDust::new);
    public static final Item INHIBITOR = register("inhibitor", Inhibitor::new);
    public static final Item POWERFUL_INHIBITOR = register("powerful_inhibitor", PowerfulInhibitor::new);
    public static final Item CREATIVE_INHIBITOR = register("creative_inhibitor", CreativeInhibitor::new);
    public static final Item CATALYST = register("catalyst", Catalyst::new);
    public static final Item POWERFUL_CATALYST = register("powerful_catalyst", PowerfulCatalyst::new);
    public static final Item MOONDUST_MATRIX = register("moondust_matrix", MoonDustMatrix::new);
    // morphscale armor
    public static final Item MORPHSCALE_CORE = register("morphscale_core", Item::new);
    public static final Item SUPER_MORPHSCALE_CORE = register("super_morphscale_core", props -> new SuperMorphScaleCore(props.durability(64 * SuperMorphScaleCore.damagePerItem).rarity(Rarity.EPIC)));
    public static final Item MORPHSCALE_HEADRING = register("morphscale_headring", props -> new MorphScaleArmor(net.minecraft.world.item.equipment.ArmorType.HELMET, props));
    public static final Item MORPHSCALE_VEST = register("morphscale_vest", props -> new MorphScaleArmor(net.minecraft.world.item.equipment.ArmorType.CHESTPLATE, props));
    public static final Item MORPHSCALE_CUISH = register("morphscale_cuish", props -> new MorphScaleArmor(net.minecraft.world.item.equipment.ArmorType.LEGGINGS, props));
    public static final Item MORPHSCALE_ANKLET = register("morphscale_anklet", props -> new MorphScaleArmor(net.minecraft.world.item.equipment.ArmorType.BOOTS, props));
    // netherite morphscale armor
    public static final Item NETHERITE_MORPHSCALE_HEADRING = register("netherite_morphscale_headring", props -> new NetheriteMorphScaleArmor(net.minecraft.world.item.equipment.ArmorType.HELMET, props));
    public static final Item NETHERITE_MORPHSCALE_VEST = register("netherite_morphscale_vest", props -> new NetheriteMorphScaleArmor(net.minecraft.world.item.equipment.ArmorType.CHESTPLATE, props));
    public static final Item NETHERITE_MORPHSCALE_CUISH = register("netherite_morphscale_cuish", props -> new NetheriteMorphScaleArmor(net.minecraft.world.item.equipment.ArmorType.LEGGINGS, props));
    public static final Item NETHERITE_MORPHSCALE_ANKLET = register("netherite_morphscale_anklet", props -> new NetheriteMorphScaleArmor(net.minecraft.world.item.equipment.ArmorType.BOOTS, props));
    // 模组自定义物品
    public static final Item MOONDUST_CRYSTAL_SHARD = register("moondust_crystal_shard", MoonDustCrystalShard::new);
    public static final Item ECTOPLASM_RAG = register("ectoplasm_rag", Item::new);
    public static final Item BOTTLED_SNOWFALL = register("bottled_snowfall", props -> new BottledSnowfall(BottledSnowfallToolMaterial.INSTANCE, 1, 1, props));
    public static final Item DIAMOND_MINING_CLAW = register("diamond_mining_claw", props -> new DiamondMiningClaw(DiamondMiningClawToolMaterial.INSTANCE, 1, -2.4f, props));
    public static final Item FIRE_CHARM_PAPER = register("fire_charm_paper", Item::new);
    public static final Item AUXILIARY_SWORD = register("auxiliary_sword", props -> new AuxiliarySword(AuxiliarySwordToolMaterial.INSTANCE, 1, -2.4f, props));
    public static final Item AUXILIARY_PICKAXE = register("auxiliary_pickaxe", props -> new AuxiliaryPickaxe(AuxiliaryPickaxeToolMaterial.INSTANCE, 1, -2.8f, props));
    public static final Item AUXILIARY_AXE = register("auxiliary_axe", props -> new AuxiliaryAxe(AuxiliaryAxeToolMaterial.INSTANCE, 1, -3.1f, props));
    // 模组自定义Trinkets
    public static final Item AMULET_BRACELET = register("amulet_bracelet", AmuletBraceletTrinket::new);
    public static final Item ATTACH_HOOK = register("attach_hook", AttachHookTrinket::new);
    public static final Item CHARM_OF_HOLLOW_FANG = register("charm_of_hollow_fang", CharmOfHollowFangTrinket::new);
    public static final Item CHARM_OF_NIGHT_CRYSTAL = register("charm_of_night_crystal", CharmOfNightCrystalTrinket::new);
    public static final Item CHARM_OF_REVERSE_THERMOMETER = register("charm_of_reverse_thermometer", CharmOfReverseThermometerTrinket::new);
    public static final Item COLLAR_OF_TENSION = register("collar_of_tension", CollarOfTensionTrinket::new);
    public static final Item COLLAR_OF_WHISKERS = register("collar_of_whiskers", CollarOfWhiskersTrinket::new);
    public static final Item DIGESTION_FIBER_BALL = register("digestion_fiber_ball", DigestionFiberBallTrinket::new);
    public static final Item FROST_PAWGLOVE = register("frost_pawglove", FrostPawgloveTrinket::new);
    public static final Item WITHERED_BANDAGE = register("withered_bandage", WitheredBandageTrinket::new);
    public static final Item FOUNTAIN_BELT = register("fountain_belt", FountainBeltTrinket::new);
    public static final Item RESONANT_CORE = register("resonant_core", ResonantCoreTrinket::new);
    public static final Item VENOM_SPINDLE = register("venom_spindle", VenomSpindle::new);

    public static final Item TRANSFORMATIVE_AXOLOTL_BUCKET = register("transformative_axolotl_bucket", props -> new MobBucketItem(ShapeShifterCurseFabric.T_AXOLOTL, Fluids.WATER, SoundEvents.BUCKET_EMPTY_AXOLOTL, props.stacksTo(1)));
    // 减少非蜘蛛玩家食用的中毒量，做到实在没东西吃的时候也能硬着头皮吃的感觉
    public static final Item SPIDER_FLUID_COCOON = register("spider_fluid_cocoon", SpiderFluidCocoon::new);

    public static final Item PATRON_FORM_ITEM = register("patron_form_item", PatronFormItem::new);
    public static final Item SELECT_FORM_ITEM = register("select_form_item", SelectFormItem::new);

    public static final Item CUSTOM_TRINKET = register("custom_trinket", CustomTrinket::new);
    // 用于成就图标的占位物品
    public static final Item ICON_CURSED_MOON = register("icon_cursed_moon", Item::new);
    // 蛛丝弹占位物品
    public static final Item WEB_PROJECTILE = register("web_projectile", Item::new);
    public static final Item SILK_DEW = register("silk_dew", SilkDew::new);

    public static final CreativeModeTab SSC_GROUP = new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
            .icon(() -> new ItemStack(ICON_CURSED_MOON))
            .title(Component.translatable("itemGroup.shape_shifter_curse.sscitems"))
            .displayItems((context, entries) -> {
                entries.accept(BOOK_OF_SHAPE_SHIFTER);
                entries.accept(UNTREATED_MOONDUST);
                entries.accept(MOONDUST_MATRIX);
                entries.accept(MORPHSCALE_CORE);
                entries.accept(SUPER_MORPHSCALE_CORE);
                entries.accept(INHIBITOR);
                entries.accept(POWERFUL_INHIBITOR);
                entries.accept(CREATIVE_INHIBITOR);
                entries.accept(CATALYST);
                entries.accept(POWERFUL_CATALYST);
                entries.accept(MORPHSCALE_HEADRING);
                entries.accept(MORPHSCALE_VEST);
                entries.accept(MORPHSCALE_CUISH);
                entries.accept(MORPHSCALE_ANKLET);
                entries.accept(NETHERITE_MORPHSCALE_HEADRING);
                entries.accept(NETHERITE_MORPHSCALE_VEST);
                entries.accept(NETHERITE_MORPHSCALE_CUISH);
                entries.accept(NETHERITE_MORPHSCALE_ANKLET);
                entries.accept(DIAMOND_MINING_CLAW);
                entries.accept(MOONDUST_CRYSTAL_SHARD);
                entries.accept(ECTOPLASM_RAG);
                entries.accept(AMULET_BRACELET);
                entries.accept(ATTACH_HOOK);
                entries.accept(BOTTLED_SNOWFALL);
                entries.accept(CHARM_OF_HOLLOW_FANG);
                entries.accept(CHARM_OF_NIGHT_CRYSTAL);
                entries.accept(CHARM_OF_REVERSE_THERMOMETER);
                entries.accept(COLLAR_OF_TENSION);
                entries.accept(COLLAR_OF_WHISKERS);
                entries.accept(DIGESTION_FIBER_BALL);
                entries.accept(FROST_PAWGLOVE);
                entries.accept(WITHERED_BANDAGE);
                entries.accept(FOUNTAIN_BELT);
                entries.accept(RESONANT_CORE);
                entries.accept(VENOM_SPINDLE);
                entries.accept(CUSTOM_TRINKET);
                entries.accept(FIRE_CHARM_PAPER);
                entries.accept(TRANSFORMATIVE_AXOLOTL_BUCKET);
                entries.accept(SPIDER_FLUID_COCOON);
                entries.accept(AUXILIARY_SWORD);
                entries.accept(AUXILIARY_PICKAXE);
                entries.accept(AUXILIARY_AXE);
                entries.accept(SELECT_FORM_ITEM);
                entries.accept(SILK_DEW);
                // 方块物品注册
                entries.accept(MOONDUST_CRYSTAL_GRIT);
	            entries.accept(WEB_COMPOSTER);
                entries.accept(DEW_COVERED_COBWEB);
                entries.acceptAll(buildAllPotions(
                        RegCustomPotions.MOONDUST_POTION,
                        RegCustomPotions.BAT_FORM_POTION,
                        RegCustomPotions.AXOLOTL_FORM_POTION,
                        RegCustomPotions.OCELOT_FORM_POTION,
                        RegCustomPotions.FAMILIAR_FOX_FORM_POTION,
                        RegCustomPotions.SNOW_FOX_FORM_POTION,
                        RegCustomPotions.ANUBIS_WOLF_FORM_POTION,
                        RegCustomPotions.SPIDER_FORM_POTION,
                        RegCustomPotions.ALLEY_FORM_POTION,
                        RegCustomPotions.FERAL_CAT_FORM_POTION,
                        RegCustomPotions.CUSTOM_STATUE_FORM_POTION,
                        RegCustomPotions.FEED_POTION
                ));
                if (PatronUtils.EnablePatronFeature) {
                    entries.accept(PATRON_FORM_ITEM);
                }
            })
            .build();

    public static Collection<ItemStack> buildAllPotions(Potion... potions) {
        List<ItemStack> potionStacks = new ArrayList<>();
        for (Potion potion : potions) {
            potionStacks.add(buildPotion(Items.POTION, potion));
        }
        for (Potion potion : potions) {
            potionStacks.add(buildPotion(Items.SPLASH_POTION, potion));
        }
        for (Potion potion : potions) {
            potionStacks.add(buildPotion(Items.LINGERING_POTION, potion));
        }
        for (Potion potion : potions) {
            potionStacks.add(buildPotion(Items.TIPPED_ARROW, potion));
        }
        return potionStacks;
    }

    public static ItemStack buildPotion(Item PotionItem, Potion potion) {
        ItemStack potionStack = new ItemStack(PotionItem);
        potionStack.set(DataComponents.POTION_CONTENTS, new PotionContents(BuiltInRegistries.POTION.wrapAsHolder(potion)));
        return potionStack;
    }

    public static <T extends Item> T register(String path, Function<Item.Properties, T> factory) {
        ResourceKey<Item> key = ResourceKey.create(BuiltInRegistries.ITEM.key(), Identifier.fromNamespaceAndPath(ShapeShifterCurseFabric.MOD_ID, path));
        T item = factory.apply(new Item.Properties().setId(key));
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    public static void initialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ShapeShifterCurseFabric.identifier("ssc_item"), SSC_GROUP);
        /*
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(BOOK_OF_SHAPE_SHIFTER);
            if (PatronUtils.EnablePatronFeature) {
                entries.add(PATRON_FORM_ITEM);
            }
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(UNTREATED_MOONDUST);
            entries.add(MOONDUST_MATRIX);
            entries.add(MORPHSCALE_CORE);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> {
            entries.add(INHIBITOR);
            entries.add(POWERFUL_INHIBITOR);
            entries.add(CREATIVE_INHIBITOR);
            entries.add(CATALYST);
            entries.add(POWERFUL_CATALYST);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(MORPHSCALE_HEADRING);
            entries.add(MORPHSCALE_VEST);
            entries.add(MORPHSCALE_CUISH);
            entries.add(MORPHSCALE_ANKLET);
            entries.add(NETHERITE_MORPHSCALE_HEADRING);
            entries.add(NETHERITE_MORPHSCALE_VEST);
            entries.add(NETHERITE_MORPHSCALE_CUISH);
            entries.add(NETHERITE_MORPHSCALE_ANKLET);
        });
         */
    }
}