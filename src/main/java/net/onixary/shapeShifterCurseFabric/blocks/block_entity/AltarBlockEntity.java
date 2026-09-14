package net.onixary.shapeShifterCurseFabric.blocks.block_entity;

import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.onixary.shapeShifterCurseFabric.blocks.RegCustomBlock;
import net.onixary.shapeShifterCurseFabric.custom_ui.AltarCraftUIHandler;
import net.onixary.shapeShifterCurseFabric.custom_ui.RegMenuType;
import net.onixary.shapeShifterCurseFabric.items.RegCustomItem;
import net.onixary.shapeShifterCurseFabric.recipes.RecipeUtils;
import net.onixary.shapeShifterCurseFabric.recipes.altar.AltarRecipe;
import net.onixary.shapeShifterCurseFabric.recipes.altar.AltarRecipeInput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class AltarBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, RecipeCraftingHolder, StackedContentsCompatible {
    // 进度锁是个不错的设计 能降低难度(毕竟之前做限制进度使用得上对应阶段的材料 有些材料是真不好量产 有这个就能用便宜材料了)
    public UUID lastUser;
    public AltarRecipe nowRecipe;
    public RecipeHolder<?> nowRecipeHolder;
    public static final int maxFuel = 102400;
    // data slot 网络用 16-bit(short) 传输，值域 [-32768,32767]；而 fuelTime 可累积到 102400 超上限，
    // 超过 32767 会被 writeShort 截断成负值 → 客户端燃料条"消失-重涨"。
    // 按原作者建议：用 2 个 short 无损拆分传输 fuelTime —— slot 2=低16位, slot 3=高16位，
    // 客户端 getNowFuel() 拼回完整 int。getCount()/size() 相应从 3 增到 4。
    public int progress = 0;
    public int totalProgress = 0;  // Only Client
    public int fuelTime = 0;
    // public int totalFuelTime = 0;  // Only Client
    public final NonNullList<ItemStack> inventory;

    public boolean needCheckRecipe = true;
    public final ContainerData propertyDelegate;

    public static final int[] TOP = {0, 1, 2, 3, 4, 5, 6, 7, 8};
    public static final int[] SIDE = {9};
    public static final int[] BOTTOM = {10};

    public static final HashMap<Item, Integer> fuelTimeMap = new HashMap<>();

    private final RecipeManager.CachedCheck<RecipeInput, AltarRecipe> matchGetter;

    static {
        fuelTimeMap.put(RegCustomItem.UNTREATED_MOONDUST, 800);
    }

    public static boolean canFuel(ItemStack stack) {
        return fuelTimeMap.containsKey(stack.getItem());
    }

    public static int getFuelTime(ItemStack stack) {
        return fuelTimeMap.getOrDefault(stack.getItem(), 0);
    }

    public AltarBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(RegCustomBlock.Altar_BLOCK_ENTITY, blockPos, blockState);
        this.inventory = NonNullList.withSize(11, ItemStack.EMPTY);
        this.matchGetter = RecipeManager.createCheck(RecipeUtils.Altar_RECIPE);
        this.propertyDelegate = new ContainerData() {
            public int get(int index) {
                switch (index) {
                    case 0 -> {
                        return AltarBlockEntity.this.progress;
                    }
                    case 1 -> {
                        return AltarBlockEntity.this.totalProgress;
                    }
                    case 2 -> {
                        return AltarBlockEntity.this.fuelTime & 0xFFFF;
                    }
                    case 3 -> {
                        return (AltarBlockEntity.this.fuelTime >>> 16) & 0xFFFF;
                    }
                    default -> {
                        return 0;
                    }
                }
            }

            public void set(int index, int value) {
                switch (index) {
                    case 0 -> AltarBlockEntity.this.progress = value;
                    case 1 -> AltarBlockEntity.this.totalProgress = value;
                    case 2 -> AltarBlockEntity.this.fuelTime = (AltarBlockEntity.this.fuelTime & 0xFFFF0000) | (value & 0xFFFF);
                    case 3 -> AltarBlockEntity.this.fuelTime = (AltarBlockEntity.this.fuelTime & 0x0000FFFF) | ((value & 0xFFFF) << 16);
                }

            }

            public int size() {
                return 4;
            }

            public int getCount() {
                return 4;
            }
        };
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.shape-shifter-curse.altar");
    }

    @Override
    protected AbstractContainerMenu createMenu(int syncId, Inventory playerInventory) {
        return new AltarCraftUIHandler(RegMenuType.AltarCraftUI, syncId, playerInventory, this, ContainerLevelAccess.NULL, this.propertyDelegate);
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

    @Override
    protected @NotNull NonNullList<ItemStack> getItems() {
        return this.inventory;
    }

    @Override
    protected void setItems(@NotNull NonNullList<ItemStack> items) {
        for (int i = 0; i < items.size() && i < this.inventory.size(); i++) {
            this.inventory.set(i, items.get(i).copy());
        }
        this.setChanged();
    }

    // 构造包含燃料/催化剂槽(slot 9)的 RecipeInput 给 Recipe 匹配。
    // 1.21.1 的 CraftingInput.of 会按非空网格裁剪、丢弃 slot 9，使 matches().getItem(9) 越界；
    // 故改用自定义 AltarRecipeInput 线性映射 inventory 0-9（0-8 键材 + slot 9 燃料/催化剂）。
    // AltarBlockEntity 自身不 implements RecipeInput，避免与 WorldlyContainer 的 getItem/isEmpty 双接口在 remap 时二义。
    public RecipeInput craftInput() {
        return new AltarRecipeInput(this.inventory);
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
    public void fillStackedContents(StackedItemContents finder) {
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
        Level world = this.getLevel();
        if (this.nowRecipe != null) {
            if (world != null && this.canCraftRecipe(world.registryAccess())) {
                return;
            }
            this.nowRecipe = null;
            this.nowRecipeHolder = null;
            this.totalProgress = 0;
        }
        if (!(world instanceof ServerLevel serverLevel)) {
            this.nowRecipe = null;
            this.totalProgress = 0;
            this.progress = 0;
            return;
        }
        // 必须传 craftInput()（含 slot 9 燃料/催化剂槽的线性 RecipeInput），不能把 BlockEntity 本身当 RecipeInput；
        // 且 getRecipeFor 返回的是 RecipeHolder，配方本体要 .value()
        var altarRecipe = this.matchGetter.getRecipeFor(this.craftInput(), serverLevel);
        if (altarRecipe.isPresent()) {
            this.nowRecipe = altarRecipe.get().value();
            this.nowRecipeHolder = altarRecipe.get();
            this.totalProgress = this.nowRecipe.recipeTime();
            if (!this.canCraftRecipe(world.registryAccess())) {
                this.nowRecipe = null;
                this.nowRecipeHolder = null;
                this.totalProgress = 0;
            }
        } else {
            this.nowRecipe = null;
            this.nowRecipeHolder = null;
            this.totalProgress = 0;
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
        if (!nowRecipe.matches(this.craftInput(), world) || !nowRecipe.InputsCountEnough(this)) {
            return false;
        }
        ItemStack output = this.nowRecipe.assemble(this.craftInput());
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
            ItemStack output = this.nowRecipe.assemble(this.craftInput());
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

    public void tick(Level world, BlockPos pos, BlockState state, AltarBlockEntity blockEntity) {
        if (needCheckRecipe) {
            this.checkRecipe();
            needCheckRecipe = false;
        }
        boolean itemChanged = false;
        ItemStack fuel = this.inventory.get(9);
        if (!fuel.isEmpty()) {
            int fuelRealTime = getFuelTime(fuel);
            if (fuelRealTime > 0 && this.fuelTime + fuelRealTime <= maxFuel) {
                this.fuelTime += fuelRealTime;
                fuel.shrink(1);
                itemChanged = true;
            }
        }
        if (this.nowRecipe != null) {
            int fuelCost = nowRecipe.fuelUsage();
            if (this.fuelTime >= fuelCost) {
                this.fuelTime -= fuelCost;
                this.progress++;
            } else {
                if (this.progress > 0) {
                    this.progress--;
                } else {
                    this.progress = 0;
                }
            }

            if (this.progress >= this.nowRecipe.recipeTime()) {
                if (craftRecipe(world.registryAccess())) {
                    blockEntity.setRecipeUsed(this.nowRecipeHolder);
                }
                this.progress = 0;
                itemChanged = true;
            }
        }

        if (itemChanged) {
            this.checkRecipe();
            this.setChanged();
        }
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput valueInput) {
        super.loadAdditional(valueInput);
        ContainerHelper.loadAllItems(valueInput, this.inventory);
        this.lastUser = valueInput.read("LastUser", UUIDUtil.CODEC).orElse(null);
        this.fuelTime = valueInput.getIntOr("FuelTime", 0);
        this.progress = valueInput.getIntOr("Process", 0);
        this.totalProgress = valueInput.getIntOr("TotalProcess", 0);
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        ContainerHelper.saveAllItems(valueOutput, this.inventory);
        valueOutput.storeNullable("LastUser", UUIDUtil.CODEC, this.lastUser);
        valueOutput.putInt("FuelTime", this.fuelTime);
        valueOutput.putInt("Process", this.progress);
        valueOutput.putInt("TotalProcess", this.totalProgress);
    }
}
