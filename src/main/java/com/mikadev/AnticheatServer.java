package com.mikadev;

import net.fabricmc.api.DedicatedServerModInitializer;

import java.util.ArrayList;
import java.util.Arrays;

import com.mikadev.config.SimpleConfig;

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

    @Override
    public void onInitializeServer() {
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
}