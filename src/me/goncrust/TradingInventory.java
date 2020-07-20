package me.goncrust;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.base.Strings;

public class TradingInventory {
	
	Inventory inv;
	
	ItemStack confirmed;
	ItemStack notConfirmed;
	ItemStack pConfirm;
	
	Player p1;
	Player p2;
	
	String titleP1;
	String titleP2;
	String title;
	int titleSize = 19;
	
	public TradingInventory(Player p) {
		p1 = p;
		titleP1 = p.getDisplayName();
		titleP2 = "Empty";
		title = getCorrectSpacing(titleP1, titleP2);
		createTradeInventory(p);
	}
	
	public void createTradeInventory(Player p) {
		inv = Bukkit.createInventory(null, 27, title);
		
		// Items
		confirmed = new ItemStack(Material.LIME_CONCRETE, 1);
		ItemMeta confirmedMeta = confirmed.getItemMeta();
		confirmedMeta.setDisplayName("CONFIRMED");
		confirmed.setItemMeta(confirmedMeta);
		
		notConfirmed = new ItemStack(Material.RED_CONCRETE, 1);
		ItemMeta notConfirmedMeta = notConfirmed.getItemMeta();
		notConfirmedMeta.setDisplayName("NOT CONFIRMED");
		notConfirmed.setItemMeta(notConfirmedMeta);
		
		pConfirm = new ItemStack(Material.NETHER_STAR, 1);
		ItemMeta pConfirmedStatusMeta = pConfirm.getItemMeta();
		pConfirmedStatusMeta.setDisplayName("CONFIRMED");
		pConfirm.setItemMeta(pConfirmedStatusMeta);
		
		inv.setItem(4, notConfirmed);
		inv.setItem(13, notConfirmed);
		inv.setItem(22, pConfirm);
		p.openInventory(inv);
	}
	
	public void enterRunningTrade(Player p) {
		if (p != p1 && p != p2) {
			if (p1 == null) {
				p1 = p;
				titleP1 = p.getDisplayName();
				refreshTitles();
			} else if (p2 == null) {
				p2 = p;
				titleP2 = p.getDisplayName();
				refreshTitles();
			}
		}
	}
	
	public boolean isPlayerEmpty() {
		if (p1 == null && p2 == null) {
			return true;
		} else {
			return false;
		}
	}
	
	//Align Right Not working YET
	public String getCorrectSpacing(String firstName, String lastName) {
		int lastNamePosition = titleSize - firstName.length();
		
		return firstName + Strings.repeat(" ", lastNamePosition) + lastName;
	}
	
	public void refreshTitles() {
		title = getCorrectSpacing(titleP1, titleP2);
		Inventory oldCopy = this.inv;
		inv = Bukkit.createInventory(null, 27, title);
		inv.setContents(oldCopy.getContents());
		
		if (p1 != null) {
			p1.closeInventory();
			p1.openInventory(inv);
		}
		
		if (p2 != null) {
			p2.closeInventory();
			p2.openInventory(inv);
		}
	}
	
	public void clickPConfirm(int player) {
		if (player == 1) {
			if (inv.getItem(4).equals(notConfirmed)) {
				inv.setItem(4, confirmed);
			} else if (inv.getItem(4).equals(confirmed)) {
				inv.setItem(4, notConfirmed);
			}
		} else if (player == 2) {
			if (inv.getItem(13).equals(notConfirmed)) {
				inv.setItem(13, confirmed);
			} else if (inv.getItem(13).equals(confirmed)) {
				inv.setItem(13, notConfirmed);
			}
		}
		
		if (inv.getItem(4).equals(confirmed) && inv.getItem(13).equals(confirmed)) {
			transferItems();
		}
	}
	
	public void transferItems() {
		List<Integer> P1Slots = EventListener.P2CantDragSlots;
		List<Integer> P2Slots = EventListener.P1CantDragSlots;
		
		List<ItemStack> P1inv = new ArrayList<ItemStack>();
		List<ItemStack> P2inv = new ArrayList<ItemStack>();
		
		for (int i = 0; i < P1Slots.size(); i++) {
			P1inv.add(inv.getContents()[P1Slots.get(i)]);
		}
		
		for (int i = 0; i < P2Slots.size(); i++) {
			P2inv.add(inv.getContents()[P2Slots.get(i)]);
		}
		
		for (int i = 0; i < P2Slots.size(); i++) {
			inv.setItem(P2Slots.get(i), P1inv.get(i));
		}
		
		for (int i = 0; i < P1Slots.size(); i++) {
			inv.setItem(P1Slots.get(i), P2inv.get(i));
		}
		
		resetConfirms();
	}
	
	public void retrieveItems(int player) {
		List<Integer> P1Slots = EventListener.P2CantDragSlots;
		List<Integer> P2Slots = EventListener.P1CantDragSlots;
		
		if (player == 1) {
			for (int i = 0; i < P1Slots.size(); i++) {
				if (inv.getContents()[P1Slots.get(i)] != null) {
					if (invFull(1)) {
						p1.getWorld().dropItem(p1.getLocation(), inv.getContents()[P1Slots.get(i)]);
					} else {
						p1.getInventory().addItem(inv.getContents()[P1Slots.get(i)]);
					}
					inv.setItem(P1Slots.get(i), null);
				}
			}
		} else if (player == 2) {
			for (int i = 0; i < P2Slots.size(); i++) {
				if (inv.getContents()[P2Slots.get(i)] != null) {
					if (invFull(2)) {
						p2.getWorld().dropItem(p2.getLocation(), inv.getContents()[P2Slots.get(i)]);
					} else {
						p2.getInventory().addItem(inv.getContents()[P2Slots.get(i)]);
					}
					inv.setItem(P2Slots.get(i), null);
				}
			}
		}
	}
	
	public boolean invFull(int player) {
		if (player == 1) {
			for (ItemStack i : p1.getInventory().getContents()) {
				if (i == null) {
					return false;
				} else {
					return true;
				}
			}
		} else if (player == 2) {
			for (ItemStack i : p2.getInventory().getContents()) {
				if (i == null) {
					return false;
				} else {
					return true;
				}
			}
		}
		return true;
	}
	
	public void resetConfirms() {
		inv.setItem(4, notConfirmed);
		inv.setItem(13, notConfirmed);
	}
	
	public boolean checkIfP1Confirmed() {
		if (inv.getItem(4).equals(confirmed)) {
			return true;
		}
		return false;
	}
	
	public boolean checkIfP2Confirmed() {
		if (inv.getItem(13).equals(confirmed)) {
			return true;
		}
		return false;
	}
 	
	public Inventory getInv() {
		return inv;
	}
	
	public Player getP1() {
		return p1;
	}
	
	public Player getP2() {
		return p2;
	}
	
	public void setP1(Player p1) {
		this.p1 = p1; 
	}
	
	public void setP2(Player p2) {
		this.p2 = p2;
	}
	
	public void setTitleP1(String name) {
		titleP1 = name;
	}
	
	public void setTitleP2(String name) {
		titleP2 = name;
	}

}
