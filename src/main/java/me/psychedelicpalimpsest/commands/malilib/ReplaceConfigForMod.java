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
import fi.dy.masa.malilib.util.FileUtils;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

@PuppeteerCommand(
        cmd = "replace config",
        description = "Replaces a json file in the config folder"
)
public class ReplaceConfigForMod implements BaseCommand {
    @Override
    public void onRequest(JsonNode request, LaterCallback callback) {
        if (request.get("file name") == null || !request.get("file name").isTextual()) {
            callback.callback(Map.of(
                    "status", "error",
                    "message", "Missing parameter 'file name'"
            ));
            return;
        }
        if (request.get("json") == null) {
            callback.callback(Map.of(
                    "status", "error",
                    "message", "Missing parameter 'json'"
            ));
            return;
        }
        File config = FileUtils.getConfigDirectoryAsPath().resolve(request.get("file name").asText()).toFile();

        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(config, request.get("json"));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}
