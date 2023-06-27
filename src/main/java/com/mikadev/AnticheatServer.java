package com.mikadev;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.mixin.networking.accessor.ServerLoginNetworkHandlerAccessor;
import net.minecraft.server.BannedIpEntry;
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
        allowedMods.addAll(Arrays.asList("anticheat", "fabric-api", "fabric-api-base",
                "fabric-api-lookup-api-v1", "fabric-biome-api-v1", "fabric-block-api-v1", "fabric-blockrenderlayer-v1",
                "fabric-client-tags-api-v1", "fabric-command-api-v1", "fabric-command-api-v2", "fabric-commands-v0",
                "fabric-containers-v0", "fabric-content-registries-v0", "fabric-convention-tags-v1",
                "fabric-crash-report-info-v1", "fabric-data-generation-api-v1", "fabric-dimensions-v1",
                "fabric-entity-events-v1", "fabric-events-interaction-v0", "fabric-events-lifecycle-v0",
                "fabric-game-rule-api-v1", "fabric-item-api-v1", "fabric-item-group-api-v1",
                "fabric-key-binding-api-v1", "fabric-keybindings-v0", "fabric-lifecycle-events-v1",
                "fabric-loot-api-v2", "fabric-loot-tables-v1", "fabric-message-api-v1", "fabric-mining-level-api-v1",
                "fabric-models-v0", "fabric-networking-api-v1", "fabric-networking-v0", "fabric-object-builder-api-v1",
                "fabric-particles-v1", "fabric-recipe-api-v1", "fabric-registry-sync-v0", "fabric-renderer-api-v1",
                "fabric-renderer-indigo", "fabric-renderer-registries-v1", "fabric-rendering-data-attachment-v1",
                "fabric-rendering-fluids-v1", "fabric-rendering-v0", "fabric-rendering-v1",
                "fabric-resource-conditions-api-v1", "fabric-resource-loader-v0", "fabric-screen-api-v1",
                "fabric-screen-handler-api-v1", "fabric-sound-api-v1", "fabric-transfer-api-v1",
                "fabric-transitive-access-wideners-v1", "fabricloader", "java", "minecraft"));

        instantBanResourcePacks = new ArrayList<String>(
                Arrays.asList(CONFIG.getOrDefault(KEY_INSTANT_BAN_RESOURCE_PACKS, "").split(",")));
        instantBanMods = new ArrayList<String>(Arrays.asList(CONFIG.getOrDefault(KEY_INSTANT_BAN_MODS, "").split(",")));
    }

    @Override
    public void onInitializeServer() {
        loadConfig();

        ServerLoginConnectionEvents.QUERY_START.register((netHandler, server, packetSender, sync) -> {
            packetSender.sendPacket(Anticheat.USED_RESOURCES_IDENTIFIER, ResourcesPacket.REQUEST_PACKET);
        });

        ServerLoginNetworking.registerGlobalReceiver(Anticheat.USED_RESOURCES_IDENTIFIER,
                (server, handler, understood, buf, sync, responseSender) -> {
                    sync.waitFor(server.submit(() -> {
                        if (!understood) {
                            Anticheat.LOGGER.info("Client did not understand packet.");
                            handler.disconnect(Text.literal("Please install the Anticheat mod."));
                            return;
                        }

                        try {
                            String[][] response = ResourcesPacket.readResourcePacket(buf);
                            String[] usedResourcePacks = response[0];
                            String ipAddress = ((ServerLoginNetworkHandlerAccessor) handler).getConnection()
                                    .getAddress().toString().split(":")[0].replace("/", "");

                            for (String resourcePack : usedResourcePacks) {
                                if (!allowedResourcePacks.contains(resourcePack)) {
                                    if (instantBanResourcePacks.contains(resourcePack)) {
                                        BannedIpEntry banEntry = new BannedIpEntry(ipAddress, null, null, null,
                                                "You used highly illegal resource packs.");
                                        server.getPlayerManager().getIpBanList().add(banEntry);

                                        Anticheat.LOGGER.info("Instant banned player for using " + resourcePack);
                                        handler.disconnect(
                                                Text.literal("You are using highly illegal resource packs."));
                                        return;
                                    } else {
                                        Anticheat.LOGGER.info("Kicked player for using " + resourcePack + " ("
                                                + String.join(", ", usedResourcePacks) + ")");
                                        handler.disconnect(Text.literal("You are using illegal resource packs."));
                                        return;
                                    }
                                }
                            }

                            String[] usedMods = response[1];

                            for (String mod : usedMods) {
                                if (!allowedMods.contains(mod)) {
                                    if (instantBanMods.contains(mod)) {
                                        BannedIpEntry banEntry = new BannedIpEntry(ipAddress, null, null, null,
                                                "You used highly illegal modifications.");
                                        server.getPlayerManager().getIpBanList().add(banEntry);

                                        Anticheat.LOGGER.info("Instant banned player for using " + mod);
                                        handler.disconnect(Text.literal("You are using highly illegal modifications."));
                                        return;
                                    } else {
                                        Anticheat.LOGGER.info("Kicked player for using " + mod + " ("
                                                + String.join(", ", usedMods) + ")");
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