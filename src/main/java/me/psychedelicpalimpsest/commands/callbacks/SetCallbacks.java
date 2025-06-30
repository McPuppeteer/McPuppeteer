/**
 * Copyright (C) 2025 - PsychedelicPalimpsest
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */


package me.psychedelicpalimpsest.commands.callbacks;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.CallbackManager;
import me.psychedelicpalimpsest.PuppeteerCommand;

import java.util.Map;

@PuppeteerCommand(
        cmd = "set callbacks",
        description = ""
)
public class SetCallbacks implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        JsonObject userCallbacks = request.getAsJsonObject("callbacks");





        final var packetCallbacks = userCallbacks.entrySet().stream()
                .filter(entry -> !CallbackManager.CALLBACK_STRING_TYPES.contains(entry.getKey()))
                .map((entry -> Map.entry(entry.getKey(), entry.getValue().getAsString())))
                .toList();


        var typicalCallbacks = userCallbacks.entrySet().stream()
                .filter(entry -> CallbackManager.CALLBACK_STRING_TYPES.contains(entry.getKey())).toList();
        if (packetCallbacks.stream().anyMatch((entry) -> !CallbackManager.PACKET_LIST.contains(entry.getKey()))) {
            callback.resultCallback(BaseCommand.jsonOf(
                    "status", "error",
                    "type", "unknown callback",
                    "message", "Unknown packet callback"
            ));
            return;
        }
        if (typicalCallbacks.stream().anyMatch(entry -> null == entry.getKey())) {
            callback.resultCallback(BaseCommand.jsonOf(
                    "status", "error",
                    "type", "unknown callback",
                    "message", "Unknown callback"
            ));
            return;
        }


        final var typicalCallbackList = typicalCallbacks.stream().map(
                (entry) -> Map.entry(
                        CallbackManager.CallbackType.valueOf(entry.getKey()),
                        entry.getValue().getAsBoolean()
                )
        ).toList();


        callback.callbacksModView((callbackMap, packetMap) -> {
            typicalCallbackList.forEach(entry -> callbackMap.put(entry.getKey(), entry.getValue()));
            packetCallbacks.forEach(entry ->
                    packetMap.put(entry.getKey(), CallbackManager.PacketCallbackMode.valueOf(entry.getValue())));

            callback.resultCallback(new JsonObject());
        });
    }
}
