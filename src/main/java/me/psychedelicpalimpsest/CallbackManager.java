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
	public enum PacketCallbackMode {
		DISABLED,

		NOTIFY_ONLY,
		NOTIFY_NEXT,

		NETWORK_SERIALIZED,
		NETWORK_SERIALIZED_NEXT,

		OBJECT_SERIALIZED,
		OBJECT_SERIALIZED_NEXT,
	}

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

		OPEN_SCREEN,
		SET_CONTAINER_CONTENTS,
		SET_CONTAINER_PROPERTIES,
		CLOSE_CONTAINER,
		SET_CURSOR_ITEM,
	}
	public static final Set<String> CALLBACK_STRING_TYPES;

	public static final Set<String> PACKET_LIST = new HashSet<>();

	static {
		Set<String> types = new HashSet<>();
		for (CallbackType type : CallbackType.values()) { types.add(type.name()); }
		CALLBACK_STRING_TYPES = Collections.unmodifiableSet(types);

		PlayStateFactories.S2C.buildUnbound().forEachPacketType(
		    (type, packet) -> { PACKET_LIST.add(type.toString()); });
		PlayStateFactories.C2S.buildUnbound().forEachPacketType(
		    (type, packet) -> { PACKET_LIST.add(type.toString()); });
	}
}
