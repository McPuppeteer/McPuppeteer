package me.psychedelicpalimpsest.commands.input;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import net.minecraft.client.MinecraftClient;


@PuppeteerCommand(
        cmd = "use key click",
        description = "Simulate a single click of the use key"
)
public class UseKey implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        MinecraftClient.getInstance().execute(() -> {
            MinecraftClient.getInstance().doItemUse();
            callback.resultCallback(new JsonObject());
        });
    }
}
