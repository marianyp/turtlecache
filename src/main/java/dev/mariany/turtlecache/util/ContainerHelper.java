package dev.mariany.turtlecache.util;

import dev.mariany.turtlecache.attachment.ModAttachmentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.PlayerScreenHandler;

public final class ContainerHelper {
    public static boolean isContainerOpen(PlayerEntity player) {
        if (!(player.currentScreenHandler instanceof PlayerScreenHandler)) {
            return true;
        }
        return player.getAttachedOrElse(ModAttachmentTypes.INVENTORY_OPEN, false);
    }
}
