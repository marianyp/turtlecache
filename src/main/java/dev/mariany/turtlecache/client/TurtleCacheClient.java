package dev.mariany.turtlecache.client;

import dev.mariany.turtlecache.TurtleCache;
import dev.mariany.turtlecache.event.client.ClientTickHandler;
import dev.mariany.turtlecache.item.custom.CacheItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class TurtleCacheClient implements ClientModInitializer {
    private static final Identifier SUPPLYING = TurtleCache.id("supplying");

    @Override
    public void onInitializeClient() {
        registerModelPredicateProviders();
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickHandler::onClientTick);
    }

    public static void registerModelPredicateProviders() {
        ModelPredicateProviderRegistry.register(SUPPLYING,
                (itemStack, clientWorld, livingEntity, seed) -> CacheItem.isSupplyEnabled(itemStack) ? 1 : 0);
    }
}
