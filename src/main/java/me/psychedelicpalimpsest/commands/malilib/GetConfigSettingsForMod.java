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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import fi.dy.masa.malilib.util.FileUtils;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;

import java.io.*;
import java.util.Map;

@PuppeteerCommand(
        cmd = "get config",
        description = "Load a config file with a certain name from the disk, assumes it is json. Takes one parameter: 'file name'"
)
public class GetConfigSettingsForMod implements BaseCommand {
    @Override
    public void onRequest(JsonNode request, LaterCallback callback) {
        if (request.get("file name") == null || !request.get("file name").isTextual()) {
            callback.callback(Map.of(
                    "status", "error",
                    "message", "Missing parameter 'file name'"
            ));
            return;
        }
        File config = FileUtils.getConfigDirectoryAsPath().resolve(request.get("file name").asText()).toFile();
        if (!config.exists()) {
            callback.callback(Map.of(
                    "status", "error",
                    "message", "Config file does not exist"
            ));
            return;
        }
        try {
            FileReader reader = new FileReader(config);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode tree = mapper.readTree(reader);

            callback.callback(Map.of(
                    "json", tree
            ));

            reader.close();
        } catch (FileNotFoundException e) {
            /* This should not be possible as we check this ourselves */
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
