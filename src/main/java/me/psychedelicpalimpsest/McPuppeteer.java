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

package me.psychedelicpalimpsest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.icu.text.MessagePatternUtil;
import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;
import me.psychedelicpalimpsest.modules.PuppeteerInput;
import me.psychedelicpalimpsest.reflection.YarnMapping;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import static me.psychedelicpalimpsest.BaritoneListener.baritoneInit;

public class McPuppeteer {
	public static final String MOD_ID = "mc-puppeteer";
	public static long lastBroadcast = 0;
	public static Set<String> installedMods = null;
	//    public static SerializationTester serializationTester = new SerializationTester();

	public static PuppeteerInput puppeteerInput = new PuppeteerInput();

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static void init() {
		/* Yes, this makes init slower, but I do not care */
		try {
			PuppeteerServer.createServer();
		} catch (IOException e) {
			McPuppeteer.LOGGER.error("Failed to create server", e);
		}

		McPuppeteer.installedMods = FabricLoader.getInstance()
						.getAllMods()
						.stream()
						.map((mod) -> mod.getMetadata().getId())
						.collect(Collectors.toUnmodifiableSet());

		ConfigManager.getInstance().registerConfigHandler(McPuppeteer.MOD_ID, new PuppeteerConfig());
		Registry.CONFIG_SCREEN.registerConfigScreenFactory(
		    new ModInfo(McPuppeteer.MOD_ID, "Puppeteer", GuiConfigs::new));

		PuppeteerConfig.initHotkeys();

		if (McPuppeteer.installedMods.contains("baritone")) {
			baritoneInit();
		}

		YarnMapping.createMapping();
		//        KeyboardOverride.init();
	}

	public static Queue<PuppeteerTask> tasks = new ConcurrentLinkedQueue<>();

	public static JsonObject serializeText(Text text) {
		return BaseCommand.jsonOf(
		    "message", textToString(text),
		    "message json", textToJson(text));
	}

	private static String textToString(Text text) {
		StringBuilder builder = new StringBuilder();
		text.visit((style, string) -> {
			builder.append(string);

			return java.util.Optional.empty();
		}, Style.EMPTY);
		return builder.toString();
	}

	private static JsonElement textToJson(Text text) {
		Text.Serializer serializer = new Text.Serializer(
		    MinecraftClient.getInstance().world != null
			? MinecraftClient.getInstance().world.getRegistryManager()
			: DynamicRegistryManager.of(Registries.REGISTRIES));

		/* Only the text and registries are used */
		return serializer.serialize(text, null, null);
	}
}
