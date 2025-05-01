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


    public GuiConfigs() {
        super(10, 50, McPuppeteer.MOD_ID, null, "Config");
    }
    @Override
    public void initGui()
    {
        super.initGui();
        this.clearOptions();

        int x = 10;
        int y = 26;

        for (ConfigGuiTab tab : ConfigGuiTab.values())
            x += this.createButton(x, y, -1, tab);
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

        public ButtonListener(ConfigGuiTab tab, GuiConfigs parent)
        {
            this.tab = tab;
            this.parent = parent;
        }

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton)
        {
            GuiConfigs.currentTab = this.tab;
            this.parent.reCreateListWidget(); // apply the new config width
            this.parent.getListWidget().resetScrollbarPosition();
            this.parent.initGui();
        }
    }



    public enum ConfigGuiTab {
        GENERIC         ("Settings"),
        HOTKEYS           ("Hotkeys");


        private final String name;

        ConfigGuiTab(String name) {
            this.name = name;
        }

        public String getDisplayName() {
            return this.name;
        }
    }

}
