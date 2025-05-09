package me.psychedelicpalimpsest.commands.input;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import me.psychedelicpalimpsest.modules.PuppeteerInput;
import net.minecraft.client.MinecraftClient;

@PuppeteerCommand(
        cmd = "set directional movement degree",
        description = "Make the player move a certain direction, in degrees, globally independent of the players rotation."
)
public class SetDirectionalMovement implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        MinecraftClient.getInstance().execute(() -> {
            PuppeteerInput.direction = request.get("direction").getAsFloat();
            PuppeteerInput.directionalSpeed = request.has("speed") ? request.get("speed").getAsFloat() : 1f;

            if (!(request.has("force") && request.get("force").getAsBoolean())){
                PuppeteerInput.directionalSpeed = Math.clamp(PuppeteerInput.directionalSpeed, 0f, 1f);
            }


            PuppeteerInput.isDirectionalMovement = true;
            callback.resultCallback(new JsonObject());
        });
    }
}
