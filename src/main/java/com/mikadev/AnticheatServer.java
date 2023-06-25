package com.mikadev;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;

import com.mikadev.config.SimpleConfig;
import com.mikadev.packets.ResourcesPacket;

public class AnticheatServer implements DedicatedServerModInitializer {
    public static SimpleConfig CONFIG;

    public static final String KEY_ALLOWED_RESOURCE_PACKS = "allowed_resource_packs";
    public static final String KEY_ALLOWED_MODS = "allowed_mods";
    public static final String KEY_INSTANT_BAN_RESOURCE_PACKS = "instant_ban_resource_packs";
    public static final String KEY_INSTANT_BAN_MODS = "instant_ban_mods";

    public static ArrayList<String> allowedResourcePacks;
    public static ArrayList<String> allowedMods;
    public static ArrayList<String> instantBanResourcePacks;
    public static ArrayList<String> instantBanMods;

    String configProvider(String filename) {
        return KEY_ALLOWED_RESOURCE_PACKS + "=\n" +
                KEY_ALLOWED_MODS + "=\n" +
                KEY_INSTANT_BAN_RESOURCE_PACKS + "=\n" +
                KEY_INSTANT_BAN_MODS + "=\n";
    }

    void loadConfig() {
        CONFIG = SimpleConfig.of(Anticheat.MOD_ID).provider(this::configProvider).request();

        allowedResourcePacks = new ArrayList<String>(
                Arrays.asList(CONFIG.getOrDefault(KEY_ALLOWED_RESOURCE_PACKS, "").split(",")));
        allowedResourcePacks.addAll(Arrays.asList("vanilla", "fabric"));

        allowedMods = new ArrayList<String>(
                Arrays.asList(CONFIG.getOrDefault(KEY_ALLOWED_MODS, "").split(",")));
        allowedMods.addAll(Arrays.asList("fabric-object-builder-api-v1", "java", "fabric-sound-api-v1",
                "fabric-transitive-access-wideners-v1", "fabric-mining-level-api-v1", "fabric-content-registries-v0",
                "fabric-item-group-api-v1", "fabric-renderer-indigo", "fabric-transfer-api-v1", "anticheat",
                "fabric-registry-sync-v0", "fabric-rendering-fluids-v1", "fabric-recipe-api-v1",
                "fabric-gametest-api-v1", "fabric-resource-conditions-api-v1", "fabric-events-interaction-v0",
                "fabric-entity-events-v1", "fabric-dimensions-v1", "fabric-block-api-v1", "fabric-client-tags-api-v1",
                "minecraft", "fabric-api-lookup-api-v1", "fabric-message-api-v1", "fabric-rendering-v1",
                "fabric-loot-api-v2", "fabric-screen-api-v1", "fabric-networking-api-v1",
                "fabric-rendering-data-attachment-v1", "fabric-api", "fabricloader", "fabric-lifecycle-events-v1",
                "fabric-crash-report-info-v1", "fabric-biome-api-v1", "fabric-game-rule-api-v1",
                "fabric-key-binding-api-v1", "fabric-blockrenderlayer-v1", "fabric-api-base", "fabric-command-api-v2",
                "fabric-models-v0", "fabric-screen-handler-api-v1", "fabric-particles-v1", "fabric-resource-loader-v0",
                "fabric-item-api-v1", "fabric-renderer-api-v1", "fabric-data-generation-api-v1",
                "fabric-convention-tags-v1"));

        instantBanResourcePacks = new ArrayList<String>(
                Arrays.asList(CONFIG.getOrDefault(KEY_INSTANT_BAN_RESOURCE_PACKS, "").split(",")));
        instantBanMods = new ArrayList<String>(Arrays.asList(CONFIG.getOrDefault(KEY_INSTANT_BAN_MODS, "").split(",")));
    }

    @Override
    public void onInitializeServer() {
        loadConfig();

        ServerLoginConnectionEvents.QUERY_START.register((netHandler, server, packetSender, sync) -> packetSender
                .sendPacket(Anticheat.USED_RESOURCE_PACKS_IDENTIFIER, ResourcesPacket.REQUEST_PACKET));

        ServerLoginNetworking.registerGlobalReceiver(Anticheat.USED_RESOURCE_PACKS_IDENTIFIER,
                (server, handler, understood, buf, sync, responseSender) -> {
                    sync.waitFor(server.submit(() -> {
                        try {
                            String[] usedResourcePacks = ResourcesPacket.readResourcePacket(buf);

                            for (String resourcePack : usedResourcePacks) {
                                if (!allowedResourcePacks.contains(resourcePack)) {
                                    if (instantBanResourcePacks.contains(resourcePack)) {
                                        // TODO: Ban player

                                        Anticheat.LOGGER.info("Instant banned player for using " + resourcePack);
                                        handler.disconnect(
                                                Text.literal("You are using highly illegal resource packs."));
                                        return;
                                    } else {
                                        Anticheat.LOGGER.info("Kicked player for using " + resourcePack);
                                        handler.disconnect(Text.literal("You are using illegal resource packs."));
                                        return;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            String disconnectMessage = "Couldn't read package: " + e.getMessage();

                            Anticheat.LOGGER.error(disconnectMessage);
                            handler.disconnect(Text.literal(disconnectMessage));
                        }
                    }));
                });

        ServerLoginConnectionEvents.QUERY_START.register((netHandler, server, packetSender, sync) -> packetSender
                .sendPacket(Anticheat.USED_MODS_IDENTIFIER, ResourcesPacket.REQUEST_PACKET));

        ServerLoginNetworking.registerGlobalReceiver(Anticheat.USED_MODS_IDENTIFIER,
                (server, handler, understood, buf, sync, responseSender) -> {
                    sync.waitFor(server.submit(() -> {
                        try {
                            String[] usedMods = ResourcesPacket.readResourcePacket(buf);

                            for (String mod : usedMods) {
                                if (!allowedMods.contains(mod)) {
                                    if (instantBanMods.contains(mod)) {
                                        // TODO: Ban player

                                        Anticheat.LOGGER.info("Instant banned player for using " + mod);
                                        handler.disconnect(Text.literal("You are using highly illegal modifications."));
                                        return;
                                    } else {
                                        Anticheat.LOGGER.info("Kicked player for using " + mod);
                                        handler.disconnect(Text.literal("You are using illegal modifications."));
                                        return;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            String disconnectMessage = "Couldn't read package: " + e.getMessage();

                            Anticheat.LOGGER.error(disconnectMessage);
                            handler.disconnect(Text.literal(disconnectMessage));
                        }
                    }));
                });
    }
}

/*
 * if (!properties.containsKey(Anticheat.KEY_USED_RESOURCE_PACKS)
 * || !properties.containsKey(Anticheat.KEY_USED_MODS)) {
 * networkHandler.disconnect(Text.of("Please install the Anticheat mod."));
 * 
 * info.cancel();
 * return;
 * } else {
 * String[] resourcePacks =
 * properties.get(Anticheat.KEY_USED_RESOURCE_PACKS).stream()
 * .map(property -> property.getValue()).toArray(String[]::new);
 * 
 * for (String resourcePack : resourcePacks) {
 * if (!AnticheatServer.allowedResourcePacks.contains(resourcePack)) {
 * Anticheat.LOGGER.info(String.format("%s is using illegal resource packs (%s)"
 * ,
 * networkHandlerAccessor.getProfile().getName(), String.join(", ",
 * resourcePacks)));
 * 
 * if (AnticheatServer.instantBanResourcePacks.contains(resourcePack)) {
 * networkHandlerAccessor.getServer().getPlayerManager().getUserBanList().add(
 * new BannedPlayerEntry(networkHandlerAccessor.getProfile(), null, null, null,
 * "You are using highly illegal resource packs."));
 * } else {
 * networkHandler.disconnect(Text.of("You are using illegal resourcepacks."));
 * }
 * 
 * info.cancel();
 * return;
 * }
 * }
 * 
 * String[] mods = properties.get(Anticheat.KEY_USED_MODS).stream()
 * .map(property -> property.getValue()).toArray(String[]::new);
 * 
 * for (String mod : mods) {
 * if (!AnticheatServer.allowedMods.contains(mod)) {
 * Anticheat.LOGGER.info(String.format("%s is using illegal mods (%s)",
 * networkHandlerAccessor.getProfile().getName(), String.join(", ", mods)));
 * 
 * if (AnticheatServer.instantBanMods.contains(mod)) {
 * networkHandlerAccessor.getServer().getPlayerManager().getUserBanList().add(
 * new BannedPlayerEntry(networkHandlerAccessor.getProfile(), null, null, null,
 * "You are using highly illegal modifications."));
 * } else {
 * networkHandler.disconnect(Text.of("You are using illegal modifications."));
 * }
 * 
 * info.cancel();
 * return;
 * }
 * }
 * }
 */