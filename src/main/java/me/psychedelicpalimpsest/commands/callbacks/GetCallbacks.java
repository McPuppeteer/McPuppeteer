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


package me.psychedelicpalimpsest.commands.callbacks;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.CallbackManager;
import me.psychedelicpalimpsest.PuppeteerCommand;


@PuppeteerCommand(
        cmd = "get callbacks",
        description = "Gets the state of all the callbacks"
)
public class GetCallbacks implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        callback.callbacksModView((callbackMap) -> {
            JsonObject result = new JsonObject();
            CallbackManager.CALLBACK_TYPE_STRING_MAP.forEach((type, name) -> {
                result.addProperty(
                        name, callbackMap.getOrDefault(type, false)
                );
            });

            callback.resultCallback(BaseCommand.jsonOf(
                    "callbacks", result
            ));
        });
    }
}
