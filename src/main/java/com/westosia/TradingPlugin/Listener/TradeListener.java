package com.westosia.TradingPlugin.Listener;

import com.westosia.westosiaapi.WestosiaAPI;
import com.westosia.westosiaapi.api.Notifier;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.HashMap;
import java.util.List;

public class TradeListener implements Listener {
    public HashMap<Player, Player> tradingPlayers = new HashMap<Player, Player>();

    public void addPlayersToTradelist(Player p1, Player p2) {
        tradingPlayers.put(p1, p2);
    }

    @EventHandler
    public void onPlayerInventoryClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals("TRADE INVENTORY")) {
            Player p = (Player) e.getWhoClicked();
            if (tradingPlayers.containsKey(p)) {
                //Player 1
                if (e.getRawSlot() <= 8 || e.getRawSlot() == 17 || e.getRawSlot() >= 27) {
                    if (e.getRawSlot() == 17) {
                        if (e.getCurrentItem() != null || e.getCurrentItem().getType() != Material.AIR) {
                            accept(p, e.getCurrentItem());
                            e.setCancelled(true);
                        } else {
                            e.setCancelled(true);
                            p.closeInventory();
                        }
                    }
                } else {
                    e.setCancelled(true);
                }
            } else {
                //Player 2
                if (e.getRawSlot() >= 17) {
                    if (e.getRawSlot() == 17) {
                        if (e.getCurrentItem() != null || e.getCurrentItem().getType() != Material.AIR) {
                            accept(p, e.getCurrentItem());
                            e.setCancelled(true);
                        } else {
                            e.setCancelled(true);
                            p.closeInventory();
                        }
                    }
                } else if (e.getClick().isShiftClick()){
                    e.setCancelled(true);
                } else {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (e.getView().getTitle().equals("TRADE INVENTORY")) {
            Player p = (Player) e.getPlayer();
            for (HumanEntity viewer : e.getInventory().getViewers()) {
                if (tradingPlayers.containsKey((Player) viewer) || tradingPlayers.containsKey(p)) {
                    if (viewer.getUniqueId() != p.getUniqueId()) {
                        viewer.closeInventory();
                    }
                    WestosiaAPI.getNotifier().sendChatMessage((Player) viewer, Notifier.NotifyStatus.ERROR, "Trade Closed!");
                }
            }
        }
    }

    public void accept(Player p, ItemStack item) {
        if (item.getType().equals(Material.RED_STAINED_GLASS_PANE)) {
            item.setType(Material.GREEN_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(p.getName());
            item.setItemMeta(meta);


        } else if (item.getType().equals(Material.GREEN_STAINED_GLASS_PANE)) {
            if (item.getItemMeta().getDisplayName().equals(p.getName())) {
                item.setType(Material.RED_STAINED_GLASS_PANE);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(null);
                item.setItemMeta(meta);
            } else {
                completeTrade(p.getOpenInventory().getTopInventory());
            }
        }
    }

    public void completeTrade(Inventory inv) {
        List<HumanEntity> viewers = inv.getViewers();
        Player p1;
        Player p2;

        if (tradingPlayers.containsKey((Player) viewers.get(0))) {
            p1 = (Player) viewers.get(0);
            p2 = (Player) viewers.get(1);
        } else {
            p1 = (Player) viewers.get(1);
            p2 = (Player) viewers.get(0);
        }
        p1.closeInventory();
        p2.closeInventory();
        for (int i = 0; i < 9; i++) {
            if (inv.getItem(i) != null) {
                p2.getInventory().addItem(inv.getItem(i));
            }
            if (inv.getItem(i + 18) != null) {
                p1.getInventory().addItem(inv.getItem(i + 18));
            }
        }
        tradingPlayers.remove(p1);
        WestosiaAPI.getNotifier().sendChatMessage(p1, Notifier.NotifyStatus.SUCCESS, "Trade Complete");
        WestosiaAPI.getNotifier().sendChatMessage(p2, Notifier.NotifyStatus.SUCCESS, "Trade Complete");

    }
}
