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

package me.psychedelicpalimpsest.commands;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.McPuppeteer;
import me.psychedelicpalimpsest.PuppeteerCommand;
import me.psychedelicpalimpsest.constants.BuildConstants;
import net.fabricmc.loader.api.FabricLoader;

import java.util.Map;

@PuppeteerCommand(
    cmd = "sources",
    description = "Tells you that the last commit hash was, the github url (if the code was pushed), and the license")
public class GetSourcesAndLegal implements BaseCommand {
	@Override
	public void onRequest(JsonObject request, LaterCallback callback) {
		Map<String, String> contacts = FabricLoader.getInstance()
						   .getModContainer(McPuppeteer.MOD_ID)
						   .get()
						   .getMetadata()
						   .getContact()
						   .asMap();

		callback.resultCallback(BaseCommand.jsonOf(
		    "git commit hash", BuildConstants.GIT_HASH, "github source code",
		    !contacts.containsKey("sources") ? "UNKNOWN"
						     : contacts.get("sources") + "/tree/" + BuildConstants.GIT_HASH,
		    "build date", BuildConstants.BUILD_DATE, "legal license", BuildConstants.LICENSE));
	}
}
