package dev.mariany.turtlecache.item.custom;

import dev.mariany.turtlecache.item.component.ModComponents;
import dev.mariany.turtlecache.mixin.PlayerInventoryAccessor;
import dev.mariany.turtlecache.util.ContainerHelper;
import dev.mariany.turtlecache.util.ModConstants;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Colors;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

public class CacheItem extends Item {
    private static final int BAR_COLOR = MathHelper.packRgb(0.4F, 0.4F, 1.0F);


    @Override
    public String getTranslationKey(ItemStack cacheStack) {
        String original = super.getTranslationKey();
        if (isSupplyEnabled(cacheStack)) {
            return original + ".supplying";
        }
        return original;
    }

    public CacheItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean onStackClicked(ItemStack cacheStack, Slot slot, ClickType clickType, PlayerEntity player) {
        if (cacheStack.getCount() != 1 || clickType != ClickType.RIGHT) {
            return false;
        }

        ItemStack slotStack = slot.getStack();

        if (slotStack.isEmpty()) {
            removeOne(cacheStack).ifPresent((extracted) -> {
                slot.insertStack(extracted);
                this.playRemoveSound(player);
            });
        } else if (canAddItem(cacheStack, slotStack)) {
            int maxCacheCount = getMaxCacheCount(cacheStack);
            int cachedAmount = Math.min(slotStack.getCount(), maxCacheCount - getStoredItemCount(cacheStack));
            int added = add(cacheStack, slot.takeStackRange(slotStack.getCount(), cachedAmount, player));
            if (added > 0) {
                this.playInsertSound(player);
            }
        }

        return true;
    }

    @Override
    public boolean onClicked(ItemStack cacheStack, ItemStack otherStack, Slot slot, ClickType clickType,
                             PlayerEntity player, StackReference cursorStackReference) {
        if (cacheStack.getCount() == 1 && clickType == ClickType.RIGHT && slot.canTakeItems(player)) {
            if (otherStack.isEmpty()) {
                removeOne(cacheStack).ifPresent(itemStack -> {
                    this.playRemoveSound(player);
                    cursorStackReference.set(itemStack);
                });
            } else {
                int i = add(cacheStack, otherStack);
                if (i > 0) {
                    this.playInsertSound(player);
                    otherStack.decrement(i);
                }
            }

            return true;
        }

        return false;
    }

    public static boolean isSupplyEnabled(ItemStack cacheStack) {
        return cacheStack.getOrDefault(ModComponents.SUPPLY_ENABLE, false);
    }

    public static boolean toggleSupplyEnabled(ItemStack cacheStack) {
        return Boolean.TRUE.equals(cacheStack.set(ModComponents.SUPPLY_ENABLE, !isSupplyEnabled(cacheStack)));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack cacheStack = user.getStackInHand(hand);

        if (user.isSneaking()) {
            if (toggleSupplyEnabled(cacheStack)) {
                playRemoveSound(user);
            } else {
                playInsertSound(user);
            }
            return TypedActionResult.pass(cacheStack);
        }

        if (dropContents(cacheStack, user)) {
            this.playDropContentsSound(user);
            if (user instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.incrementStat(Stats.USED.getOrCreateStat(this));
            }
            return TypedActionResult.success(cacheStack, world.isClient);
        }

        return TypedActionResult.fail(cacheStack);
    }

    @Override
    public boolean isItemBarVisible(ItemStack cacheStack) {
        return getStoredItemCount(cacheStack) > 0;
    }

    @Override
    public int getItemBarStep(ItemStack cacheStack) {
        return Math.min(12 * getStoredItemCount(cacheStack) / ModConstants.MAX_CACHE_COUNT, 13);
    }

    @Override
    public int getItemBarColor(ItemStack cacheStack) {
        return BAR_COLOR;
    }

    public static int getMaxCacheCount(ItemStack cacheStack) {
        return cacheStack.getOrDefault(ModComponents.MAX_CACHE_COUNT, ModConstants.MAX_CACHE_COUNT);
    }

    private static int getStoredItemCount(ItemStack cacheStack) {
        return cacheStack.getOrDefault(ModComponents.CACHE_COUNT, 0);
    }

    public static void setStoredItemCount(ItemStack cacheStack, int cacheCount) {
        cacheStack.set(ModComponents.CACHE_COUNT, cacheCount);
    }

    public static ItemStack getStoredItemStack(ItemStack cacheStack) {
        ContainerComponent containerComponent = cacheStack.getOrDefault(ModComponents.ITEM_CACHE,
                ContainerComponent.DEFAULT);
        return containerComponent.copyFirstStack().copyWithCount(1);
    }

    public static void setCachedItem(ItemStack cacheStack, ItemStack itemToCache) {
        cacheStack.set(ModComponents.ITEM_CACHE, ContainerComponent.fromStacks(List.of(itemToCache.copyWithCount(1))));
    }

    public static void clearCachedItem(ItemStack cacheStack) {
        setCachedItem(cacheStack, ItemStack.EMPTY);
        setStoredItemCount(cacheStack, 0);
    }

    private static int add(ItemStack cacheStack, ItemStack itemStack) {
        if (itemStack.isEmpty() || !canAddItem(cacheStack, itemStack)) {
            return 0;
        }

        ItemStack itemToCache = getStoredItemStack(cacheStack);

        int currentCount = getStoredItemCount(cacheStack);
        int toAdd = Math.min(itemStack.getCount(), getMaxCacheCount(cacheStack) - currentCount);

        if (toAdd == 0) {
            return 0;
        }

        if (itemToCache.isEmpty()) {
            itemToCache = itemStack.copy();
        }

        setStoredItemCount(cacheStack, currentCount + toAdd);
        setCachedItem(cacheStack, itemToCache);

        return toAdd;
    }

    private static boolean canAddItem(ItemStack cacheStack, ItemStack otherStack) {
        ItemStack storedItemStack = getStoredItemStack(cacheStack);
        if (otherStack.getItem().canBeNested()) {
            return storedItemStack.isEmpty() || ItemStack.areItemsAndComponentsEqual(storedItemStack, otherStack);
        }
        return false;
    }

    private static Optional<ItemStack> removeOne(ItemStack cacheStack) {
        ItemStack storedItemStack = getStoredItemStack(cacheStack);

        if (storedItemStack.isEmpty()) {
            return Optional.empty();
        }

        int storedItemCount = getStoredItemCount(cacheStack);
        int toRemove = Math.min(storedItemStack.getMaxCount(), storedItemCount);

        storedItemStack.setCount(toRemove);


        if (storedItemCount - toRemove <= 0) {
            clearCachedItem(cacheStack);
        } else {
            setStoredItemCount(cacheStack, storedItemCount - toRemove);
        }

        return Optional.of(storedItemStack);
    }

    private static boolean dropContents(ItemStack cacheStack, PlayerEntity player) {
        int storedItemCount = getStoredItemCount(cacheStack);
        ItemStack storedItemStack = getStoredItemStack(cacheStack);

        if (storedItemCount > 0) {
            while (storedItemCount > 0) {
                int toDrop = Math.min(storedItemStack.getMaxCount(), storedItemCount);
                ItemStack dropStack = storedItemStack.copyWithCount(toDrop);
                player.dropItem(dropStack, true);
                storedItemCount -= toDrop;
            }

            clearCachedItem(cacheStack);
            return true;
        }

        return false;
    }

    @Override
    public void inventoryTick(ItemStack cacheStack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(cacheStack, world, entity, slot, selected);

        if (!isSupplyEnabled(cacheStack)) {
            return;
        }

        if (world.isClient) {
            return;
        }

        if (!(entity instanceof PlayerEntity player)) {
            return;
        }

        if (ContainerHelper.isContainerOpen(player)) {
            return;
        }

        ItemStack storedItemStack = getStoredItemStack(cacheStack);

        if (storedItemStack.isEmpty()) {
            return;
        }

        int currentCount = getStoredItemCount(cacheStack);
        if (currentCount > 0) {
            int maxCount = storedItemStack.getMaxCount();
            int totalItemCount = getTotalItemCount(player, storedItemStack);
            if (totalItemCount < maxCount) {
                int toAdd = maxCount - totalItemCount;
                if (currentCount < toAdd) {
                    toAdd = currentCount;
                }
                int newCount = currentCount - toAdd;
                ItemStack itemsToAdd = storedItemStack.copyWithCount(toAdd);
                if (player.getInventory().insertStack(itemsToAdd)) {
                    setStoredItemCount(cacheStack, newCount);
                    playClientBoundRemoveOneSound(player);
                }
                if (newCount <= 0) {
                    clearCachedItem(cacheStack);
                }
            }
        }
    }

    private int getTotalItemCount(PlayerEntity player, ItemStack itemStack) {
        int total = 0;
        List<DefaultedList<ItemStack>> combinedInventory = ((PlayerInventoryAccessor) player.getInventory()).turtlecache$combinedInventory();
        for (DefaultedList<ItemStack> inventory : combinedInventory) {
            for (ItemStack inventoryStack : inventory) {
                if (ItemStack.areItemsAndComponentsEqual(inventoryStack, itemStack)) {
                    total += inventoryStack.getCount();
                }
            }
        }
        ItemStack cursorStack = player.currentScreenHandler.getCursorStack();
        if (ItemStack.areItemsAndComponentsEqual(cursorStack, itemStack)) {
            total += cursorStack.getCount();
        }
        return total;
    }

    @Override
    public void appendTooltip(ItemStack cacheStack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(cacheStack, context, tooltip, type);

        int maxCacheCount = getMaxCacheCount(cacheStack);
        ItemStack storedItemStack = getStoredItemStack(cacheStack);
        Text contents = storedItemStack.toHoverableText();

        if (!storedItemStack.isEmpty()) {
            tooltip.add(Text.translatable("item.turtlecache.turtle_cache.fullness", getStoredItemCount(cacheStack),
                    maxCacheCount, contents.copy().withColor(Colors.GRAY)).withColor(Colors.GRAY));
        }
    }

    private void playClientBoundRemoveOneSound(Entity entity) {
        if (entity instanceof ServerPlayerEntity serverPlayer) {
            ServerWorld world = serverPlayer.getServerWorld();
            serverPlayer.networkHandler.sendPacket(
                    new PlaySoundS2CPacket(SoundEvents.ITEM_ARMOR_EQUIP_TURTLE, SoundCategory.NEUTRAL,
                            serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), 0.5F,
                            0.7F + world.getRandom().nextFloat() * 0.4F, world.getRandom().nextLong()));
        }
    }

    private void playRemoveSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_ARMOR_EQUIP_TURTLE.value(), 0.5F,
                0.7F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }

    private void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_ARMOR_EQUIP_TURTLE.value(), 0.5F,
                1.0F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }

    private void playDropContentsSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_ARMOR_EQUIP_TURTLE.value(), 0.5F,
                0.5F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }
}
