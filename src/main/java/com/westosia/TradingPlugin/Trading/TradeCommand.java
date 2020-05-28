package com.westosia.TradingPlugin.Trading;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import com.westosia.TradingPlugin.Listener.TradeListener;
import com.westosia.westosiaapi.WestosiaAPI;
import com.westosia.westosiaapi.api.Notifier;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

@CommandAlias("trade")
@CommandPermission("essentials.command.trade")
public class TradeCommand extends BaseCommand {

    HashMap<Player, Player> requestTrade = new HashMap<Player, Player>();
    TradeListener tradeList;

    public TradeCommand(TradeListener listener) {
        tradeList = listener;
    }

    @Default
    @Description("creates a trade request or accept a trade request")
    public void trade(Player player, String[] args) {
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("request")) {
                Player tradeWith = Bukkit.getPlayer(args[1]);
                if (tradeWith != player) {
                    if (Bukkit.getOnlinePlayers().contains(tradeWith)) {
                        WestosiaAPI.getNotifier().sendChatMessage(player, Notifier.NotifyStatus.SUCCESS, "Trade request has been sent to " + args[1]);
                        requestTrade.put(tradeWith, player);
                        WestosiaAPI.getNotifier().sendChatMessage(tradeWith, Notifier.NotifyStatus.INFO, "You have a trade request from " + player.getName());
                    } else {
                        WestosiaAPI.getNotifier().sendChatMessage(player, Notifier.NotifyStatus.ERROR, "Player requested is not online.");
                    }
                } else {
                    WestosiaAPI.getNotifier().sendChatMessage(player, Notifier.NotifyStatus.ERROR, "Cannot trade with yourself");
                }
            }
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("accept")) {
                if (requestTrade.containsKey(player)) {
                    Player tradeWith = requestTrade.get(player);
                    if (Bukkit.getOnlinePlayers().contains(tradeWith)) {
                        Inventory tradeInv = Bukkit.createInventory(null, 27, "TRADE INVENTORY");

                        ItemStack glass = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
                        ItemStack button = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                        tradeInv.setItem(9, glass);
                        tradeInv.setItem(10, glass);
                        tradeInv.setItem(11, glass);
                        tradeInv.setItem(12, glass);
                        tradeInv.setItem(13, glass);
                        tradeInv.setItem(14, glass);
                        tradeInv.setItem(15, glass);
                        tradeInv.setItem(16, glass);
                        tradeInv.setItem(17, button);

                        player.openInventory(tradeInv);
                        tradeWith.openInventory(tradeInv);
                        requestTrade.remove(player);
                        tradeList.addPlayersToTradelist(player, tradeWith);
                    } else {
                        WestosiaAPI.getNotifier().sendChatMessage(player, Notifier.NotifyStatus.ERROR, "Player isn't online anymore");
                        requestTrade.remove(player);
                    }
                } else {
                    WestosiaAPI.getNotifier().sendChatMessage(player, Notifier.NotifyStatus.ERROR, "No active trade requests");
                }
            } else if(args[0].equalsIgnoreCase("decline")) {
                if (requestTrade.containsKey(player)) {
                    Player tradeWith = requestTrade.get(player);
                    WestosiaAPI.getNotifier().sendChatMessage(tradeWith, Notifier.NotifyStatus.ERROR, "Player declined your trade request");
                    requestTrade.remove(player);
                }
            }
        }
    }
}
