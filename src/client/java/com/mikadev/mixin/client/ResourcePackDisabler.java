package com.mikadev.mixin.client;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

@Mixin(OptionsScreen.class)
public class ResourcePackDisabler {
    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/option/OptionsScreen;createButton(Lnet/minecraft/text/Text;Ljava/util/function/Supplier;)Lnet/minecraft/client/gui/widget/ButtonWidget;"))
    private ButtonWidget createButton(OptionsScreen optionsScreen, Text message, Supplier<Screen> screenSupplier) {
        OptionsScreenAccessor optionsScreenAccessor = (OptionsScreenAccessor) (Object) optionsScreen;
        ButtonWidget buttonWidget = ButtonWidget
                .builder(message, button -> optionsScreenAccessor.getClient().setScreen((Screen) screenSupplier.get()))
                .build();

        if (message.getString().equals(Text.translatable((String) "options.resourcepack").getString())
                && !MinecraftClient.getInstance().isConnectedToLocalServer()
                && MinecraftClient.getInstance().world != null) {
            buttonWidget.active = false;
        }

        return buttonWidget;
    }
}
