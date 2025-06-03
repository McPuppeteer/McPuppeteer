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

import java.util.List;
import java.util.Map;

@PuppeteerCommand(
        cmd = "set callbacks",
        description = ""
)
public class SetCallbacks implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        JsonObject userCallbacks = request.getAsJsonObject("callbacks");


        final List<Map.Entry<CallbackManager.CallbackType, Boolean>> entries = userCallbacks.entrySet().stream().map(
                (entry) -> Map.entry(
                        CallbackManager.CALLBACK_STRING_TYPE_MAP.get(entry.getKey()),
                        entry.getValue().getAsBoolean()
                )
        ).toList();


        callback.callbacksModView((callbackMap) -> {
            entries.forEach(entry -> {
                callbackMap.remove(entry.getKey());
                callbackMap.put(entry.getKey(), entry.getValue());
            });
            callback.resultCallback(new JsonObject());
        });
    }
}
