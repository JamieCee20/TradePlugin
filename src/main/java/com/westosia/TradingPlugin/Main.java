package com.westosia.TradingPlugin;

import co.aikar.commands.PaperCommandManager;
import com.westosia.TradingPlugin.Listener.TradeListener;
import com.westosia.TradingPlugin.Trading.TradeCommand;
import com.westosia.westosiaapi.utils.Text;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private static Main instance;

    @Override
    public void onEnable() {
//        instance = this;
        TradeListener trader = new TradeListener();
        PaperCommandManager manager = new PaperCommandManager(this);
        manager.registerCommand(new TradeCommand(trader));
        getServer().getPluginManager().registerEvents(trader, this);
        getServer().getConsoleSender().sendMessage(Text.colour("&aTrade Plugin Enabled!"));
    }
}
