package net.onixary.shapeShifterCurseFabric.blocks.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.onixary.shapeShifterCurseFabric.blocks.RegCustomBlock;
import net.onixary.shapeShifterCurseFabric.custom_ui.AlterCraftUIHandler;
import net.onixary.shapeShifterCurseFabric.custom_ui.RegMenuType;
import net.onixary.shapeShifterCurseFabric.items.RegCustomItem;
import net.onixary.shapeShifterCurseFabric.recipes.RecipeUtils;
import net.onixary.shapeShifterCurseFabric.recipes.alter.AlterRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AlterBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, RecipeCraftingHolder, StackedContentsCompatible {
    // 进度锁是个不错的设计 能降低难度(毕竟之前做限制进度使用得上对应阶段的材料 有些材料是真不好量产 有这个就能用便宜材料了)
    public UUID lastUser;
    public AlterRecipe nowRecipe;
    public RecipeHolder<?> nowRecipeHolder;
    public int progress = 0;
    public int totalProgress = 0;  // Only Client
    public int fuelTime = 0;
    public int totalFuelTime = 0;  // Only Client
    public final NonNullList<ItemStack> inventory;

    public boolean needCheckRecipe = true;
    public final ContainerData propertyDelegate;

    public static final int[] TOP = {0, 1, 2, 3, 4, 5, 6, 7, 8};
    public static final int[] SIDE = {9};
    public static final int[] BOTTOM = {10};

    public static final HashMap<Item, Integer> fuelTimeMap = new HashMap<>();

    private final RecipeManager.CachedCheck<RecipeInput, ? extends AlterRecipe> matchGetter;

    static {
        fuelTimeMap.put(RegCustomItem.UNTREATED_MOONDUST, 800);
    }

    public static boolean canFuel(ItemStack stack) {
        return fuelTimeMap.containsKey(stack.getItem());
    }

    public static int getFuelTime(ItemStack stack) {
        return fuelTimeMap.getOrDefault(stack.getItem(), 0);
    }

    public AlterBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(RegCustomBlock.ALTER_BLOCK_ENTITY, blockPos, blockState);
        this.inventory = NonNullList.withSize(11, ItemStack.EMPTY);
        this.matchGetter = RecipeManager.createCheck(RecipeUtils.ALTER_RECIPE);
        this.propertyDelegate = new ContainerData() {
            public int get(int index) {
                switch (index) {
                    case 0 -> {
                        return AlterBlockEntity.this.progress;
                    }
                    case 1 -> {
                        return AlterBlockEntity.this.totalProgress;
                    }
                    case 2 -> {
                        return AlterBlockEntity.this.fuelTime;
                    }
                    case 3 -> {
                        return AlterBlockEntity.this.totalFuelTime;
                    }
                    default -> {
                        return 0;
                    }
                }
            }

            public void set(int index, int value) {
                switch (index) {
                    case 0 -> AlterBlockEntity.this.progress = value;
                    case 1 -> AlterBlockEntity.this.totalProgress = value;
                    case 2 -> AlterBlockEntity.this.fuelTime = value;
                    case 3 -> AlterBlockEntity.this.totalFuelTime = value;
                }

            }

            public int size() {
                return 4;
            }
        };
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.literal("ALTER TEST NAME");
    }

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int syncId, Inventory playerInventory) {
        return new AlterCraftUIHandler(RegMenuType.AlterCraftUI, syncId, playerInventory, this, ScreenHandlerContext.EMPTY, this.propertyDelegate);
    }

    @Override
    public int @NotNull [] getSlotsForFace(Direction side) {
        return switch (side) {
            case UP -> TOP;
            case DOWN -> BOTTOM;
            case EAST, WEST, NORTH, SOUTH -> SIDE;
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        return switch (slot) {
            case 0, 1, 2, 3, 4, 5, 6, 7, 8 -> true;
            case 9 -> canFuel(stack);
            case 10 -> false;
            default -> false;
        };
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    @Override
    public int getContainerSize() {
        return inventory.size();
    }

    // 构造 3x3 的 RecipeInput（前 9 格）给 Recipe 匹配。
    // AlterBlockEntity 自身不 implements RecipeInput，避免与 WorldlyContainer 的 getItem/isEmpty 双接口在 remap 时二义。
    private RecipeInput craftInput() {
        return CraftingInput.of(3, 3, List.copyOf(this.inventory.subList(0, 9)));
    }

    @Override
    public boolean isEmpty() {
        for(ItemStack itemStack : this.inventory) {
            if (!itemStack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return this.inventory.get(slot);
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        this.checkRecipe();
        return ContainerHelper.removeItem(this.inventory, slot, amount);
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        this.checkRecipe();
        return ContainerHelper.takeItem(this.inventory, slot);
    }

    public void setItem(int slot, ItemStack stack) {
        ItemStack itemStack = (ItemStack)this.inventory.get(slot);
        boolean bl = !stack.isEmpty() && ItemStack.isSameItemSameComponents(itemStack, stack);
        this.inventory.set(slot, stack);
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
        this.checkRecipe();
        this.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void fillStackedContents(StackedContents finder) {
        for(ItemStack itemStack : this.inventory) {
            finder.accountStack(itemStack);
        }
    }

    private RecipeHolder<?> recipeUsed;

    @Override
    public void setRecipeUsed(@Nullable RecipeHolder<?> recipeHolder) {
        this.recipeUsed = recipeHolder;
    }

    @Override
    public @Nullable RecipeHolder<?> getRecipeUsed() {
        return this.recipeUsed;
    }

    public void clear() {
        this.inventory.clear();
    }

    public void checkRecipe() {
        Player playerEntity = null;
        Level world = this.level;
        if (world != null && this.lastUser != null) {
            playerEntity = world.getPlayerByUUID(this.lastUser);
        }
        if (this.nowRecipe != null) {
            if (this.nowRecipe.canCraft(playerEntity) && this.nowRecipe.matches(this.craftInput(), world)) {
                return;
            }
        }
        Optional<? extends AlterRecipe> alterRecipe = this.matchGetter.getRecipeFor(this, world);
        if (alterRecipe.isPresent() && alterRecipe.get().canCraft(playerEntity)) {
            this.nowRecipe = alterRecipe.get();
            this.totalProgress = this.nowRecipe.recipeTime();
        } else {
            this.nowRecipe = null;
            this.nowRecipeHolder = null;
        }
        this.progress = 0;
    }

    private boolean canCraftRecipe(RegistryAccess registryManager) {
        if (this.nowRecipe == null) {
            return false;
        }
        Player playerEntity = null;
        Level world = this.level;
        if (world != null && this.lastUser != null) {
            playerEntity = world.getPlayerByUUID(this.lastUser);
        }
        if (!nowRecipe.canCraft(playerEntity)) {
            return false;
        }
        if (!nowRecipe.matches(this.craftInput(), world) && !nowRecipe.InputsCountEnough(this)) {
            return false;
        }
        ItemStack output = this.nowRecipe.getResultItem(registryManager);
        if (output.isEmpty() || this.inventory.get(10).isEmpty()) {
            return true;
        }
        ItemStack outputSlot = this.inventory.get(10);
        if (!ItemStack.isSameItemSameComponents(output, outputSlot)) {
            return false;
        }
        if (outputSlot.getCount() + output.getCount() <= outputSlot.getMaxStackSize()) {
            return true;
        }
        return outputSlot.getCount() + output.getCount() <= this.getMaxStackSize();
    }

    private boolean craftRecipe(RegistryAccess registryManager) {
        if (canCraftRecipe(registryManager)) {
            ItemStack output = this.nowRecipe.getResultItem(registryManager);
            ItemStack outputSlot = this.inventory.get(10);
            if (outputSlot.isEmpty()) {
                this.inventory.set(10, output.copy());
            } else if (ItemStack.isSameItemSameComponents(output, outputSlot)) {
                outputSlot.grow(output.getCount());
            } else {
                return false;
            }
            List<ItemStack> extraOutput = this.nowRecipe.getExtraOutput(this);
            if (extraOutput != null) {
                Level world = this.level;
                BlockPos pos = this.getBlockPos().above();
                if (world != null) {
                    for (ItemStack extra : extraOutput) {
                        world.addFreshEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), extra));
                    }
                }
            }
            this.nowRecipe.consumeInputs(this);
            return true;
        } else {
            return false;
        }
    }

    public void tick(Level world, BlockPos pos, BlockState state, AlterBlockEntity blockEntity) {
        if (needCheckRecipe) {
            this.checkRecipe();
            needCheckRecipe = false;
        }
        boolean itemChanged = false;
        boolean hasRecipe = this.nowRecipe != null;
        boolean hasFuel = this.fuelTime > 0;
        if (hasRecipe && !hasFuel) {
            ItemStack fuel = this.inventory.get(9);
            if (!fuel.isEmpty()) {
                int fuelRealTime = getFuelTime(fuel);
                if (fuelRealTime > 0) {
                    this.fuelTime = fuelRealTime;
                    this.totalFuelTime = fuelRealTime;
                    fuel.shrink(1);
                    itemChanged = true;
                }
            }
        }
        hasFuel = this.fuelTime > 0;
        if (hasRecipe && hasFuel) {
            this.progress++;
            this.fuelTime--;
        } else {
            if (hasRecipe && this.progress > 0) {
                this.progress --;
            } else {
                this.progress = 0;
            }
            if (hasFuel) {
                this.fuelTime--;
            }
        }
        if (hasRecipe && this.progress >= this.nowRecipe.recipeTime()) {
            if (craftRecipe(world.registryAccess())) {
                blockEntity.setRecipeUsed(this.nowRecipeHolder);
            }
            this.progress = 0;
            itemChanged = true;
        }
        if (itemChanged) {
            this.setChanged();
        }
    }

    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        ContainerHelper.loadAllItems(nbt, this.inventory, provider);
        if (nbt.contains("LastUser")) {
            this.lastUser = nbt.getUUID("LastUser");
        } else {
            this.lastUser = null;
        }
        this.fuelTime = nbt.getInt("FuelTime");
        this.progress = nbt.getInt("Process");
        this.totalProgress = nbt.getInt("TotalProcess");
        this.totalFuelTime = nbt.getInt("TotalFuelTime");
    }

    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider provider) {
        super.saveAdditional(nbt, provider);
        ContainerHelper.saveAllItems(nbt, this.inventory, provider);
        if (this.lastUser != null) {
            nbt.putUUID("LastUser", this.lastUser);
        }
        nbt.putInt("FuelTime", this.fuelTime);
        nbt.putInt("Process", this.progress);
        nbt.putInt("TotalProcess", this.totalProgress);
        nbt.putInt("TotalFuelTime", this.totalFuelTime);
    }
}
