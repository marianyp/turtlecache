package dev.mariany.turtlecache;

import dev.mariany.turtlecache.item.ModItems;
import dev.mariany.turtlecache.item.component.ModComponents;
import dev.mariany.turtlecache.packet.Packets;
import dev.mariany.turtlecache.packet.serverbound.ServerboundPackets;
import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TurtleCache implements ModInitializer {
    public static final String MOD_ID = "turtlecache";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModComponents.registerModComponents();
        ModItems.registerModItems();

        Packets.register();
        ServerboundPackets.init();
    }

    public static Identifier id(String resource) {
        return Identifier.of(MOD_ID, resource);
    }
}