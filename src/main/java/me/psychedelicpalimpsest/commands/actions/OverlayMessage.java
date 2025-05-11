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
package me.psychedelicpalimpsest.commands.actions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.McPuppeteer;
import me.psychedelicpalimpsest.PuppeteerCommand;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

@PuppeteerCommand(
        cmd = "overview message",
        description = "Show a message to the player"
)
public class OverlayMessage implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        JsonElement element =  request.get("message");


        Text text;
        if (element.isJsonObject())
            text = McPuppeteer.createTextJsonSerializer().fromJson(element, Text.class);
        else
            text = Text.of(element.getAsString());


        MinecraftClient.getInstance().execute(()-> MinecraftClient.getInstance().inGameHud.setOverlayMessage(text, false));
        callback.resultCallback(new JsonObject());
    }
}
