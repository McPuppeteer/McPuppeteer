package me.psychedelicpalimpsest.commands.input;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import me.psychedelicpalimpsest.modules.PuppeteerInput;
import net.minecraft.client.MinecraftClient;


@PuppeteerCommand(
        cmd = "set directional movement vector",
        description = ""
)
public class SetDirectionalInputVector implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        MinecraftClient.getInstance().execute(() -> {
            setDirectionalInputVector(
                    request.get("x").getAsFloat(),
                    request.get("z").getAsFloat()
            );

            PuppeteerInput.directionalSpeed = request.has("speed") ? request.get("speed").getAsFloat() : 1f;

            if (!(request.has("force") && request.get("force").getAsBoolean())){
                PuppeteerInput.directionalSpeed = Math.clamp(PuppeteerInput.directionalSpeed, 0f, 1f);
            }
            PuppeteerInput.isDirectionalMovement = true;

            callback.resultCallback(new JsonObject());
        });
    }

    public void setDirectionalInputVector(float x, float z){
        PuppeteerInput.direction = (float) Math.toDegrees(Math.atan2(z, x));
    }


}
