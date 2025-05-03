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

package me.psychedelicpalimpsest.commands.worldAndServers;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.resource.language.I18n;

@PuppeteerCommand(
        cmd = "join server",
        description = "Join multiplayer server. This requires one parameter, the 'address', and it must be valid."
)
public class joinServer implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        if (request.get("address") == null || !request.get("address").isJsonPrimitive()) {
            callback.resultCallback(BaseCommand.jsonOf(
                    "status", "error",
                    "type", "expected argument",
                    "message", "Missing parameter 'address' in join server"
            ));
            return;
        }
        if (!ServerAddress.isValid(request.get("address").getAsString())) {
            callback.resultCallback(BaseCommand.jsonOf(
                    "status", "error",
                    "type", "cannot connect",
                    "message", "Invalid server address"
            ));
            return;
        }


        MinecraftClient.getInstance().execute(() -> {
            String addr = request.get("address").getAsString();

            ServerInfo info = new ServerInfo(I18n.translate("selectServer.defaultName"), addr, ServerInfo.ServerType.OTHER);
            ConnectScreen.connect(
                    MinecraftClient.getInstance().currentScreen,
                    MinecraftClient.getInstance(),
                    ServerAddress.parse(addr),
                    info,
                    false,
                    null
            );

            new Thread(() -> {
                try {
                    while (true) {
                        Thread.sleep(100);

                        if (MinecraftClient.getInstance().currentScreen instanceof DisconnectedScreen){
                            callback.resultCallback(BaseCommand.jsonOf(
                                    "status", "error",
                                    "type", "cannot connect",
                                    "message", "Disconnect during connect"
                            ));
                            return;
                        }
                        if (MinecraftClient.getInstance().world != null && MinecraftClient.getInstance().player != null) {
                            callback.resultCallback(BaseCommand.jsonOf("message", "in game"));
                            return;
                        }
                        if (!(MinecraftClient.getInstance().currentScreen instanceof ConnectScreen)) {
                            callback.resultCallback(BaseCommand.jsonOf(
                                    "status", "error",
                                    "type", "cannot connect",
                                    "message", "Unexpected screen: "
                                            + (MinecraftClient.getInstance().currentScreen.getTitle() != null
                                            ? MinecraftClient.getInstance().currentScreen.getTitle()
                                            : MinecraftClient.getInstance().currentScreen.toString())
                            ));
                            return;
                        }
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }).start();

        });


    }
}
