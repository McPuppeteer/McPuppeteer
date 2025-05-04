package me.psychedelicpalimpsest.modules;

import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;

import static me.psychedelicpalimpsest.PuppeteerEffects.noRotationEffect;


public class Freerot {

    private static boolean isFreerot = false;
    public static boolean toggleFreerot(KeyAction keyAction, IKeybind iKeybind) {
        isFreerot = !isFreerot;

        if (isFreerot) initializeFreerot();
        else deactivateFreerot();
        return true;
    }

    public static void initializeFreerot(){
        if (!isFreerot) return;
        noRotationEffect.isActive = true;
    }

    public static void deactivateFreerot(){
        if (isFreerot) return;
        noRotationEffect.isActive = false;
    }


    public static Boolean isFreerotActive(){
        return isFreerot;
    }
}
