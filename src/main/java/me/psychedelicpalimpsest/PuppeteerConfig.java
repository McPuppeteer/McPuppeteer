/**
 *     Copyright (C) 2025 - PsychedelicPalimpsest
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;

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

    public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
            UDP_BROADCAST_INTERVAL,
            SEND_BROADCASTS
    );


    public static final ConfigHotkey OPEN_CONFIG_GUI   = new ConfigHotkey("Open Config UI",  "I,C", "Open this menu");
    public static final ConfigHotkey TOGGLE_FREECAM   = new ConfigHotkey("Freecam",  "I,F", "Toggle freecam");
    public static final ConfigHotkey TOGGLE_FREEROT   = new ConfigHotkey("Free rotation",  "I,R", "Disconnects your player and cameras rotation");
    public static final ConfigHotkey PANNIC_BUTTON   = new ConfigHotkey("Panic button",  "I,P", "Kills Baritone, and throws a python error");


    public static final List<ConfigHotkey> HOTKEY_LIST = ImmutableList.of(
            OPEN_CONFIG_GUI,
            TOGGLE_FREECAM,
            TOGGLE_FREEROT,
            PANNIC_BUTTON
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
        }
        else
            McPuppeteer.LOGGER.error("save(): Config Folder '{}' does not exist!", dir.toAbsolutePath());

    }



}