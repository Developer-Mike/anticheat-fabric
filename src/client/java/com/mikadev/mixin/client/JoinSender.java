package com.mikadev.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mikadev.Anticheat;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Session;

@Mixin(Session.class)
public class JoinSender {
    @Inject(at = @At("RETURN"), method = "getProfile", cancellable = true)
    private void onServerJoin(CallbackInfoReturnable<GameProfile> cir) {
        GameProfile profile = cir.getReturnValue();

        Iterable<Property> usedResourcePacks = MinecraftClient.getInstance().getResourcePackManager()
                .getEnabledProfiles()
                .stream()
                .map(resourcePack -> new Property(null, resourcePack.getName(), null))
                .toList();
        profile.getProperties().putAll(Anticheat.KEY_USED_RESOURCE_PACKS, usedResourcePacks);

        Iterable<Property> usedMods = FabricLoader.getInstance().getAllMods()
                .stream()
                .map(mod -> new Property(null, mod.getMetadata().getId(), null))
                .toList();
        profile.getProperties().putAll(Anticheat.KEY_USED_MODS, usedMods);

        cir.setReturnValue(profile);
    }
}