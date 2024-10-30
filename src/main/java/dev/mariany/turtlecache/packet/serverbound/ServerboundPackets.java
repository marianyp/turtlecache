package dev.mariany.turtlecache.packet.serverbound;

import dev.mariany.turtlecache.attachment.ModAttachmentTypes;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ServerboundPackets {
    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(InventoryOpenPayload.ID, (payload, context) -> {
            context.player().setAttached(ModAttachmentTypes.INVENTORY_OPEN, payload.open());
        });
    }
}
