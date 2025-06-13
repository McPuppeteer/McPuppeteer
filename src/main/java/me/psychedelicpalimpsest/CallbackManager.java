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

import net.minecraft.network.state.PlayStateFactories;

import java.util.*;

public class CallbackManager {
    public enum CallbackType {
        FORCED,

        BARITONE,

        PLAYER_POSITION,
        PLAYER_YAW,
        PLAYER_PITCH,
        PLAYER_DAMAGE,
        PLAYER_DEATH,
        PLAYER_INVENTORY,
        CHAT,
    }


    public static final Map<CallbackType, String> CALLBACK_TYPE_STRING_MAP;
    public static final Map<String, CallbackType> CALLBACK_STRING_TYPE_MAP;

    public static final Set<String> PACKET_LIST = new HashSet<>();

    static {
        Map<CallbackType, String> map = new HashMap<>(CallbackType.values().length);

        map.put(CallbackType.BARITONE, "BARITONE");

        map.put(CallbackType.PLAYER_POSITION, "PLAYER_POSITION");
        map.put(CallbackType.PLAYER_YAW, "PLAYER_YAW");
        map.put(CallbackType.PLAYER_PITCH, "PLAYER_PITCH");
        map.put(CallbackType.PLAYER_DAMAGE, "PLAYER_DAMAGE");
        map.put(CallbackType.PLAYER_DEATH, "PLAYER_DEATH");
        map.put(CallbackType.PLAYER_INVENTORY, "PLAYER_INVENTORY");
        map.put(CallbackType.CHAT, "CHAT");


        CALLBACK_TYPE_STRING_MAP = Collections.unmodifiableMap(map);

        Map<String, CallbackType> map2 = new HashMap<>(map.size());
        map.forEach((k, v) -> map2.put(v, k));
        CALLBACK_STRING_TYPE_MAP = Collections.unmodifiableMap(map2);

        PlayStateFactories.S2C.forEachPacketType((type, packet) -> {
            PACKET_LIST.add(type.toString());
        });
        PlayStateFactories.C2S.forEachPacketType((type, packet) -> {
            PACKET_LIST.add(type.toString());
        });
    }


}
