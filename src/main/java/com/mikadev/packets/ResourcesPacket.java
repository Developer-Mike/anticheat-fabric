package com.mikadev.packets;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

public class ResourcesPacket {
    public static final String DIVIDER = ";!;";
    public static final PacketByteBuf REQUEST_PACKET = PacketByteBufs.create()
            .writeString("give_me_that_info_or_get_yeeeted");

    public static PacketByteBuf createResourcePacket(String[] resourceList) {
        PacketByteBuf packet = PacketByteBufs.create()
                .writeString(String.join(DIVIDER, resourceList));

        return packet;
    }

    public static String[] readResourcePacket(PacketByteBuf packet) {
        String[] resourceList = packet.readString().split(DIVIDER);

        return resourceList;
    }
}
