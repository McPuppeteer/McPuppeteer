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

package me.psychedelicpalimpsest.commands.inventory;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import me.psychedelicpalimpsest.reflection.McReflector;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;

@PuppeteerCommand(
    cmd = "get trades", description = "Gets the trades of the current merchant",
    cmd_context = BaseCommand.CommandContext.PLAY)
public class GetTrades implements BaseCommand {
	@Override
	public void onRequest(JsonObject request, LaterCallback callback) {
		if (!(MinecraftClient.getInstance().currentScreen instanceof MerchantScreen)) {
			callback.resultCallback(BaseCommand.jsonOf(
			    "status", "error",
			    "type", "unexpected screen",
			    "message", "No MerchantScreen is open"));
			return;
		}

		callback.resultCallback(BaseCommand.jsonOf(
		    "trades", getTrades()));
	}

	private static JsonObject tradeItemToJson(TradedItem item) {
		return BaseCommand.jsonOf(
		    "count", item.count(),
		    "item stack", McReflector.serializeObject(item.itemStack()));
	}

	public static JsonArray getTrades() {
		JsonArray jsonArray = new JsonArray();
		var handler = (MerchantScreenHandler) MinecraftClient.getInstance().player.currentScreenHandler;
		for (TradeOffer offer : handler.getRecipes()) {
			jsonArray.add(BaseCommand.jsonOf(
			    "first item", tradeItemToJson(offer.getFirstBuyItem()),
			    "second item", offer.getSecondBuyItem().isEmpty() ? JsonNull.INSTANCE : tradeItemToJson(offer.getSecondBuyItem().get()),
			    "sell item", McReflector.serializeObject(offer.getSellItem()),
			    "uses", offer.getUses(),
			    "max uses", offer.getMaxUses(),
			    "gives xp", offer.shouldRewardPlayerExperience(),
			    "special price", offer.getSpecialPrice(),
			    "demand bonus", offer.getDemandBonus(),
			    "price multiplier", offer.getPriceMultiplier(),
			    "merchant xp", offer.getMerchantExperience()));
		}
		return jsonArray;
	}
}
