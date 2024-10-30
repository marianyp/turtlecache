package dev.mariany.turtlecache.packet.serverbound;

import dev.mariany.turtlecache.TurtleCache;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record InventoryOpenPayload(boolean open) implements CustomPayload {
    public static final CustomPayload.Id<InventoryOpenPayload> ID = new CustomPayload.Id<>(
            TurtleCache.id("inventory_open"));
    public static final PacketCodec<RegistryByteBuf, InventoryOpenPayload> CODEC = PacketCodec.tuple(PacketCodecs.BOOL,
            InventoryOpenPayload::open, InventoryOpenPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
