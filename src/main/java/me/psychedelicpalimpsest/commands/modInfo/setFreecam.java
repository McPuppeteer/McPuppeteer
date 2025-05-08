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

package me.psychedelicpalimpsest.commands.modInfo;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import me.psychedelicpalimpsest.modules.Freecam;

@PuppeteerCommand(
        cmd="set freecam",
        description = "Enable/disable freecam"
)
public class setFreecam implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        if (!request.has("enabled") || !request.get("enabled").isJsonPrimitive()) {
            callback.resultCallback(BaseCommand.jsonOf(
                    "status", "error",
                    "message", "Must have 'enabled' as a boolean property",
                    "type", "expected argument"
            ));
            return;
        }
        if (request.get("enabled").getAsBoolean() != Freecam.isFreecamActive()) {
            Freecam.toggleFreecam(null, null);
        }

        callback.resultCallback(BaseCommand.jsonOf());
    }
}
