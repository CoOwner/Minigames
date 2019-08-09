package com.zyrox.events;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;

public class HostGUI implements Listener {
	
//	@EventHandler
//	public void onInventoryClick(InventoryClickEvent event) {
//		Player player = (Player) event.getWhoClicked();
//		Inventory inventory = event.getInventory();
//		if (inventory != null && inventory.getName().equals(ChatColor.RED + "Events")) {
//			ItemStack clicked = event.getCurrentItem();
//			event.setCancelled(true);
//			if (clicked != null && clicked.getType() != Material.AIR) {
//				if (clicked.getType() == Material.EMERALD) {
//					if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(T.replace("&3Sumo"))) {
//						boolean once = false;
//						boolean broken = false;
//						for (Event e1 : Main.get().getEventManager().getEvents()) {
//							if (!e1.getSimpleName().equalsIgnoreCase("sumo")) {
//								if (!once) {
//									broken = true;
//								}
//							}
//							else {
//								broken = false;
//								if (!broken) {
//									once = true;
//									String mode = "sumo";
//
//									EventManager eman = Main.get().getEventManager();
//
//									for (Event e : eman.getEvents())
//									{
//										if (e.getSimpleName().equalsIgnoreCase(mode))
//										{
//											if (player.hasPermission("events.host." + e.getSimpleName()))
//											{
//												e.initStart(player);
//												player.closeInventory();
//											}
//											else
//											{
//												player.sendMessage(T.noPerm);
//												player.closeInventory();
//											}
//										}
//									}
//								}
//							}
//						}
//						if (broken) {
//							if (!once) {
//								player.sendMessage(T.main(T.replace("&c&lThis event is currently disabled.")));
//								player.closeInventory();
//							}
//						}
//					}
//					if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(T.replace("&3RedRover"))) {
//						boolean once = false;
//						boolean broken = false;
//						for (Event e1 : Main.get().getEventManager().getEvents()) {
//							if (!e1.getSimpleName().equalsIgnoreCase("redrover")) {
//								if (!once) {
//									broken = true;
//								}
//							}
//							else {
//								broken = false;
//								if (!broken) {
//									once = true;
//									String mode = "redrover";
//			
//									EventManager eman = Main.get().getEventManager();
//			
//									for (Event e : eman.getEvents())
//									{
//										if (e.getSimpleName().equalsIgnoreCase(mode))
//										{
//											if (player.hasPermission("events.host." + e.getSimpleName()))
//											{
//												e.initStart(player);
//												player.closeInventory();
//											}
//											else
//											{
//												player.sendMessage(T.noPerm);
//												player.closeInventory();
//											}
//										}
//									}
//								}
//							}
//						}
//						if (broken) {
//							if (!once) {
//								player.sendMessage(T.main(T.replace("&c&lThis event is currently disabled.")));
//								player.closeInventory();
//							}
//						}			
//					}
//					if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(T.replace("&3Brackets"))) {
//						boolean once = false;
//						boolean broken = false;
//						for (Event e1 : Main.get().getEventManager().getEvents()) {
//							if (!e1.getSimpleName().equalsIgnoreCase("brackets")) {
//								if (!once) {
//									broken = true;
//								}
//							}
//							else {
//								broken = false;
//								if (!broken) {
//									once = true;
//									String mode = "brackets";
//			
//									EventManager eman = Main.get().getEventManager();
//			
//									for (Event e : eman.getEvents())
//									{
//										if (e.getSimpleName().equalsIgnoreCase(mode))
//										{
//											if (player.hasPermission("events.host." + e.getSimpleName()))
//											{
//												e.initStart(player);
//												player.closeInventory();
//											}
//											else
//											{
//												player.sendMessage(T.noPerm);
//												player.closeInventory();
//											}
//										}
//									}
//								}
//							}
//						}
//						if (broken) {
//							if (!once) {
//								player.sendMessage(T.main(T.replace("&c&lThis event is currently disabled.")));
//								player.closeInventory();
//							}
//						}
//					}
//					if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(T.replace("&3RoD"))) {
//						boolean once = false;
//						boolean broken = false;
//						for (Event e1 : Main.get().getEventManager().getEvents()) {
//							if (!e1.getSimpleName().equalsIgnoreCase("rod")) {
//								if (!once) {
//									broken = true;
//								}
//							}
//							else {
//								broken = false;
//								if (!broken) {
//									once = true;
//									String mode = "rod";
//			
//									EventManager eman = Main.get().getEventManager();
//			
//									for (Event e : eman.getEvents())
//									{
//										if (e.getSimpleName().equalsIgnoreCase(mode))
//										{
//											if (player.hasPermission("events.host." + e.getSimpleName()))
//											{
//												e.initStart(player);
//												player.closeInventory();
//											}
//											else
//											{
//												player.sendMessage(T.noPerm);
//												player.closeInventory();
//											}
//										}
//									}
//								}
//							}
//						}
//						if (broken) {
//							if (!once) {
//								player.sendMessage(T.main(T.replace("&c&lThis event is currently disabled.")));
//								player.closeInventory();
//							}
//						}
//					}
//					if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(T.replace("&3LMS"))) {
//						boolean once = false;
//						boolean broken = false;
//						for (Event e1 : Main.get().getEventManager().getEvents()) {
//							if (!e1.getSimpleName().equalsIgnoreCase("lms")) {
//								if (!once) {
//									broken = true;
//								}
//							}
//							else {
//								broken = false;
//								if (!broken) {
//									once = true;
//									String mode = "lms";
//			
//									EventManager eman = Main.get().getEventManager();
//			
//									for (Event e : eman.getEvents())
//									{
//										if (e.getSimpleName().equalsIgnoreCase(mode))
//										{
//											if (player.hasPermission("events.host." + e.getSimpleName()))
//											{
//												e.initStart(player);
//												player.closeInventory();
//											}
//											else
//											{
//												player.sendMessage(T.noPerm);
//												player.closeInventory();
//											}
//										}
//									}
//								}
//							}
//						}
//						if (broken) {
//							if (!once) {
//								player.sendMessage(T.main(T.replace("&c&lThis event is currently disabled.")));
//								player.closeInventory();
//							}
//						}
//					}
//					if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(T.replace("&3Maze"))) {
//						boolean once = false;
//						boolean broken = false;
//						for (Event e1 : Main.get().getEventManager().getEvents()) {
//							if (!e1.getSimpleName().equalsIgnoreCase("maze")) {
//								if (!once) {
//									broken = true;
//								}
//							}
//							else {
//								broken = false;
//								if (!broken) {
//									once = true;
//									String mode = "maze";
//			
//									EventManager eman = Main.get().getEventManager();
//			
//									for (Event e : eman.getEvents())
//									{
//										if (e.getSimpleName().equalsIgnoreCase(mode))
//										{
//											if (player.hasPermission("events.host." + e.getSimpleName()))
//											{
//												e.initStart(player);
//												player.closeInventory();
//											}
//											else
//											{
//												player.sendMessage(T.noPerm);
//												player.closeInventory();
//											}
//										}
//									}
//								}
//							}
//						}
//						if (broken) {
//							if (!once) {
//								player.sendMessage(T.main(T.replace("&c&lThis event is currently disabled.")));
//								player.closeInventory();
//							}
//						}
//					}
//					if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(T.replace("&3WaterDrop"))) {
//						boolean once = false;
//						boolean broken = false;
//						for (Event e1 : Main.get().getEventManager().getEvents()) {
//							if (!e1.getSimpleName().equalsIgnoreCase("waterdrop")) {
//								if (!once) {
//									broken = true;
//								}
//							}
//							else {
//								broken = false;
//								if (!broken) {
//									once = true;
//									String mode = "waterdrop";
//			
//									EventManager eman = Main.get().getEventManager();
//			
//									for (Event e : eman.getEvents())
//									{
//										if (e.getSimpleName().equalsIgnoreCase(mode))
//										{
//											if (player.hasPermission("events.host." + e.getSimpleName()))
//											{
//												e.initStart(player);
//												player.closeInventory();
//											}
//											else
//											{
//												player.sendMessage(T.noPerm);
//												player.closeInventory();
//											}
//										}
//									}
//								}
//							}
//						}
//						if (broken) {
//							if (!once) {
//								player.sendMessage(T.main(T.replace("&c&lThis event is currently disabled.")));
//								player.closeInventory();
//							}
//						}
//					}
//					if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(T.replace("&3Nodebuff"))) {
//						boolean once = false;
//						boolean broken = false;
//						for (Event e1 : Main.get().getEventManager().getEvents()) {
//							if (!e1.getSimpleName().equalsIgnoreCase("node")) {
//								if (!once) {
//									broken = true;
//								}
//							}
//							else {
//								broken = false;
//								if (!broken) {
//									once = true;
//									String mode = "node";
//			
//									EventManager eman = Main.get().getEventManager();
//			
//									for (Event e : eman.getEvents())
//									{
//										if (e.getSimpleName().equalsIgnoreCase(mode))
//										{
//											if (player.hasPermission("events.host." + e.getSimpleName()))
//											{
//												e.initStart(player);
//												player.closeInventory();
//											}
//											else
//											{
//												player.sendMessage(T.noPerm);
//												player.closeInventory();
//											}
//										}
//									}
//								}
//							}
//						}
//						if (broken) {
//							if (!once) {
//								player.sendMessage(T.main(T.replace("&c&lThis event is currently disabled.")));
//								player.closeInventory();
//							}
//						}
//					}
//				}
//			}
//		}
//	}
}
