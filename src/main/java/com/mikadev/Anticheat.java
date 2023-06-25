package com.mikadev;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Anticheat implements ModInitializer {
    public static final String MOD_ID = "anticheat";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final String KEY_USED_MODS = "mods";
    public static final String KEY_USED_RESOURCE_PACKS = "resource_packs";

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Anticheat");
    }
}