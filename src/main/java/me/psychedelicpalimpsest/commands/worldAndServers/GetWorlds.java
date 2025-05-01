package me.psychedelicpalimpsest.commands.worldAndServers;

import com.fasterxml.jackson.databind.JsonNode;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@PuppeteerCommand(
        cmd = "get worlds",
        description =
                "List ALL the worlds on this minecraft instances .minecraft folder."
              +" This can be slow on some installs, as some users may have thousands of worlds."
)
public class GetWorlds implements BaseCommand {

    public CompletableFuture<List<Map<String, Object>>> getWorldListJson(){
        LevelStorage.LevelList list = MinecraftClient.getInstance().getLevelStorage().getLevelList();
        if (list.isEmpty()){
            return CompletableFuture.completedFuture(List.of());
        }
        List<LevelStorage.LevelSave> levelsSaves = list.levels();
        return MinecraftClient.getInstance().getLevelStorage().loadSummaries(list).thenApply((levelSummaries ->{
            List<Map<String, Object>> worlds = new ArrayList<>(levelSummaries.size());
            for (int i = 0; i < levelSummaries.size(); i++) {
                LevelSummary levelSummary = levelSummaries.get(i);
                LevelStorage.LevelSave save = levelsSaves.get(i);

                worlds.add(Map.of(
                        "display name", levelSummary.getDisplayName(),
                        "load name", levelSummary.getName(),
                        "last played", levelSummary.getLastPlayed(),
                        "version", levelSummary.getVersion().toString(),
                        "details", levelSummary.getDetails().toString(),
                        "icon path", levelSummary.getIconPath().toString(),
                        "save path", save.getRootPath()
                ));
            }
            return worlds;
        }));
    }


    @Override
    public void onRequest(JsonNode request, LaterCallback callback) {
        new Thread(() -> {
            getWorldListJson().thenAccept(list -> {
                callback.callback(Map.of(
                        "world count", list.size(),
                        "worlds", list
                ));
            });
        }).start();
    }
}
