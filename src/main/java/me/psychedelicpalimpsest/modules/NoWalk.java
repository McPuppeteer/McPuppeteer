package me.psychedelicpalimpsest.modules;

import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import me.psychedelicpalimpsest.PuppeteerEffects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class NoWalk {
    public static boolean isActive = false;
    public static boolean toggle(KeyAction keyAction, IKeybind iKeybind) {
        isActive = !isActive;


        MinecraftClient.getInstance().inGameHud.setOverlayMessage(Text.of(isActive ? "Enabled NoWalk" : "Disabled NoWalk"), false);


        PuppeteerEffects.noWalkEffect.isActive = isActive;
        PuppeteerInput.allowUserInput = !isActive;

        return true;
    }

}
