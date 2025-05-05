package me.psychedelicpalimpsest.commands.actions;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import net.minecraft.client.MinecraftClient;

@PuppeteerCommand(
        cmd = "instantaneous rotation",
        description = "Immediately set the players rotation, no interpolation, just speed!"
)
public class InstantaneousRotation implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        if (!request.has("pitch") || !request.has("yaw")) {
            callback.resultCallback(BaseCommand.jsonOf(
                    "status", "error",
                    "type", "expected argument",
                    "message", "Must have two float arguments, pitch and yaw"
            ));
            return;
        }
        float pitch = request.get("pitch").getAsFloat();
        float yaw = request.get("yaw").getAsFloat();


        MinecraftClient.getInstance().execute(() -> {
            MinecraftClient.getInstance().player.setPitch(pitch);
            MinecraftClient.getInstance().player.setYaw(yaw);

            callback.resultCallback(BaseCommand.jsonOf());
        });

    }
}
