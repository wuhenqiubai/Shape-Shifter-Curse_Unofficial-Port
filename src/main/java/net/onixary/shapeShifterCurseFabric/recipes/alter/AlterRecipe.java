package net.onixary.shapeShifterCurseFabric.recipes.alter;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.recipes.RecipeSerializerRegister;
import net.onixary.shapeShifterCurseFabric.recipes.RecipeUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

// 类熔炉配方 多输入物品 单种燃料 多输出物品
public class AlterRecipe implements Recipe<AlterRecipe.AlterRecipeInput> {
    public static final RecipeType<AlterRecipe> ALTER_RECIPE = RecipeUtils.registerRecipeType(ShapeShifterCurseFabric.identifier("alter"));
    public static final ResourceLocation EmptyRecipeId = ShapeShifterCurseFabric.identifier("empty_alter_recipe");

    public static final AlterRecipe EmptyRecipe = new AlterRecipe(EmptyRecipeId, Ingredient.EMPTY, Ingredient.EMPTY, Ingredient.EMPTY, Ingredient.EMPTY, Ingredient.EMPTY, Ingredient.EMPTY, Ingredient.EMPTY, (inventory) -> new ArrayList<>(), 0);

    public static final int InputSlotIndex = 0;
    public static final int InputSlotCount = 7;
    public static final int FuelSlotIndex = 7;
    public static final int FuelSlotCount = 1;
    public static final int OutputSlotIndex = 8;
    public static final int OutputSlotCount = 9;

    public final Ingredient input1;
    public final Ingredient input2;
    public final Ingredient input3;
    public final Ingredient input4;
    public final Ingredient input5;
    public final Ingredient input6;
    public final Ingredient input7;
    public final Function<@Nullable Container, List<ItemStack>> output;  // 支持从战利品表拉取
    public final int recipeTime;

    public ItemStack VirtualOutput;

    public final ResourceLocation id;

    public AlterRecipe(ResourceLocation id, Ingredient input1, Ingredient input2, Ingredient input3, Ingredient input4, Ingredient input5, Ingredient input6, Ingredient input7, Function<@Nullable Container, List<ItemStack>> output, int recipeTime) {
        this.id = id;
        this.input1 = input1;
        this.input2 = input2;
        this.input3 = input3;
        this.input4 = input4;
        this.input5 = input5;
        this.input6 = input6;
        this.input7 = input7;
        this.output = output;
        this.recipeTime = recipeTime;
        this.VirtualOutput = this.getVirtualOutput(null);
        List<ItemStack> list = output.apply(null);
        if (list.size() > 9) {
            ShapeShifterCurseFabric.LOGGER.warn("AlterRecipe " + id + " has more than 9 outputs!");  // 警告一下 防止吞物品
        }
    }

    public ItemStack getVirtualOutput(@Nullable Container inventory) {
        List<ItemStack> list = output.apply(inventory);
        if (!list.isEmpty()) {
            if (list.size() >= 5) {
                return list.get(4);
            } else {
                return list.get(0);
            }
        } else {
            return ItemStack.EMPTY;
        }
    }

	@Override
	public boolean matches(AlterRecipeInput input, Level world) {
		if (this.id.equals(EmptyRecipeId)) {
			return false;
		}
		boolean noPass = false;
		noPass |= !this.input1.test(input.getItem(InputSlotIndex));
		noPass |= !this.input2.test(input.getItem(InputSlotIndex + 1));
		noPass |= !this.input3.test(input.getItem(InputSlotIndex + 2));
		noPass |= !this.input4.test(input.getItem(InputSlotIndex + 3));
		noPass |= !this.input5.test(input.getItem(InputSlotIndex + 4));
		noPass |= !this.input6.test(input.getItem(InputSlotIndex + 5));
		noPass |= !this.input7.test(input.getItem(InputSlotIndex + 6));
		return !noPass;
	}

    @Override
    public ItemStack assemble(AlterRecipeInput input, HolderLookup.Provider lookup) {
	    return this.getVirtualOutput(input.getInventory());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return !this.id.equals(EmptyRecipeId);
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider lookup) {
	    return this.VirtualOutput;
    }

	public static class AlterRecipeInput implements RecipeInput {
		private final Container inventory;

		public AlterRecipeInput(Container inventory) {
			this.inventory = inventory;
		}

		public Container getInventory() {
			return inventory;
		}

		@Override
		public ItemStack getItem(int slot) {
			return inventory.getItem(slot);
		}

		@Override
		public int size() {
			return inventory.getContainerSize();
		}
	}

    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializerRegister.ALTER_RECIPE;
    }

    @Override
    public RecipeType<?> getType() {
        return ALTER_RECIPE;
    }

    public static class Serializer implements RecipeSerializer<AlterRecipe> {

	    private static final MapCodec<AlterRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			    Ingredient.CODEC_NONEMPTY.fieldOf("input1").forGetter(r -> r.input1),
			    Ingredient.CODEC_NONEMPTY.fieldOf("input2").forGetter(r -> r.input2),
			    Ingredient.CODEC_NONEMPTY.fieldOf("input3").forGetter(r -> r.input3),
			    Ingredient.CODEC_NONEMPTY.fieldOf("input4").forGetter(r -> r.input4),
			    Ingredient.CODEC_NONEMPTY.fieldOf("input5").forGetter(r -> r.input5),
			    Ingredient.CODEC_NONEMPTY.fieldOf("input6").forGetter(r -> r.input6),
			    Ingredient.CODEC_NONEMPTY.fieldOf("input7").forGetter(r -> r.input7),
			    ItemStack.CODEC.listOf().fieldOf("output").forGetter(r -> r.output.apply(null)),
			    com.mojang.serialization.Codec.INT.optionalFieldOf("recipeTime", 200).forGetter(r -> r.recipeTime)
	    ).apply(instance, (i1, i2, i3, i4, i5, i6, i7, outputList, time) ->
			    new AlterRecipe(ResourceLocation.parse("alter"), i1, i2, i3, i4, i5, i6, i7, inv -> outputList, time)
	    ));

	    private static final StreamCodec<RegistryFriendlyByteBuf, AlterRecipe> PACKET_CODEC = new StreamCodec<>() {
		    @Override
		    public AlterRecipe decode(RegistryFriendlyByteBuf buf) {
			    Ingredient i1 = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
			    Ingredient i2 = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
			    Ingredient i3 = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
			    Ingredient i4 = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
			    Ingredient i5 = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
			    Ingredient i6 = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
			    Ingredient i7 = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
			    List<ItemStack> outputs = new ArrayList<>();
			    int outputCount = buf.readVarInt();
			    for (int i = 0; i < outputCount; i++) {
				    outputs.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
                }
			    int time = buf.readVarInt();
			    return new AlterRecipe(ResourceLocation.parse("alter"), i1, i2, i3, i4, i5, i6, i7, inv -> outputs, time);
		    }

		    @Override
		    public void encode(RegistryFriendlyByteBuf buf, AlterRecipe recipe) {
			    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.input1);
			    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.input2);
			    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.input3);
			    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.input4);
			    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.input5);
			    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.input6);
			    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.input7);
			    List<ItemStack> outputs = recipe.output.apply(null);
			    buf.writeVarInt(outputs.size());
			    for (ItemStack stack : outputs) {
				    ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
                }
			    buf.writeVarInt(recipe.recipeTime);
            }
	    };

	    @Override
	    public MapCodec<AlterRecipe> codec() {
		    return CODEC;
	    }

	    @Override
	    public StreamCodec<RegistryFriendlyByteBuf, AlterRecipe> streamCodec() {
		    return PACKET_CODEC;
        }
    }
}