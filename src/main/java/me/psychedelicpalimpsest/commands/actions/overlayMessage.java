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
public class overlayMessage implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        JsonElement element =  request.get("message");


        Text text;
        if (element.isJsonObject())
            text = McPuppeteer.createTextJsonSerializer().fromJson(element, Text.class);
        else
            text = Text.of(element.getAsString());


        MinecraftClient.getInstance().inGameHud.setOverlayMessage(text, false);
        callback.resultCallback(new JsonObject());
    }
}
