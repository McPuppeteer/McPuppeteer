/**
 * Copyright (C) 2025 - PsychedelicPalimpsest
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.psychedelicpalimpsest.commands.worldAndServers;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@PuppeteerCommand(
        cmd = "get worlds",
        description =
                "List ALL the worlds on this minecraft instances .minecraft folder."
                        + " This can be slow on some installs, as some users may have thousands of worlds."
)
public class GetWorlds implements BaseCommand {

    public CompletableFuture<List<JsonObject>> getWorldListJson() {
        LevelStorage.LevelList list = MinecraftClient.getInstance().getLevelStorage().getLevelList();
        if (list.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }
        List<LevelStorage.LevelSave> levelsSaves = list.levels();
        return MinecraftClient.getInstance().getLevelStorage().loadSummaries(list).thenApply((levelSummaries -> {
            List<JsonObject> worlds = new ArrayList<>(levelSummaries.size());
            for (int i = 0; i < levelSummaries.size(); i++) {
                LevelSummary levelSummary = levelSummaries.get(i);
                LevelStorage.LevelSave save = levelsSaves.get(i);

                worlds.add(BaseCommand.jsonOf(
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
    public void onRequest(JsonObject request, LaterCallback callback) {
        new Thread(() -> {
            getWorldListJson().thenAccept(list -> {
                callback.resultCallback(BaseCommand.jsonOf(
                        "world count", list.size(),
                        "worlds", list
                ));
            });
        }).start();
    }
}
