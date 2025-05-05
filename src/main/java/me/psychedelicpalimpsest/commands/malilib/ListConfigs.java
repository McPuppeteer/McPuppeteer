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

package me.psychedelicpalimpsest.commands.malilib;

import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.util.FileUtils;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

import static me.psychedelicpalimpsest.McPuppeteer.LOGGER;

@PuppeteerCommand(
        cmd = "list config info",
        description = "Lists all mods with configs registered with malilib"
)
public class ListConfigs implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        File configDir = FileUtils.getConfigDirectoryAsPath().toFile();
        List<String> jsonFiles = new ArrayList<>();
        if (configDir.exists() && configDir.isDirectory()) {
            jsonFiles = Arrays.stream(Objects.requireNonNull(
                    configDir.listFiles((dir, name) -> name.endsWith(".json"))
            )).map(File::getName).toList();
        }

        callback.resultCallback(BaseCommand.jsonOf(
                "mods installed", getConfigHandlers().keySet().stream().toList(),
                "json files", jsonFiles
        ));
    }


    public static Map<String, IConfigHandler> getConfigHandlers(){
        try {
            Field field = ConfigManager.class.getDeclaredField("configHandlers");
            field.setAccessible(true);

            return (Map<String, IConfigHandler>) field.get(ConfigManager.getInstance());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOGGER.error("Error trying to get config handlers", e);

            return null;
        }
    }
}
