/**
 * Copyright (C) 2025 - PsychedelicPalimpsest
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.psychedelicpalimpsest.commands.modInfo;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.Person;

import java.util.List;
import java.util.stream.Collectors;

@PuppeteerCommand(cmd = "get mod list", description = "List all the installed fabric mods")
public class GetFabricModList implements BaseCommand {
	@Override
	public void onRequest(JsonObject request, LaterCallback callback) {
		List<JsonObject> mods =
		    FabricLoader.getInstance()
			.getAllMods()
			.stream()
			.map((modContainer
			      -> BaseCommand.jsonOf("name", modContainer.getMetadata().getName(), "description",
						    modContainer.getMetadata().getDescription(), "version",
						    modContainer.getMetadata().getVersion().getFriendlyString(),
						    "mod id", modContainer.getMetadata().getId(), "type",
						    modContainer.getMetadata().getType(),

						    "author names",
						    modContainer.getMetadata()
							.getAuthors()
							.stream()
							.map(Person::getName)
							.collect(Collectors.toList()),
						    "author contacts",
						    modContainer.getMetadata()
							.getAuthors()
							.stream()
							.map((author) -> author.getContact().asMap())
							.collect(Collectors.toList()),

						    "contacts", modContainer.getMetadata().getContact().asMap())))
			.toList();

		callback.resultCallback(BaseCommand.jsonOf("mods", mods));
	}
}
