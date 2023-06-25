package com.mikadev;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Anticheat implements ModInitializer {
    public static final String MOD_ID = "anticheat";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier USED_RESOURCE_PACKS_IDENTIFIER = new Identifier(MOD_ID, "used_resource_packs");
    public static final Identifier USED_MODS_IDENTIFIER = new Identifier(MOD_ID, "used_mods");

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Anticheat");
    }
}