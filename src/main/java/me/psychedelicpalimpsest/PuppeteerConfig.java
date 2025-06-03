 /**
 * Copyright (C) 2025 - PsychedelicPalimpsest
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */


package me.psychedelicpalimpsest;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigFloat;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.IKeybindProvider;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import me.psychedelicpalimpsest.modules.Freecam;
import me.psychedelicpalimpsest.modules.Freerot;
import me.psychedelicpalimpsest.modules.NoWalk;
import me.psychedelicpalimpsest.modules.PuppeteerInput;
import net.minecraft.client.MinecraftClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


/*
    This file is very much 'inspired' by litematica, as malilib is an undocumented mess

    URL: https://github.com/sakura-ryoko/litematica/blob/LTS/1.21.3/src/main/java/fi/dy/masa/litematica/config/Configs.java
*/

public class PuppeteerConfig implements IConfigHandler {

    private static final String CONFIG_FILE_NAME = McPuppeteer.MOD_ID + ".json";


    public static ConfigFloat UDP_BROADCAST_INTERVAL = new ConfigFloat("UDP broadcast interval", 3f, 0.5f, 10f, "Amount of time (In seconds) between UDP broadcasts. Smaller values mean you can detect the game faster in python, but slow down your network very slightly.");
    public static ConfigBoolean SEND_BROADCASTS = new ConfigBoolean("Send UDP broadcasts", true, "If you disable broadcasts you will not be able to \"Discover\" the client from python, but you might need this if you are on public wifi.");

    /** See: TweekerooCameraMixin.java */
    public static ConfigBoolean WARN_ON_TWEAKEROO_FREECAM = new ConfigBoolean("Warn on tweakeroo freecam", true, "Gives a warning message if the user attempts to use the freecam module on tweakeroo");

    public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
            UDP_BROADCAST_INTERVAL,
            SEND_BROADCASTS,
            WARN_ON_TWEAKEROO_FREECAM
    );


    public static final ConfigHotkey OPEN_CONFIG_GUI = new ConfigHotkey("Open Config UI", "I,C", "Open this menu");
    public static final ConfigHotkey TOGGLE_FREECAM = new ConfigHotkey("Freecam", "I,F", "Toggle freecam");
    public static final ConfigHotkey TOGGLE_FREEROT = new ConfigHotkey("Free rotation", "I,R", "Disconnects your player and cameras rotation");
    public static final ConfigHotkey TOGGLE_NOWALK = new ConfigHotkey("No walk", "I,W", "Toggles if the player it allowed to walk");
    public static final ConfigHotkey PANIC_BUTTON = new ConfigHotkey("Panic button", "I,P", "Kills Baritone, and throws a python error");


    public static final List<ConfigHotkey> HOTKEY_LIST = ImmutableList.of(
            OPEN_CONFIG_GUI,
            TOGGLE_FREECAM,
            TOGGLE_FREEROT,
            PANIC_BUTTON,
            TOGGLE_NOWALK
    );


    @Override
    public void load() {
        Path configFile = FileUtils.getConfigDirectoryAsPath().resolve(CONFIG_FILE_NAME);

        if (Files.exists(configFile) && Files.isReadable(configFile)) {
            JsonElement element = JsonUtils.parseJsonFileAsPath(configFile);

            if (element != null && element.isJsonObject()) {
                JsonObject root = element.getAsJsonObject();

                ConfigUtils.readConfigBase(root, "Options", OPTIONS);
                ConfigUtils.readConfigBase(root, "Hot keys", HOTKEY_LIST);
            } else
                McPuppeteer.LOGGER.error("load(): Failed to load config file '{}'.", configFile.toAbsolutePath());

        }
    }

    @Override
    public void save() {
        Path dir = FileUtils.getConfigDirectoryAsPath();

        if (!Files.exists(dir))
            FileUtils.createDirectoriesIfMissing(dir);


        if (Files.isDirectory(dir)) {
            JsonObject root = new JsonObject();
            ConfigUtils.writeConfigBase(root, "Options", OPTIONS);
            ConfigUtils.writeConfigBase(root, "Hot keys", HOTKEY_LIST);

            JsonUtils.writeJsonToFileAsPath(root, dir.resolve(CONFIG_FILE_NAME));
        } else
            McPuppeteer.LOGGER.error("save(): Config Folder '{}' does not exist!", dir.toAbsolutePath());

    }


    public static void initHotkeys() {
        PuppeteerConfig.OPEN_CONFIG_GUI.getKeybind().setCallback((action, key) -> {
            MinecraftClient.getInstance().setScreen(new GuiConfigs());
            return true;
        });

        PuppeteerConfig.TOGGLE_FREECAM.getKeybind().setCallback(Freecam::toggleFreecam);
        PuppeteerConfig.TOGGLE_FREEROT.getKeybind().setCallback(Freerot::toggleFreerot);
        PuppeteerConfig.TOGGLE_NOWALK.getKeybind().setCallback(NoWalk::toggle);
        PuppeteerConfig.PANIC_BUTTON.getKeybind().setCallback((ignored, ignored2) -> {
            if (McPuppeteer.installedMods.contains("baritone")) {
                BaritoneListener.panic();
            }

            if (!McPuppeteer.tasks.isEmpty()) {
                McPuppeteer.tasks.peek().kill();
                McPuppeteer.tasks.clear();
            }

            if (NoWalk.isActive)
                NoWalk.toggle(null, null);
            if (Freerot.isFreerotActive())
                Freerot.toggleFreerot(null, null);
            if (Freecam.isFreecamActive())
                Freecam.toggleFreecam(null, null);

            PuppeteerInput.isForcePressed.clear();
            PuppeteerInput.allowUserInput = true;
            PuppeteerInput.isDirectionalMovement = false;


            PuppeteerServer.broadcastJsonPacket(CallbackManager.CallbackType.FORCED, BaseCommand.jsonOf(
                    "status", "error",
                    "type", "panic",
                    "message", "The user has pressed that panic button",

                    /* Specifically force the client to interpret as error */
                    "callback", false
            ));

            return true;
        });


        InputEventHandler.getKeybindManager().registerKeybindProvider(new IKeybindProvider() {
            @Override
            public void addKeysToMap(IKeybindManager manager) {
                for (IHotkey hotkey : PuppeteerConfig.HOTKEY_LIST)
                    manager.addKeybindToMap(hotkey.getKeybind());
            }

            @Override
            public void addHotkeys(IKeybindManager manager) {
                manager.addHotkeysForCategory(McPuppeteer.MOD_ID, "", PuppeteerConfig.HOTKEY_LIST);
            }
        });
    }


}