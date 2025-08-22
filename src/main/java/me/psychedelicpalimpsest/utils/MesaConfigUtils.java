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

package me.psychedelicpalimpsest.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import me.psychedelicpalimpsest.BaseCommand;

import java.util.List;
import java.util.Map;

public class MesaConfigUtils {
	/*
	Most Masa-style mods have a very simular config system. A couple categories, each with lists of Malilib config
	items. So it only makes sense to exploit this for integration.
    */

	public static JsonObject dumpJsonForMasaConfig(Map<String, List<? extends IConfigBase>> configs) {
		JsonObject main = new JsonObject();
		configs.forEach((k, v) -> {
			JsonObject sub = new JsonObject();
			v.forEach((item) -> {
				JsonObject configItem = new JsonObject();
				configItem.addProperty("comment", item.getComment());
				configItem.addProperty("pretty name", item.getPrettyName());
				configItem.add("value", item.getAsJsonElement());

				sub.add(item.getName(), configItem);
			});

			main.add(k, sub);
		});

		return main;
	}

	public static JsonObject handleGetMalilibConfigRequest(Map<String, List<? extends IConfigBase>> config,
							       JsonObject request) {
		if (!request.has("category") || !request.has("name")) {
			return BaseCommand.jsonOf("status", "error", "type", "expected argument",

						  "message", "Must contain 'category' and 'name'");
		}
		return BaseCommand.jsonOf("value", getValueForMasaConfig(config, request.get("category").getAsString(),
									 request.get("name").getAsString()));
	}

	public static JsonElement getValueForMasaConfig(Map<String, List<? extends IConfigBase>> config,
							String category, String name) {
		if (!config.containsKey(category)) return JsonNull.INSTANCE;
		for (IConfigBase configItem : config.get(category)) {
			if (!configItem.getName().equals(name)) continue;
			return configItem.getAsJsonElement();
		}

		return JsonNull.INSTANCE;
	}

	public static JsonObject handleSetMalilibConfigRequest(Map<String, List<? extends IConfigBase>> config,
							       JsonObject request) {
		if (!request.has("category") || !request.has("name") || !request.has("value") ||
		    !request.get("value").isJsonObject()) {
			return BaseCommand.jsonOf("status", "error", "type", "expected argument",

						  "message", "Must contain 'category' and 'name'");
		}
		return setValueForMasaConfig(config, request.get("category").getAsString(),
					     request.get("name").getAsString(), request.get("value").getAsJsonObject());
	}

	public static JsonObject setValueForMasaConfig(Map<String, List<? extends IConfigBase>> config, String catagory,
						       String name, JsonElement value) {

		if (config.containsKey(catagory))
			for (IConfigBase configItem : config.get(catagory)) {
				if (!configItem.getName().equals(name)) continue;
				configItem.setValueFromJsonElement(value);
				return new JsonObject();
			}

		return BaseCommand.jsonOf(
		    "status", "error", "type", "unknown config item", "message",
		    "Cannot lookup value for Mesa style Malilib item '" + catagory + "'/'" + name + "'. Reason: " +
			(config.containsKey(catagory) ? "Category exists, item does not" : "Category does not exist"));
	}

	public static JsonObject handleExecMesaConfigRequest(Map<String, List<? extends IConfigBase>> config,
							     JsonObject request) {
		if (!request.has("category") || !request.has("name")) {
			return BaseCommand.jsonOf("status", "error", "type", "expected argument",

						  "message", "Must contain 'category' and 'name'");
		}
		KeyAction action = KeyAction.BOTH;

		if (request.has("action") && request.get("action").isJsonPrimitive()) {
			if (request.get("action").getAsString().equals("press")) action = KeyAction.PRESS;
			else if (request.get("action").getAsString().equals("release"))
				action = KeyAction.RELEASE;
		}

		return executeHotkeyForMasaConfig(config, action, request.get("category").getAsString(),
						  request.get("name").getAsString());
	}

	public static JsonObject executeHotkeyForMasaConfig(Map<String, List<? extends IConfigBase>> config,
							    KeyAction action, String catagory, String name) {
		if (config.containsKey(catagory))
			for (IConfigBase configItem : config.get(catagory)) {
				if (!configItem.getName().equals(name)) continue;
				if (!(configItem instanceof IHotkey hotkey))
					return BaseCommand.jsonOf("status", "error", "type", "unknown config item",
								  "message",
								  "'" + catagory + "'/'" + name + "' is not a hotkey!");

				/* To my knowledge, this is the only thing that implements IKeybind */
				if (!(hotkey.getKeybind() instanceof KeybindMulti))
					return BaseCommand.jsonOf(
					    "status", "error", "type", "exception", "message",
					    "An IKeybind that isn't KeybindMulti cannot be handled!");

				IHotkeyCallback callback = ((KeybindMulti) hotkey.getKeybind()).getCallback();

				if (callback == null)
					return BaseCommand.jsonOf("status", "error", "type", "exception", "message",
								  "Callback is null, and thus cannot be handled!");

				/* So, we use it to simulate a key press */
				callback.onKeyAction(action, hotkey.getKeybind());
				return new JsonObject();
			}

		return BaseCommand.jsonOf(
		    "status", "error", "type", "unknown config item", "message",
		    "Cannot lookup value for Mesa style Malilib item '" + catagory + "'/'" + name + "'. Reason: " +
			(config.containsKey(catagory) ? "Category exists, item does not" : "Category does not exist"));
	}
}
