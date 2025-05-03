/**
 *     Copyright (C) 2025 - PsychedelicPalimpsest
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.psychedelicpalimpsest;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.IKeybindProvider;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;
import me.psychedelicpalimpsest.modules.Freecam;
import me.psychedelicpalimpsest.modules.Freerot;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static me.psychedelicpalimpsest.BaritoneListener.baritoneInit;


public class McPuppeteer {
	public static final String MOD_ID = "mc-puppeteer";
	public static long lastBroadcast = 0;
	public static Set<String> installedMods = null;

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
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
				.map((mod)->mod.getMetadata().getId())
				.collect(Collectors.toUnmodifiableSet());

		ConfigManager.getInstance().registerConfigHandler(McPuppeteer.MOD_ID, new PuppeteerConfig());
		Registry.CONFIG_SCREEN.registerConfigScreenFactory(
				new ModInfo(McPuppeteer.MOD_ID, "Puppeteer", GuiConfigs::new)
		);


		PuppeteerConfig.OPEN_CONFIG_GUI.getKeybind().setCallback((action, key) -> {
			MinecraftClient.getInstance().setScreen(new GuiConfigs());
			return true;
		});

		PuppeteerConfig.TOGGLE_FREECAM.getKeybind().setCallback(Freecam::toggleFreecam);
		PuppeteerConfig.TOGGLE_FREEROT.getKeybind().setCallback(Freerot::toggleFreerot);
		PuppeteerConfig.PANNIC_BUTTON.getKeybind().setCallback((ignored, ignored2)->{
			if (McPuppeteer.installedMods.contains("baritone")) {
				BaritoneListener.panic();
			}
			PuppeteerServer.broadcastJsonPacket(BaseCommand.jsonOf(
			"status", "error",
					"type", "panic",
					"message", "The user has pressed that panic button",

					/* Specifically force the client to interpret as error */
					"callback", false
			));

			return true;
		});



		InputEventHandler.getKeybindManager().registerKeybindProvider(new IKeybindProvider() {
			@Override
			public void addKeysToMap(IKeybindManager manager) {
				for (IHotkey hotkey : PuppeteerConfig.HOTKEY_LIST)
					manager.addKeybindToMap(hotkey.getKeybind());
			}

			@Override
			public void addHotkeys(IKeybindManager manager) {
				manager.addHotkeysForCategory(McPuppeteer.MOD_ID, "", PuppeteerConfig.HOTKEY_LIST);
			}
		});

		if (McPuppeteer.installedMods.contains("baritone")){
			baritoneInit();
		}


	}


	public static PuppeteerEffect noRotationEffect = new PuppeteerEffect(Identifier.of(MOD_ID, "textures/no_rotation.png"));


	/* Just some simple potion effect like icons */
	public static final List<PuppeteerEffect> effects = ImmutableList.of(
			noRotationEffect
	);


	public static final class PuppeteerEffect {
		public boolean isActive = false;
		public final Identifier texture;

		public PuppeteerEffect(Identifier texture) {
			this.texture = texture;
		}
	}



}