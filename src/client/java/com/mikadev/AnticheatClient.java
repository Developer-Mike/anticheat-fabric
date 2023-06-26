package com.mikadev;

import com.mikadev.packets.ResourcesPacket;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

public class AnticheatClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("AnticheatClient initialized!");

        ClientLoginNetworking.registerGlobalReceiver(Anticheat.USED_RESOURCES_IDENTIFIER,
                (client, handler, buf, responseSender) -> {
                    return client.submit(() -> {
                        return ResourcesPacket.createResourcePacket(getResourcePacks(), getMods());
                    });
                });
    }

    String[] getResourcePacks() {
        return MinecraftClient.getInstance().getResourcePackManager()
                .getEnabledProfiles()
                .stream()
                .map(resourcePack -> resourcePack.getName())
                .toArray(String[]::new);
    }

    String[] getMods() {
        return FabricLoader.getInstance().getAllMods()
                .stream()
                .map(mod -> mod.getMetadata().getId())
                .toArray(String[]::new);
    }
}