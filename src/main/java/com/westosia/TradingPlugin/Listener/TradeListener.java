package com.westosia.TradingPlugin.Listener;

import com.westosia.westosiaapi.Main;
import com.westosia.westosiaapi.WestosiaAPI;
import com.westosia.westosiaapi.api.Notifier;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TradeListener implements Listener {
    public HashMap<Player, Player> tradingPlayers = new HashMap<Player, Player>();

    private boolean accept1, accept2;
    private final Plugin plugin = Main.getPlugin(Main.class);
    Player p1;
    Player p2;

    public void addPlayersToTradelist(Player p1, Player p2) {
        tradingPlayers.put(p1, p2);
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInventoryClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals("TRADE INVENTORY")) {
            Player p = (Player) e.getWhoClicked();
            if (tradingPlayers.containsKey(p)) {
                if (!e.getClick().isShiftClick()) {
                    //Player 1
                    if (e.getRawSlot() <= 8 || e.getRawSlot() == 17 || e.getRawSlot() >= 27) {
                        if (e.getRawSlot() == 17) {
                            if (e.getCurrentItem() != null || e.getCurrentItem().getType() != Material.AIR) {
                                accept(p, e.getCurrentItem());
                                e.setCancelled(true);
                            } else {
                                e.setCancelled(true);
                                p.closeInventory();
                                WestosiaAPI.getNotifier().sendChatMessage(p, Notifier.NotifyStatus.ERROR, "Trade Closed!");
                            }
                        }
                    } else {
                        e.setCancelled(true);
                    }
                } else if (e.getClick().isLeftClick()) {
                    e.setCancelled(true);
                    WestosiaAPI.getNotifier().sendChatMessage(p, Notifier.NotifyStatus.ERROR, "Do not left click!");
                } else {
                    e.setCancelled(true);
                    WestosiaAPI.getNotifier().sendChatMessage(p, Notifier.NotifyStatus.ERROR, "Do not shift click!");
                }
            } else {
                if (!e.getClick().isShiftClick()) {
                    //Player 2
                    if (e.getRawSlot() >= 17) {
                        if (e.getRawSlot() == 17) {
                            if (e.getCurrentItem() != null || e.getCurrentItem().getType() != Material.AIR) {
                                accept(p, e.getCurrentItem());
                                e.setCancelled(true);
                            } else {
                                e.setCancelled(true);
                                p.closeInventory();
                                WestosiaAPI.getNotifier().sendChatMessage(p, Notifier.NotifyStatus.ERROR, "Trade Closed!");
                            }
                        }
                    } else {
                        e.setCancelled(true);
                    }
                } else if (e.getClick().isRightClick()) {
                    e.setCancelled(true);
                    WestosiaAPI.getNotifier().sendChatMessage(p, Notifier.NotifyStatus.ERROR, "Do not right click!");
                } else {
                    e.setCancelled(true);
                    WestosiaAPI.getNotifier().sendChatMessage(p, Notifier.NotifyStatus.ERROR, "Do not shift click!");
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) e.getWhoClicked();
        Inventory inv = e.getInventory();
        if (inv.getViewers().contains(player)) {
            if (accept1 == true && accept2 == true) {
                for (int slots : e.getInventorySlots()) {
                    if (e.getInventorySlots().size() > 0 ) {
                        if ((player.equals(p1) && accept1) || (player.equals(p2) && accept2) && plugin.getConfig().getBoolean("antiscam.preventchangeonaccept")) {
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
            } else {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (e.getView().getTitle().equals("TRADE INVENTORY")) {
            Player closed = (Player) e.getPlayer();

            // Trade is still active -- find who we need to remove from tradingPlayers to declare the end of the trade
            // (the key of the HashMap, which is the player who accepted the trade)
            if (tradingPlayers.containsKey(closed) || tradingPlayers.containsValue(closed)) {
                // Initialise the variable. This is the key of the HashMap
                Player accepted = closed;

                // Get a copy of the viewers for this event (which should contain the initiator and accepter)
                // We need a copy because we need to loop through this whilst editing it (closing inv = removing viewers)
                final List<HumanEntity> viewersCopy = new ArrayList<>(e.getViewers());

                // Uh oh! Wrong player selected as 'accepted'
                if (!tradingPlayers.containsKey(accepted)) {
                    for (HumanEntity viewer : viewersCopy) {
                        // Looping through the viewers. Since there should only be two people viewing, find the person
                        // we don't have as 'accepted,' and stop searching once we do
                        if (!viewer.getUniqueId().equals(accepted.getUniqueId())) {
                            accepted = (Player) viewer;
                            break;
                        }
                    }
                }
                // Declare trade inactive. Do this before closing other's inventories so the events from those will not
                // meet the criteria for line 73, causing the infinite close loop
                tradingPlayers.remove(accepted);

                // Close inventory for all viewers
                for (HumanEntity viewer : viewersCopy) {
                    // Don't close the inventory of the person who caused this event
                    //TODO: logic for giving items back. Good luck!
                    if (viewer.getUniqueId().equals(accepted.getUniqueId())) {
                        // Return Items to player who accepted the trade
                        Inventory acceptedInv = accepted.getInventory();
                        Inventory inv = accepted.getOpenInventory().getTopInventory();
                        for (int i = 0; i < 9; i++) {
                            if (inv.getItem(i) != null) {
                                acceptedInv.addItem(inv.getItem(i));
                            }
                        }
                    } else {
                        // Return Items to player who requested the trade*
                        for (int i = 0; i < 9; i++) {
                            if (viewer.getOpenInventory().getTopInventory().getItem(i + 18) != null) {
                                viewer.getInventory().addItem(viewer.getOpenInventory().getTopInventory().getItem(i + 18));
                            }
                        }
                    }
                    if (!closed.getUniqueId().equals(viewer.getUniqueId())) {
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
            accept1 = true;


        } else if (item.getType().equals(Material.GREEN_STAINED_GLASS_PANE)) {
            if (item.getItemMeta().getDisplayName().equals(p.getName())) {
                item.setType(Material.RED_STAINED_GLASS_PANE);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(null);
                item.setItemMeta(meta);
            } else {
                accept2 = true;
                completeTrade(p.getOpenInventory().getTopInventory());
            }
        }
    }

    public void completeTrade(Inventory inv) {
        List<HumanEntity> viewers = inv.getViewers();

        if (tradingPlayers.containsKey((Player) viewers.get(0))) {
            p1 = (Player) viewers.get(0);
            p2 = (Player) viewers.get(1);
        } else {
            p1 = (Player) viewers.get(1);
            p2 = (Player) viewers.get(0);
        }
        for (int i = 0; i < 9; i++) {
            if (inv.getItem(i) != null) {
                p2.getInventory().addItem(inv.getItem(i));
            }
            if (inv.getItem(i + 18) != null) {
                p1.getInventory().addItem(inv.getItem(i + 18));
            }
        }
        tradingPlayers.remove(p1);
        tradingPlayers.remove(p2);
        p1.closeInventory();
        p2.closeInventory();
        WestosiaAPI.getNotifier().sendChatMessage(p1, Notifier.NotifyStatus.SUCCESS, "Trade Complete");
        WestosiaAPI.getNotifier().sendChatMessage(p2, Notifier.NotifyStatus.SUCCESS, "Trade Complete");

    }
}
