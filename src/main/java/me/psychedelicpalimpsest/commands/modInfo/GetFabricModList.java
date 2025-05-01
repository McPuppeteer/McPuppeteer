package me.psychedelicpalimpsest.commands.modInfo;

import com.fasterxml.jackson.databind.JsonNode;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.Person;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@PuppeteerCommand(
        cmd = "get mod list",
        description = "List all the installed fabric mods"
)
public class GetFabricModList implements BaseCommand {
    @Override
    public void onRequest(JsonNode request, LaterCallback callback) {
        List<Map<String, Object>> mods = FabricLoader.getInstance().getAllMods().stream().map((modContainer -> Map.of(
                "name", modContainer.getMetadata().getName(),
                "description", modContainer.getMetadata().getDescription(),
                "version", modContainer.getMetadata().getVersion().getFriendlyString(),
                "mod id", modContainer.getMetadata().getId(),
                "type", modContainer.getMetadata().getType(),

                "author names", modContainer.getMetadata().getAuthors().stream().map(Person::getName).collect(Collectors.toList()),
                "author contacts", modContainer.getMetadata().getAuthors().stream().map((author) -> author.getContact().asMap()).collect(Collectors.toList()),

                "contacts", modContainer.getMetadata().getContact().asMap()
        ))).toList();

        callback.callback(Map.of("mods", mods));
    }
}
