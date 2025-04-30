package me.psychedelicpalimpsest.commands.modInfo;


import com.fasterxml.jackson.databind.JsonNode;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;

import java.util.Map;
import java.util.Optional;

import static me.psychedelicpalimpsest.McPuppeteer.MOD_ID;

@PuppeteerCommand(
        cmd = "client info",
        description = "Returns information about the client itself"
)
public class GetClientInfo implements BaseCommand {
    @Override
    public void onRequest(JsonNode request, LaterCallback callback) {
        Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer(MOD_ID);
        if (mod.isEmpty()) {
            throw new RuntimeException("Could not find ModContainer for " + MOD_ID);
        }
        ModContainer modc = mod.get();
        MinecraftClient mc = MinecraftClient.getInstance();


        callback.callback(Map.of(
                "puppeteer commands", GetCommandsList.getCommands(),
                "puppeteer info", Map.of(
                        "name", modc.getMetadata().getName(),
                        "mod id", MOD_ID,
                        "version", modc.getMetadata().getVersion().getFriendlyString(),
                        "description", modc.getMetadata().getDescription()
                ),
                "minecraft version", SharedConstants.getGameVersion().getName(),
                "minecraft protocol version", SharedConstants.getProtocolVersion(),

                "username", MinecraftClient.getInstance().getSession().getUsername(),
                "account type", MinecraftClient.getInstance().getSession().getAccountType().getName(),


                "uuid", MinecraftClient.getInstance().getSession().getXuid().orElse("UNKNOWN")
        ));


    }
}
