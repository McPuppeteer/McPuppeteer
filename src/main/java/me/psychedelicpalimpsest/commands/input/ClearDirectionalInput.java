package me.psychedelicpalimpsest.commands.input;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import me.psychedelicpalimpsest.modules.PuppeteerInput;
import net.minecraft.client.MinecraftClient;

@PuppeteerCommand(
        cmd = "clear directional movement",
        description = "Clears the directional movement"
)
public class ClearDirectionalInput implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        MinecraftClient.getInstance().execute(() -> {
            PuppeteerInput.isDirectionalMovement = false;
            PuppeteerInput.directionalSpeed = 1f;
            PuppeteerInput.direction = 0f;

           callback.resultCallback(new JsonObject());
        });
    }
}
