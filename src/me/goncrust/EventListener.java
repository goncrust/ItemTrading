package me.goncrust;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.help.HelpTopicComparator.TopicNameComparator;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Range;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class EventListener implements Listener {
	
	public static Map<Location, TradingInventory> trackTrades = new HashMap();
	public static List<Integer> P1CantDragSlots = new ArrayList<Integer>();
	public static List<Integer> P2CantDragSlots = new ArrayList<Integer>();
	
	public EventListener() {
		createLists();
	}
	
	@EventHandler
	public void onClickTradingBlock(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (e.getClickedBlock().getType() == Material.OBSIDIAN) {
				e.setCancelled(true);
				
				//Variables
				Location tradeLocation = e.getClickedBlock().getLocation();
				
				//Debug
				Bukkit.getServer().getConsoleSender().sendMessage("Inicial: " + tradeLocation + "  ||  " + trackTrades.get(tradeLocation));
				
				//Code
				if (!trackTrades.containsKey(tradeLocation)) {
					trackTrades.put(tradeLocation, new TradingInventory(e.getPlayer()));
				} else {
					trackTrades.get(tradeLocation).enterRunningTrade(e.getPlayer());
				}
				
				//Debug
				Bukkit.getServer().getConsoleSender().sendMessage(tradeLocation + "  ||  " + trackTrades.get(tradeLocation));
			}
		}
	}
	
	@EventHandler
	public void clickTradeInventory(InventoryClickEvent e) {
		if (isTradeInventory(e.getInventory())) {
			if (e.getClickedInventory() == e.getView().getTopInventory()) {
				if (e.getSlot() == 4 || e.getSlot() == 13) {
					e.setCancelled(true);
				} else if (e.getSlot() == 22) {
					e.setCancelled(true);
					for (Map.Entry<Location, TradingInventory> entry : trackTrades.entrySet()) {
						Location loc = entry.getKey();
						TradingInventory inv = entry.getValue();

						if (e.getInventory() == inv.getInv()) {
							if (e.getWhoClicked() == getP1fromInv(e.getInventory())) {
								inv.clickPConfirm(1);
							} else if (e.getWhoClicked() == getP2fromInv(e.getInventory())) {
								inv.clickPConfirm(2);
							}
						}
					}
				} else if (e.getWhoClicked() == getP2fromInv(e.getInventory())) {
					if ((e.getSlot() >= 0 && e.getSlot() < 4) || (e.getSlot() >= 9 && e.getSlot() < 13) || (e.getSlot() >= 18 && e.getSlot() < 22)) {
						e.setCancelled(true);
					}
				} else if (e.getWhoClicked() == getP1fromInv(e.getInventory())) {
					if ((e.getSlot() >= 5 && e.getSlot() < 9) || (e.getSlot() >= 14 && e.getSlot() < 18) || (e.getSlot() >= 23 && e.getSlot() <= 26)) {
						e.setCancelled(true);
					}
				} 

				for (Map.Entry<Location, TradingInventory> entry : trackTrades.entrySet()) {
					Location loc = entry.getKey();
					TradingInventory inv = entry.getValue();
					
					if (e.getInventory() == inv.getInv()) {
						if (e.getWhoClicked() == getP1fromInv(e.getInventory())) {
							if (inv.checkIfP1Confirmed()) {
								e.setCancelled(true);
							}
						} else if (e.getWhoClicked() == getP2fromInv(e.getInventory())) {
							if (inv.checkIfP2Confirmed()) {
								e.setCancelled(true);
							}
						}
					}
				}
				
			} else {
				if (e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT) {
					
					List<ItemStack> items = new ArrayList<ItemStack>(9*3);
					for (ItemStack item : e.getView().getTopInventory().getContents()) {
						items.add(item);
					}
					
					if (e.getWhoClicked() == getP1fromInv(e.getInventory())) {
						e.setCancelled(true);
						
						for (int i = 0; i < P2CantDragSlots.size(); i++) {
							if(items.get(P2CantDragSlots.get(i)) == null) {
								ItemStack item = e.getCurrentItem();
								e.getWhoClicked().getInventory().setItem(e.getSlot(), null);
								e.getView().getTopInventory().setItem(P2CantDragSlots.get(i), item);
								break;
							} 
						}
					} else if (e.getWhoClicked() == getP2fromInv(e.getInventory())) {
						e.setCancelled(true);
						
						for (int i = 0; i < P1CantDragSlots.size(); i ++) {
							if (items.get(P1CantDragSlots.get(i)) == null) {
								ItemStack item = e.getCurrentItem();
								e.getWhoClicked().getInventory().setItem(e.getSlot(), null);
								e.getView().getTopInventory().setItem(P1CantDragSlots.get(i), item);
								break;
							}
						}
					}
				}
			}
			
		}
		
		
		
	}
	
	@EventHandler
	public void dragTradeInventory(InventoryDragEvent e) {
		if (isTradeInventory(e.getInventory())) {
			if (e.getRawSlots().contains(4) || e.getRawSlots().contains(13) || e.getRawSlots().contains(22)) {
				e.setCancelled(true);
			} else if (e.getWhoClicked() == getP2fromInv(e.getInventory())) {
				e.setCancelled(checkDragSlot(2, e.getRawSlots()));
			} else if (e.getWhoClicked() == getP1fromInv(e.getInventory())) {
				e.setCancelled(checkDragSlot(1, e.getRawSlots()));
			}
			
		}
	}
	
	@EventHandler
	public void closeInventory(InventoryCloseEvent e) {
		for (Map.Entry<Location, TradingInventory> entry : trackTrades.entrySet()) {
			Location loc = entry.getKey();
			TradingInventory tradinginv = entry.getValue();
			
			if (tradinginv.getInv() == e.getInventory()) {
				if (e.getPlayer() == tradinginv.getP1()) {
					tradinginv.retrieveItems(1);
					tradinginv.setP1(null);
					tradinginv.setTitleP1("Empty");
					tradinginv.refreshTitles();
				} else if (e.getPlayer() == tradinginv.getP2()) {
					tradinginv.retrieveItems(2);
					tradinginv.setP2(null);
					tradinginv.setTitleP2("Empty");
					tradinginv.refreshTitles();
				}
				
				if (tradinginv.isPlayerEmpty()) {
					trackTrades.remove(loc);
				}
			}
		}
	}
	
	public Player getP1fromInv(Inventory inv) {
		for (Map.Entry<Location, TradingInventory> entry : trackTrades.entrySet()) {
			Location loc = entry.getKey();
			TradingInventory tradinginv = entry.getValue();
			
			if (tradinginv.getInv() == inv) {
				return tradinginv.getP1();
			}
		}
		return null;
	}
	
	public Player getP2fromInv(Inventory inv) {
		for (Map.Entry<Location, TradingInventory> entry : trackTrades.entrySet()) {
			Location loc = entry.getKey();
			TradingInventory tradinginv = entry.getValue();
			
			if (tradinginv.getInv() == inv) {
				return tradinginv.getP2();
			}
		}
		return null;
	}
	
	public boolean checkDragSlot(int player, Set<Integer> draggedSlots) {
		List<Integer> draggedSlotsList = new ArrayList<Integer>(draggedSlots.size());
		for (int i : draggedSlots) {
			draggedSlotsList.add(i);
		}
		
		for (int d = 0; d < draggedSlotsList.size(); d++) {
			int i = draggedSlotsList.get(d);
			if (player == 1) {
				if ((i >= 5 && i < 9) || (i >= 14 && i < 18) || (i >= 23 && i <= 26)) {
					return true;
				}
			} else if (player == 2) {
				if ((i >= 0 && i < 4) || (i >= 9 && i < 13) || (i >= 18 && i < 22)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isTradeInventory(Inventory inv) {
		for (Map.Entry<Location, TradingInventory> entry : trackTrades.entrySet()) {
			Location loc = entry.getKey();
			TradingInventory tradingInv = entry.getValue();
			
			if (tradingInv.getInv() == inv) {
				return true;
			}
		}
		return false;
	}
	
	void createLists() {
		//P1CantDragSlots
		for (int i = 5; i < 9; i++) {
			P1CantDragSlots.add(i);
		}
		
		for (int i = 14; i < 18; i++) {
			P1CantDragSlots.add(i);
		}
		
		for (int i = 23; i <= 26; i++) {
			P1CantDragSlots.add(i);
		}
		
		//P2CantDragSlots
		for (int i = 0; i < 4; i++) {
			P2CantDragSlots.add(i);
		}
		
		for (int i = 9; i < 13; i++) {
			P2CantDragSlots.add(i);
		}
		
		for (int i = 18; i < 22; i++) {
			P2CantDragSlots.add(i);
		}
		
		
	}

}
