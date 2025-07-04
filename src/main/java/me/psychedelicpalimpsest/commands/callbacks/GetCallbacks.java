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


@PuppeteerCommand(
        cmd = "get callbacks",
        description = "Gets the state of all the callbacks"
)
public class GetCallbacks implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        callback.callbacksModView((callbackMap, packetMap) -> {
            JsonObject typicalCallbackResults = new JsonObject();
            JsonObject packetCallbackResults = new JsonObject();
            for (CallbackManager.CallbackType callbackType : CallbackManager.CallbackType.values()){
                typicalCallbackResults.addProperty(
                        callbackType.name(), callbackMap.getOrDefault(callbackType, false)
                );
            }
            CallbackManager.PACKET_LIST.forEach(packet -> packetCallbackResults.addProperty(
                    packet, packetMap.getOrDefault(packet, CallbackManager.PacketCallbackMode.DISABLED).name()
            ));

            callback.resultCallback(BaseCommand.jsonOf(
                    "typical callbacks", typicalCallbackResults,
                    "packet callbacks", packetCallbackResults
            ));
        });
    }
}
