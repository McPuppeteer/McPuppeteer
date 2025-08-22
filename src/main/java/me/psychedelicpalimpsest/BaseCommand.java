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

/*
    If you are making a command, you need to use the @PuppeteerCommand annotation.
    to actually register it.
 */

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import me.psychedelicpalimpsest.PuppeteerServer.PacketOnCompletion;
import net.minecraft.nbt.NbtElement;

import java.util.Collection;
import java.util.Map;

public interface BaseCommand {

	interface CallbackModView {
		void invoke(Map<CallbackManager.CallbackType, Boolean> callbacks, Map<String, CallbackManager.PacketCallbackMode> packetCallbacks);
	}

	interface LaterCallback {
		/* Allows you to both view and modify the callbacks map */
		void callbacksModView(CallbackModView callback);

		/* When something specific to this has occurred */
		void resultCallback(JsonObject result);

		/* Something general everybody should here */
		void generalCallback(JsonObject result);

		void packetResultCallback(byte[] result);

		void nbtResultCallback(NbtElement result);

		void simulatePuppeteerCommand(JsonObject request, PacketOnCompletion onCompletion);
	}

	enum CommandContext {
		ANY,

		/* Before the world has loaded */
		PRE_PLAY,

		/* Must be in world */
		PLAY,

		/* Must be in world, and the player can move (Ex. Not in a bed) */
		PLAY_WITH_MOVEMENT,
	}

	/* You can request a packet be sent immediately */
	void onRequest(JsonObject request, LaterCallback callback);

	static JsonElement jsonObjectOf(Object object) {
		switch (object) {
			case String s -> {
				return new JsonPrimitive(s);
			}
			case Number i -> {
				return new JsonPrimitive(i);
			}

			case JsonElement jsonElement -> {
				return jsonElement;
			}
			case Map map -> {
				JsonObject jsonObject = new JsonObject();
				map.forEach((key, value) -> {
					if (!(key instanceof String)) {
						throw new IllegalArgumentException("Unknown map key type: " + key.getClass());
					}
					jsonObject.add((String) key, jsonObjectOf(value));
				});

				return jsonObject;
			}
			case Collection ignored -> {
				Collection<Object> collection = (Collection<Object>) object;
				JsonArray jsonArray = new JsonArray();
				for (Object o : collection) {
					jsonArray.add(jsonObjectOf(o));
				}
				return jsonArray;
			}
			case null, default -> throw new IllegalArgumentException("Unknown value type: " + object.getClass());
		}
	}

	static JsonObject jsonOf(Object... objects) {
		if (objects.length % 2 != 0) {
			throw new IllegalArgumentException("Must have an even number of arguments (key-value pairs)");
		}
		JsonObject jsonObject = new JsonObject();
		for (int i = 0; i < objects.length; i += 2) {
			Object key = objects[i];
			Object value = objects[i + 1];
			if (!(key instanceof String)) {
				throw new IllegalArgumentException("Key must be a string");
			}
			jsonObject.add((String) key, jsonObjectOf(value));
		}
		return jsonObject;
	}
}
