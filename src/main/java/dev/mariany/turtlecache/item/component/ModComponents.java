package dev.mariany.turtlecache.item.component;

import com.mojang.serialization.Codec;
import dev.mariany.turtlecache.TurtleCache;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModComponents {
    public static final ComponentType<ContainerComponent> ITEM_CACHE = register("item_cache",
            ComponentType.<ContainerComponent>builder().codec(ContainerComponent.CODEC)
                    .packetCodec(ContainerComponent.PACKET_CODEC).cache());
    public static final ComponentType<Integer> MAX_CACHE_COUNT = register("max_cache_count",
            ComponentType.<Integer>builder().codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT).cache());
    public static final ComponentType<Integer> CACHE_COUNT = register("cache_count",
            ComponentType.<Integer>builder().codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT).cache());
    public static final ComponentType<Boolean> SUPPLY_ENABLE = register("supply_enable",
            ComponentType.<Boolean>builder().codec(Codec.BOOL).packetCodec(PacketCodecs.BOOL).cache());

    private static <T> ComponentType<T> register(String name, ComponentType.Builder<T> builder) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, TurtleCache.id(name), builder.build());
    }

    public static void registerModComponents() {
        TurtleCache.LOGGER.info("Registering Mod Components for " + TurtleCache.MOD_ID);
    }
}
