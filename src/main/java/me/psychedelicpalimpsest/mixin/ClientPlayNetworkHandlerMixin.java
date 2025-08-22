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

package me.psychedelicpalimpsest.mixin;

import me.psychedelicpalimpsest.CallbackManager;
import me.psychedelicpalimpsest.PuppeteerServer;
import me.psychedelicpalimpsest.reflection.McReflector;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.psychedelicpalimpsest.BaseCommand.jsonOf;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@Inject(method = "onInventory", at = @At("HEAD"))
	void onInventory(InventoryS2CPacket packet, CallbackInfo ci) {

		PuppeteerServer.broadcastJsonPacket(CallbackManager.CallbackType.SET_CONTAINER_CONTENTS,
						    () -> jsonOf("data", McReflector.serializeObject(packet)));
	}

	@Inject(method = "onSetPlayerInventory", at = @At("HEAD"))
	void onSetPlayerInventory(SetPlayerInventoryS2CPacket packet, CallbackInfo ci) {

		PuppeteerServer.broadcastJsonPacket(CallbackManager.CallbackType.PLAYER_INVENTORY,
						    () -> jsonOf("data", McReflector.serializeObject(packet)));
	}

	@Inject(method = "onCloseScreen", at = @At("HEAD"))
	void onClose(CloseScreenS2CPacket packet, CallbackInfo ci) {

		PuppeteerServer.broadcastJsonPacket(CallbackManager.CallbackType.CLOSE_CONTAINER,
						    () -> jsonOf("data", McReflector.serializeObject(packet)));
	}

	@Inject(method = "onOpenScreen", at = @At("HEAD"))
	void onOpenScreen(OpenScreenS2CPacket packet, CallbackInfo ci) {
		PuppeteerServer.broadcastJsonPacket(CallbackManager.CallbackType.OPEN_SCREEN,
						    () -> jsonOf("data", McReflector.serializeObject(packet)));
	}

	@Inject(method = "onSetCursorItem", at = @At("HEAD"))
	void onSetCursorItem(SetCursorItemS2CPacket packet, CallbackInfo ci) {

		PuppeteerServer.broadcastJsonPacket(CallbackManager.CallbackType.SET_CURSOR_ITEM,
						    () -> jsonOf("data", McReflector.serializeObject(packet)));
	}

	@Inject(method = "onScreenHandlerPropertyUpdate", at = @At("HEAD"))
	void onScreenHandlerPropertyUpdate(ScreenHandlerPropertyUpdateS2CPacket packet, CallbackInfo ci) {
		PuppeteerServer.broadcastJsonPacket(CallbackManager.CallbackType.SET_CONTAINER_PROPERTIES,
						    () -> jsonOf("data", McReflector.serializeObject(packet)));
	}
}
