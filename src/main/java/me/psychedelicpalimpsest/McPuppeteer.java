package me.psychedelicpalimpsest;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.IKeybindProvider;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;


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
			PuppeteerSocketServer.createServer();
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
			System.out.println("Open Config GUI");
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


	}

}