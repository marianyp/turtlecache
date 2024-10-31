package dev.mariany.turtlecache.item;

import dev.mariany.turtlecache.TurtleCache;
import dev.mariany.turtlecache.item.component.ModComponents;
import dev.mariany.turtlecache.item.custom.CacheItem;
import dev.mariany.turtlecache.util.ModConstants;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModItems {
    public static final Item TURTLE_CACHE = registerItem("turtle_cache", new CacheItem(
            new Item.Settings().component(ModComponents.MAX_CACHE_COUNT, ModConstants.MAX_CACHE_COUNT)
                    .component(ModComponents.CACHE_COUNT, 0)
                    .component(ModComponents.ITEM_CACHE, ContainerComponent.DEFAULT)
                    .component(ModComponents.SUPPLY_ENABLE, false).maxCount(1)));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, TurtleCache.id(name), item);
    }

    public static void registerModItems() {
        TurtleCache.LOGGER.info("Registering Mod Items for " + TurtleCache.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.addAfter(Items.LEAD, TURTLE_CACHE);
        });
    }
}
