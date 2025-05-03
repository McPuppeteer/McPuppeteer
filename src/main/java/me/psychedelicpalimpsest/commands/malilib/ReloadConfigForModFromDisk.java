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
import fi.dy.masa.malilib.config.IConfigHandler;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;

import java.util.Map;

@PuppeteerCommand(
        cmd = "reload config",
        description = "Force a Malilib mod to reload a config file from disk (Warning: Does not call callbacks associated with settings changes)"
)
public class ReloadConfigForModFromDisk implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        Map<String, IConfigHandler> configs = ListConfigs.getConfigHandlers();
        if (request.get("mod id") == null || !request.get("mod id").isJsonPrimitive()) {
            callback.resultCallback(BaseCommand.jsonOf(
                    "status", "error",
                    "type", "expected argument",
                    "message", "Missing parameter 'mod id'"
            ));
            return;
        }
        String modId = request.get("mod id").getAsString();

        if (!configs.containsKey(modId)) {
            callback.resultCallback(BaseCommand.jsonOf(
                    "status", "error",
                    "type", "unknown mod",
                    "message", "Unknown mod id"
            ));
            return;
        }


        configs.get(modId).load();
    }
}
