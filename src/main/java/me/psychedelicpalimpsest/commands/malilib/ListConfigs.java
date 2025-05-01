package me.psychedelicpalimpsest.commands.malilib;

import com.fasterxml.jackson.databind.JsonNode;
import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.util.FileUtils;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

@PuppeteerCommand(
        cmd = "list config info",
        description = "Lists all mods with configs registered with malilib"
)
public class ListConfigs implements BaseCommand {
    @Override
    public void onRequest(JsonNode request, LaterCallback callback) {
        File configDir = FileUtils.getConfigDirectoryAsPath().toFile();
        List<String> jsonFiles = new ArrayList<>();
        if (configDir.exists() && configDir.isDirectory()) {
            jsonFiles = Arrays.stream(Objects.requireNonNull(
                    configDir.listFiles((dir, name) -> name.endsWith(".json"))
            )).map(File::getName).toList();
        }
        callback.callback(Map.of(
                "mods installed", getConfigHandlers().keySet().stream().toList(),
                "json files", jsonFiles
        ));
    }


    public static Map<String, IConfigHandler> getConfigHandlers(){
        try {
            Field field = ConfigManager.class.getDeclaredField("configHandlers");
            field.setAccessible(true);

            return (Map<String, IConfigHandler>) field.get(ConfigManager.getInstance());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
}
