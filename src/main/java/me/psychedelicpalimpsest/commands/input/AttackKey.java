package me.psychedelicpalimpsest.commands.input;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import net.minecraft.client.MinecraftClient;

@PuppeteerCommand(
        cmd = "attack key click",
        description = "Simulate a single click of the attack key"
)
public class AttackKey implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        MinecraftClient.getInstance().execute(() -> {
            /* Wtf does this do? I just reset attackCooldown because of the check, but what does it actually do??????*/
            MinecraftClient.getInstance().attackCooldown = 0;
            MinecraftClient.getInstance().doAttack();
            callback.resultCallback(new JsonObject());
        });
    }
}
