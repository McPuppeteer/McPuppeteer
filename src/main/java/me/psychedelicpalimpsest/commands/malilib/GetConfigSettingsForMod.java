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
import com.google.gson.JsonParser;
import fi.dy.masa.malilib.util.FileUtils;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

@PuppeteerCommand(
        cmd = "get config",
        description = "Load a config file with a certain name from the disk, assumes it is json. Takes one parameter: 'file name'"
)
public class GetConfigSettingsForMod implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        if (request.get("file name") == null || !request.get("file name").isJsonPrimitive()) {
            callback.resultCallback(BaseCommand.jsonOf(
                    "status", "error",
                    "type", "expected argument",
                    "message", "Missing parameter 'file name'"
            ));
            return;
        }
        File config = FileUtils.getConfigDirectoryAsPath().resolve(request.get("file name").getAsString()).toFile();
        if (!config.exists()) {
            callback.resultCallback(BaseCommand.jsonOf(
            "status", "error",
                    "type", "config file missing",
                    "message", "Config file does not exist"
            ));
            return;
        }
        try {
            FileReader reader = new FileReader(config);



            callback.resultCallback(BaseCommand.jsonOf(
                    "json", JsonParser.parseReader(reader)
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
