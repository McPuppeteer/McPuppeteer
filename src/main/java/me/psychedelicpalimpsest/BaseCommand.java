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


/*
    If you are making a command, you need to use the @PuppeteerCommand annotation.
    to actually register it.
 */

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Collection;
import java.util.Map;

public interface BaseCommand {

    interface CallbackModView {
        void invoke(Map<CallbackManager.CallbackType, Boolean> callbacks);
    }
    interface LaterCallback {
        /* Allows you to both view and modify the callbacks map */
        void callbacksModView(CallbackModView callback);
        /* When something specific to this has occurred */
        void resultCallback(JsonObject result);
        /* Something general everybody should here */
        void generalCallback(JsonObject result);
    }


    /* You can request a packet be sent immediately */
    void onRequest(JsonObject request, LaterCallback callback);


    static JsonElement jsonObjectOf(Object object) {

        if (object instanceof String) {
            return new JsonPrimitive((String) object);
        } else if (object instanceof Integer) {
            return new JsonPrimitive((Integer) object);
        } else if (object instanceof Double) {
            return new JsonPrimitive((Double) object);
        } else if (object instanceof Boolean) {
            return new JsonPrimitive((Boolean) object);
        } else if (object instanceof Float) {
            return new JsonPrimitive((Float) object);
        } else if (object instanceof JsonElement) {
            return (JsonElement) object;
        } else if (object instanceof Map) {
            Map map = (Map) object;
            JsonObject jsonObject = new JsonObject();
            map.forEach((key, value) -> {
                if (!(key instanceof String)) {
                    throw new IllegalArgumentException("Unknown map key type: " + key.getClass());
                }
                jsonObject.add((String) key, jsonObjectOf(value));
            });


            return jsonObject;

        }
        else if (object instanceof Collection) {
            Collection<Object> collection = (Collection<Object>) object;
            JsonArray jsonArray = new JsonArray();
            for (Object o : collection) {
                jsonArray.add(jsonObjectOf(o));
            }
            return jsonArray;
        } else {
            throw new IllegalArgumentException("Unknown value type: " + object.getClass());
        }
    }

    static JsonObject jsonOf(Object... objects) {
        if (objects.length % 2 != 0) {
            throw new IllegalArgumentException("Must have an even number of arguments (key-value pairs)");
        }
        JsonObject jsonObject = new JsonObject();
        for (int i = 0; i < objects.length; i += 2) {
            Object key = objects[i];
            Object value = objects[i+1];
            if (!(key instanceof String)) {
                throw new IllegalArgumentException("Key must be a string");
            }
            jsonObject.add((String) key, jsonObjectOf(value));


        }
        return jsonObject;
    }

}
