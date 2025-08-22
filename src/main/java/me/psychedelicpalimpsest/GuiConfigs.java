/**
 * Copyright (C) 2025 - PsychedelicPalimpsest
 * Copyright (C) 2025 - Tweekeroo developers (Mostly mesa)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.psychedelicpalimpsest;

import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;

import java.util.List;

/*
    I stole this all from tweakeroo.
 */

public class GuiConfigs extends GuiConfigsBase {

	static ConfigGuiTab currentTab = ConfigGuiTab.GENERIC;

	public GuiConfigs() { super(10, 50, McPuppeteer.MOD_ID, null, "Config"); }

	@Override
	public void initGui() {
		super.initGui();
		this.clearOptions();

		int x = 10;
		int y = 26;

		for (ConfigGuiTab tab : ConfigGuiTab.values()) x += this.createButton(x, y, -1, tab);
	}

	@Override
	public List<ConfigOptionWrapper> getConfigs() {
		if (currentTab == ConfigGuiTab.GENERIC) {
			return ConfigOptionWrapper.createFor(PuppeteerConfig.OPTIONS);
		} else if (currentTab == ConfigGuiTab.HOTKEYS) {
			return ConfigOptionWrapper.createFor(PuppeteerConfig.HOTKEY_LIST);
		}

		return List.of();
	}

	private int createButton(int x, int y, int width, ConfigGuiTab tab) {
		ButtonGeneric button = new ButtonGeneric(x, y, width, 20, tab.getDisplayName());
		button.setEnabled(currentTab != tab);
		this.addButton(button, new ButtonListener(tab, this));

		return button.getWidth() + 2;
	}

	private static class ButtonListener implements IButtonActionListener {
		private final GuiConfigs parent;
		private final ConfigGuiTab tab;

		public ButtonListener(ConfigGuiTab tab, GuiConfigs parent) {
			this.tab = tab;
			this.parent = parent;
		}

		@Override
		public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
			GuiConfigs.currentTab = this.tab;
			this.parent.reCreateListWidget(); // apply the new config width
			this.parent.getListWidget().resetScrollbarPosition();
			this.parent.initGui();
		}
	}

	public enum ConfigGuiTab {
		GENERIC("Settings"),
		HOTKEYS("Hotkeys");

		private final String name;

		ConfigGuiTab(String name) { this.name = name; }

		public String getDisplayName() { return this.name; }
	}
}
