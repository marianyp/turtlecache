package dev.mariany.turtlecache.attachment;

import com.mojang.serialization.Codec;
import dev.mariany.turtlecache.TurtleCache;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public class ModAttachmentTypes {
    public static final AttachmentType<Boolean> INVENTORY_OPEN = AttachmentRegistry.<Boolean>builder()
            .persistent(Codec.BOOL).initializer(() -> false).buildAndRegister(TurtleCache.id("inventory_open"));
}
