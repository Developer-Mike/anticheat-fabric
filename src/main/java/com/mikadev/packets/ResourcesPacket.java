package com.mikadev.packets;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

public class ResourcesPacket {
    public static final String TYPE_DIVIDER = ";;;";
    public static final String LIST_DIVIDER = ";!;";
    public static final PacketByteBuf REQUEST_PACKET = PacketByteBufs.create()
            .writeString("give_me_that_info_or_get_yeeeted");
    public static final int MAX_PACKET_SIZE = 8192;

    public static PacketByteBuf createResourcePacket(String[] resourcePacks, String[] mods) {
        PacketByteBuf packet = PacketByteBufs.create()
                .writeString(String.join(LIST_DIVIDER, resourcePacks) + TYPE_DIVIDER + String.join(LIST_DIVIDER, mods),
                        MAX_PACKET_SIZE);

        return packet;
    }

    public static String[][] readResourcePacket(PacketByteBuf packet) {
        String[] content = packet.readString(MAX_PACKET_SIZE).split(TYPE_DIVIDER);

        return new String[][] { content[0].split(LIST_DIVIDER), content[1].split(LIST_DIVIDER) };
    }
}
