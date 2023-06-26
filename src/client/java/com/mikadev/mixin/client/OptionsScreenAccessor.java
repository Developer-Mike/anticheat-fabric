package com.mikadev.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

@Mixin(Screen.class)
public interface OptionsScreenAccessor {
    @Accessor("client")
    public MinecraftClient getClient();
}
