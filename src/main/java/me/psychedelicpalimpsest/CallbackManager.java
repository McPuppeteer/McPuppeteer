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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CallbackManager {
    public enum CallbackType {
        FORCED,

        BARITONE,

        PLAYER_POSITION,
        PLAYER_HEALTH,
        PLAYER_DEATH,
        PLAYER_INVENTORY,
    }

    public static final Map<CallbackType, String> CALLBACK_TYPE_STRING_MAP;
    public static final Map<String, CallbackType> CALLBACK_STRING_TYPE_MAP;
    static {
        Map<CallbackType, String> map = new HashMap<>(CallbackType.values().length);

        map.put(CallbackType.BARITONE, "BARITONE");

        map.put(CallbackType.PLAYER_POSITION, "PLAYER_POSITION");
        map.put(CallbackType.PLAYER_HEALTH, "PLAYER_HEALTH");
        map.put(CallbackType.PLAYER_DEATH, "PLAYER_DEATH");
        map.put(CallbackType.PLAYER_INVENTORY, "PLAYER_INVENTORY");




        CALLBACK_TYPE_STRING_MAP = Collections.unmodifiableMap(map);

        Map<String, CallbackType> map2 = new HashMap<>(map.size());
        map.forEach((k, v) -> map2.put(v, k));
        CALLBACK_STRING_TYPE_MAP = Collections.unmodifiableMap(map2);
    }














}
