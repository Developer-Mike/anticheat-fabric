package com.mikadev.mixin;

import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.Text;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mikadev.Anticheat;
import com.mikadev.AnticheatServer;
import com.mojang.authlib.properties.PropertyMap;

@Mixin(ServerLoginNetworkHandler.class)
public class PlayerConnectMixin {
    @Inject(at = @At("HEAD"), method = "acceptPlayer", cancellable = true)
    private void onConnect(CallbackInfo info) {
        ServerLoginNetworkHandler networkHandler = (ServerLoginNetworkHandler) ((Object) this);
        ServerLoginNetworkHandlerAccessor networkHandlerAccessor = (ServerLoginNetworkHandlerAccessor) networkHandler;
        PropertyMap properties = networkHandlerAccessor.getProfile().getProperties();

        if (!properties.containsKey(Anticheat.KEY_USED_RESOURCE_PACKS)
                || !properties.containsKey(Anticheat.KEY_USED_MODS)) {
            networkHandler.disconnect(Text.of("Please install the Anticheat mod."));

            info.cancel();
            return;
        } else {
            String[] resourcePacks = properties.get(Anticheat.KEY_USED_RESOURCE_PACKS).stream()
                    .map(property -> property.getValue()).toArray(String[]::new);

            for (String resourcePack : resourcePacks) {
                if (!AnticheatServer.allowedResourcePacks.contains(resourcePack)) {
                    Anticheat.LOGGER.info(String.format("%s is using illegal resource packs (%s)",
                            networkHandlerAccessor.getProfile().getName(), String.join(", ", resourcePacks)));

                    if (AnticheatServer.instantBanResourcePacks.contains(resourcePack)) {
                        networkHandlerAccessor.getServer().getPlayerManager().getUserBanList().add(
                                new BannedPlayerEntry(networkHandlerAccessor.getProfile(), null, null, null,
                                        "You are using highly illegal resource packs."));
                    } else {
                        networkHandler.disconnect(Text.of("You are using illegal resourcepacks."));
                    }

                    info.cancel();
                    return;
                }
            }

            String[] mods = properties.get(Anticheat.KEY_USED_MODS).stream()
                    .map(property -> property.getValue()).toArray(String[]::new);

            for (String mod : mods) {
                if (!AnticheatServer.allowedMods.contains(mod)) {
                    Anticheat.LOGGER.info(String.format("%s is using illegal mods (%s)",
                            networkHandlerAccessor.getProfile().getName(), String.join(", ", mods)));

                    if (AnticheatServer.instantBanMods.contains(mod)) {
                        networkHandlerAccessor.getServer().getPlayerManager().getUserBanList().add(
                                new BannedPlayerEntry(networkHandlerAccessor.getProfile(), null, null, null,
                                        "You are using highly illegal modifications."));
                    } else {
                        networkHandler.disconnect(Text.of("You are using illegal modifications."));
                    }

                    info.cancel();
                    return;
                }
            }
        }
    }
}