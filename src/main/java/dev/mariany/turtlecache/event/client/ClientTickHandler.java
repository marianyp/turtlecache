package dev.mariany.turtlecache.event.client;

import dev.mariany.turtlecache.packet.serverbound.InventoryOpenPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;

@Environment(EnvType.CLIENT)
public class ClientTickHandler {
    private static boolean inventoryOpenFlag = false;

    public static void onClientTick(MinecraftClient client) {
        if (client.currentScreen instanceof AbstractInventoryScreen<?>) {
            if (!inventoryOpenFlag) {
                inventoryOpenFlag = true;
                ClientPlayNetworking.send(new InventoryOpenPayload(true));
            }
        } else {
            if (inventoryOpenFlag) {
                inventoryOpenFlag = false;
                ClientPlayNetworking.send(new InventoryOpenPayload(false));
            }
        }
    }
}
