package com.zyrox.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.cloutteam.samjakob.gui.ItemBuilder;
import com.cloutteam.samjakob.gui.buttons.GUIButton;
import com.cloutteam.samjakob.gui.types.PaginatedGUI;

import net.md_5.bungee.api.ChatColor;

public class CommandMain implements CommandExecutor, TabCompleter
{
	
	private static List<Integer> events = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7);
	
	public static HashMap<Player, Inventory> inventoryMap = new HashMap<Player, Inventory>();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (args.length == 0 || args[0].equalsIgnoreCase("help"))
		{
			T.sendMessage(sender, "&eShowing help for &d/event");
			sendCmd(sender, "join", "events.join");
			sendCmd(sender, "spec", "events.spectate");
			sendCmd(sender, "leave", null);
			sendCmd(sender, "host", null);
//			for (Event e : Main.get().getEventManager().getEvents())
//			{
//				sendCmd(sender, "host " + e.getSimpleName(), "events.host." + e.getSimpleName());
//			}
			sendCmd(sender, "reload", "events.admin");
			sendCmd(sender, "stop", "events.admin");
			sendCmd(sender, "kick", "events.admin");
			T.sendMessage(sender, "&e- Showing page &d1 &eof &d1 &e(7 results).");
		}
		else if (args[0].equalsIgnoreCase("join"))
		{
			if (!isPlayer(sender, true))
			{
				return false;
			}

			if (!sender.hasPermission("events.join"))
			{
				sender.sendMessage(T.noPerm);
				return false;
			}

			Event event = Main.get().getEventManager().getActive();
			if (event == null)
			{
				T.sendMessage(sender, "&cThere currently isn't any event active right now.");
				return false;
			}
			Player player = (Player) sender;
			if (event.canJoin(player, true))
			{
				event.addPlayer(player);
			}
		}
		else if (args[0].equalsIgnoreCase("spec") || args[0].equalsIgnoreCase("spectate") || args[0].equalsIgnoreCase("s"))
		{
			if (!isPlayer(sender, true))
			{
				return false;
			}

			if (!sender.hasPermission("events.spectate"))
			{
				sender.sendMessage(T.noPerm);
				return false;
			}

			Event event = Main.get().getEventManager().getActive();
			if (event == null)
			{
				T.sendMessage(sender, "&cThere currently isn't any event active right now.");
				return false;
			}
			Player player = (Player) sender;
			if (event.canSpec(player, true))
			{
				event.addSpec(player);
			}
		}
		else if (args[0].equalsIgnoreCase("leave") || args[0].equalsIgnoreCase("quit"))
		{
			if (!isPlayer(sender, true))
			{
				return false;
			}
			Player player = (Player) sender;
			Event event = Main.get().getEventManager().getActive();
			if (event == null)
			{
				T.sendMessage(sender, "&cThere currently isn't any event active right now.");
				return false;
			}
			if (!event.isPlaying(player) && event.specPlayers.contains(player))
			{
				event.removeSpec(player);
				return true;
			}
			if (!event.isPlaying(player))
			{
				T.sendMessage(sender, "&cYou must be in an event to leave one.");
				return false;
			}
			event.removePlayer(player);
		}
		else if (args[0].equalsIgnoreCase("host")) {
			if (args.length > 0 || args.length == 0) {
				EventManager eman = Main.get().getEventManager();
				if (eman.getActive() != null)
				{
					T.sendMessage(sender, "&cYou're unable to host an event as there is one currently running.");
					return false;
				}
				Player player = (Player) sender;
				PaginatedGUI menu = new PaginatedGUI("Host Event");
//				Inventory inv = setupInventoryEvents(Bukkit.createInventory(null, 27, ChatColor.RED + "Events"), player);
				List<String> sumolore = new ArrayList<String>();
				List<String> rrlore = new ArrayList<String>();
				List<String> bracketslore = new ArrayList<String>();
				List<String> rodlore = new ArrayList<String>();
				List<String> lmslore = new ArrayList<String>();
				List<String> mazelore = new ArrayList<String>();
				List<String> waterdroplore = new ArrayList<String>();
				List<String> nodelore = new ArrayList<String>();
//				player.openInventory(inv);
//				sumolore.add(T.replace("&3Sumo &eis a fast 1v1 in a fair playing &efield."));
//				sumolore.add(T.replace("&7[&3Click here to Host Sumo 1v1's&7]"));
//				bracketslore.add(T.replace("&3Brackets &eis a fast 1v1 in a fair playing &efield."));
//				bracketslore.add(T.replace("&7[&3Click here to Host Brackets 1v1's&7]"));
//				rodlore.add(T.replace("&eParkour your way to the end and let no one stop you"));
//				rodlore.add(T.replace("&efirst person to touch the finish is declared the winner."));
//				rodlore.add(T.replace("&7[&3Click here to Host RoD&7]"));
//				lmslore.add(T.replace("&eFree for all and fight to the death in kit pvp."));
//				lmslore.add(T.replace("&7[&3Click here to Host LMS&7]"));
//				mazelore.add(T.replace("&eFind your way through the maze while at risk"));
//				mazelore.add(T.replace("&eof others beating you."));
//				mazelore.add(T.replace("&7[&3Click here to Host Maze&7]"));
//				waterdroplore.add(T.replace("&eMultiple round game with the objective"));
//				waterdroplore.add(T.replace("&eof not falling on anything but water."));
//				waterdroplore.add(T.replace("&eSimilar to &3Thimble&e."));
//				waterdroplore.add(T.replace("&7[&3Click here to Host WaterDrop&7]"));
//				nodelore.add(T.replace("&3Nodebuff &eis a slow paced 1v1 in a fair playing &efield."));
//				nodelore.add(T.replace("&7[&3Click here to Host Nodebuff 1v1's&7]"));
//				ItemMeta sumo = player.getOpenInventory().getTopInventory().getItem(0).getItemMeta();
//				sumo.setDisplayName(T.replace("&3Host Sumo"));
//				sumo.setLore(sumolore);
//				player.getOpenInventory().getTopInventory().getItem(0).setItemMeta(sumo);
//				ItemMeta brackets = player.getOpenInventory().getTopInventory().getItem(2).getItemMeta();
//				brackets.setDisplayName(T.replace("&3Host Brackets"));
//				brackets.setLore(bracketslore);
//				player.getOpenInventory().getTopInventory().getItem(2).setItemMeta(brackets);
//				ItemMeta rod = player.getOpenInventory().getTopInventory().getItem(3).getItemMeta();
//				rod.setDisplayName(T.replace("&3Host RoD"));
//				rod.setLore(rodlore);
//				player.getOpenInventory().getTopInventory().getItem(3).setItemMeta(rod);
//				ItemMeta lms = player.getOpenInventory().getTopInventory().getItem(4).getItemMeta();
//				lms.setDisplayName(T.replace("&3Host LMS"));
//				lms.setLore(lmslore);
//				player.getOpenInventory().getTopInventory().getItem(4).setItemMeta(lms);
//				ItemMeta maze = player.getOpenInventory().getTopInventory().getItem(5).getItemMeta();
//				maze.setDisplayName(T.replace("&3Host Maze"));
//				maze.setLore(mazelore);
//				player.getOpenInventory().getTopInventory().getItem(5).setItemMeta(maze);
//				ItemMeta wd = player.getOpenInventory().getTopInventory().getItem(6).getItemMeta();
//				wd.setDisplayName(T.replace("&3Host WaterDrop"));
//				wd.setLore(waterdroplore);
//				player.getOpenInventory().getTopInventory().getItem(6).setItemMeta(wd);
//				ItemMeta node = player.getOpenInventory().getTopInventory().getItem(7).getItemMeta();
//				node.setDisplayName(T.replace("&3Host NoDebuff"));
//				node.setLore(nodelore);
//				player.getOpenInventory().getTopInventory().getItem(7).setItemMeta(node);
				boolean sumobroken = false;
				boolean rrbroken = false;
				boolean bracketsbroken = false;
				boolean rodbroken = false;
				boolean lmsbroken = false;
				boolean mazebroken = false;
				boolean wdbroken = false;
				boolean nodebroken = false;
				boolean sumoonce = false;
				boolean rronce = false;
				boolean bracketsonce = false;
				boolean rodonce = false;
				boolean lmsonce = false;
				boolean mazeonce = false;
				boolean wdonce = false;
				boolean nodeonce = false;
				GUIButton glass = new GUIButton(ItemBuilder.start(Material.STAINED_GLASS_PANE).data((short)7).name(" ").build());
				glass.setListener(event -> {
					event.setCancelled(true);
				});
				for (int i = 0; i < 9; i++) {
					menu.setButton(i, glass);
				}
				if (Main.get().getEventManager().getMainSpawn() == null) {
					sumolore.add(T.replace("&7Knock your opponent off the platform!"));
					sumolore.add(T.replace(" "));
					sumolore.add(T.replace("&c&lThis event is currently disabled."));
					GUIButton button = new GUIButton(ItemBuilder.start(Material.LEASH).name(replace1("&e&lSumo")).lore(sumolore).build());
					button.setListener(event -> {
						event.setCancelled(true);
					});
					menu.addButton(button);
					rrlore.add(T.replace("&7Try to cross the middle without dying!"));
					rrlore.add(T.replace(" "));
					rrlore.add(T.replace("&c&lThis event is currently disabled."));
					GUIButton button1 = new GUIButton(ItemBuilder.start(Material.REDSTONE_BLOCK).name(replace1("&e&lRedRover")).lore(rrlore).build());
					button1.setListener(event -> {
						event.setCancelled(true);
					});
					menu.addButton(button1);
					bracketslore.add(T.replace("&7Beat your opponents in a head to head battle!"));
					bracketslore.add(T.replace(" "));
					bracketslore.add(T.replace("&c&lThis event is currently disabled."));
					GUIButton button2 = new GUIButton(ItemBuilder.start(Material.DIAMOND_SWORD).name(replace1("&e&lBrackets")).lore(bracketslore).build());
					button2.setListener(event -> {
						event.setCancelled(true);
					});
					menu.addButton(button2);
					rodlore.add(T.replace("&7The first player to make it to the end of the parkour wins!"));
					rodlore.add(T.replace(" "));
					rodlore.add(T.replace("&c&lThis event is currently disabled."));
					GUIButton button3 = new GUIButton(ItemBuilder.start(Material.RAILS).name(replace1("&e&lRace of Death")).lore(rodlore).build());
					button3.setListener(event -> {
						event.setCancelled(true);
					});
					menu.addButton(button3);
					lmslore.add(T.replace("&7Be the last man standing! Kill all your opponents!"));
					lmslore.add(T.replace(" "));
					lmslore.add(T.replace("&c&lThis event is currently disabled."));
					GUIButton button4 = new GUIButton(ItemBuilder.start(Material.IRON_SWORD).name(replace1("&e&lLast Man Standing")).lore(lmslore).build());
					button4.setListener(event -> {
						event.setCancelled(true);
					});
					menu.addButton(button4);
					mazelore.add(T.replace("&7The first player to make it to the end of the maze wins!"));
					mazelore.add(T.replace(" "));
					mazelore.add(T.replace("&c&lThis event is currently disabled."));
					GUIButton button5 = new GUIButton(ItemBuilder.start(Material.LEAVES).name(replace1("&e&lMaze")).lore(mazelore).build());
					button5.setListener(event -> {
						event.setCancelled(true);
					});
					menu.addButton(button5);
					waterdroplore.add(T.replace("&7The goal of this game is to jump into the water!"));
					waterdroplore.add(T.replace("&7It will get progressively harder, and the hole will get smaller."));
					waterdroplore.add(T.replace(" "));
					waterdroplore.add(T.replace("&c&lThis event is currently disabled."));
					GUIButton button6 = new GUIButton(ItemBuilder.start(Material.WATER_BUCKET).name(replace1("&e&lWaterdrop")).lore(waterdroplore).build());
					button6.setListener(event -> {
						event.setCancelled(true);
					});
					menu.addButton(button6);
					nodelore.add(T.replace("&7Beat your opponents in a head to head battle with potions!"));
					nodelore.add(T.replace(" "));
					nodelore.add(T.replace("&c&lThis event is currently disabled."));
					GUIButton button7 = new GUIButton(ItemBuilder.start(Material.POTION).data((short)16421).name(replace1("&e&lNodebuff")).lore(nodelore).build());
					button7.setListener(event -> {
						event.setCancelled(true);
					});
					menu.addButton(button7);
					for (int i = 36; i < 45; i++) {
						menu.setButton(i, glass);
					}
					player.openInventory(menu.getInventory());
				}
				else {
					for (Event e : Main.get().getEventManager().getEvents()) {
						if (!e.getSimpleName().equalsIgnoreCase("sumo")) {
							if (!sumoonce) {
								sumobroken = true;
							}
						}
						else {
							sumobroken = false;
							if (!sumobroken) {
								sumoonce = true;
								if (player.hasPermission("events.host." + e.getSimpleName())) {
									sumolore.add(T.replace("&7Knock your opponent off the platform!"));
									sumolore.add(T.replace(" "));
									long last = e.cache.get(player, "last_hosting", 0);
									long diff = (System.currentTimeMillis() - last) / 1000;
									if (diff < e.startCooldown && !player.hasPermission("events.bypass_cooldown"))
									{
										diff = e.startCooldown - diff;
										sumolore.add(T.replace("&cYou must wait &e" + T.formatSeconds(diff) + " &cto host this event."));
									}
									else {
										sumolore.add(T.replace("&3&l[Left click to host]"));
									}
									GUIButton button = new GUIButton(ItemBuilder.start(Material.LEASH).name(replace1("&e&lSumo")).lore(sumolore).build());
									button.setListener(event -> {
										event.setCancelled(true);
										String mode = "sumo";
	
										if (e.getSimpleName().equalsIgnoreCase(mode))
										{
											if (player.hasPermission("events.host." + e.getSimpleName()))
											{
												long last1 = e.cache.get(player, "last_hosting", 0);
												long diff1 = (System.currentTimeMillis() - last1) / 1000;
												if (diff1 < e.startCooldown && !player.hasPermission("events.bypass_cooldown"))
												{
													diff1 = e.startCooldown - diff1;
													player.closeInventory();
													player.sendMessage(T.replace("&cYou must wait &e" + T.formatSeconds(diff1) + " &cto host this event."));
												}
												else {
													e.initStart(player);
													player.closeInventory();
												}
											}
											else
											{
												player.sendMessage(T.noPerm);
												player.closeInventory();
											}
										}
									});
									menu.addButton(button);
								}
								else {
									sumolore.add(T.replace("&7Knock your opponent off the platform!"));
									sumolore.add(T.replace(" "));
									sumolore.add(T.replace("&c&lYou don't have permission to host this event."));
									GUIButton button = new GUIButton(ItemBuilder.start(Material.LEASH).name(replace1("&e&lSumo")).lore(sumolore).build());
									button.setListener(event -> {
										event.setCancelled(true);
									});
									menu.addButton(button);
								}
							}
						}
						if (!e.getSimpleName().equalsIgnoreCase("redrover")) {
							if (!rronce) {
								rrbroken = true;
							}
						}
						else {
							rrbroken = false;
							if (!rrbroken) {
								rronce = true;
								if (player.hasPermission("events.host." + e.getSimpleName())) {
									rrlore.add(T.replace("&7Try to cross the middle without dying!"));
									rrlore.add(T.replace(" "));
									long last = e.cache.get(player, "last_hosting", 0);
									long diff = (System.currentTimeMillis() - last) / 1000;
									if (diff < e.startCooldown && !player.hasPermission("events.bypass_cooldown"))
									{
									    diff = e.startCooldown - diff;
									    rrlore.add(T.replace("&cYou must wait &e" + T.formatSeconds(diff) + " &cto host this event."));
									}
									else {
									    rrlore.add(T.replace("&3&l[Left click to host]"));
									}
									GUIButton button = new GUIButton(ItemBuilder.start(Material.REDSTONE_BLOCK).name(replace1("&e&lRedRover")).lore(rrlore).build());
									button.setListener(event -> {
										event.setCancelled(true);
										String mode = "redrover";
	
										if (e.getSimpleName().equalsIgnoreCase(mode))
										{
											if (player.hasPermission("events.host." + e.getSimpleName()))
											{
												long last1 = e.cache.get(player, "last_hosting", 0);
												long diff1 = (System.currentTimeMillis() - last1) / 1000;
												if (diff1 < e.startCooldown && !player.hasPermission("events.bypass_cooldown"))
												{
												    diff1 = e.startCooldown - diff1;
												    player.closeInventory();
												    player.sendMessage(T.replace("&cYou must wait &e" + T.formatSeconds(diff1) + " &cto host this event."));
												}
												else {
												    e.initStart(player);
												    player.closeInventory();
												}
											}
											else
											{
												player.sendMessage(T.noPerm);
												player.closeInventory();
											}
										}
									});
									menu.addButton(button);
								}
								else {
									rrlore.add(T.replace("&7Try to cross the middle without dying!"));
									rrlore.add(T.replace(" "));
									rrlore.add(T.replace("&c&lYou don't have permission to host this event."));
									GUIButton button = new GUIButton(ItemBuilder.start(Material.REDSTONE_BLOCK).name(replace1("&e&lRedRover")).lore(rrlore).build());
									button.setListener(event -> {
										event.setCancelled(true);
									});
									menu.addButton(button);
								}
							}
						}
						if (!e.getSimpleName().equalsIgnoreCase("brackets")) {
							if (!bracketsonce) {
								bracketsbroken = true;
							}
						}
						else {
							bracketsbroken = false;
							if (!bracketsbroken) {
								bracketsonce = true;
								if (player.hasPermission("events.host." + e.getSimpleName())) {
									bracketslore.add(T.replace("&7Beat your opponents in a head to head battle!"));
									bracketslore.add(T.replace(" "));
									long last = e.cache.get(player, "last_hosting", 0);
									long diff = (System.currentTimeMillis() - last) / 1000;
									if (diff < e.startCooldown && !player.hasPermission("events.bypass_cooldown"))
									{
									    diff = e.startCooldown - diff;
									    bracketslore.add(T.replace("&cYou must wait &e" + T.formatSeconds(diff) + " &cto host this event."));
									}
									else {
										bracketslore.add(T.replace("&3&l[Left click to host]"));
									}
									GUIButton button = new GUIButton(ItemBuilder.start(Material.DIAMOND_SWORD).name(replace1("&e&lBrackets")).lore(bracketslore).build());
									button.setListener(event -> {
										event.setCancelled(true);
										String mode = "brackets";
	
										if (e.getSimpleName().equalsIgnoreCase(mode))
										{
											if (player.hasPermission("events.host." + e.getSimpleName()))
											{
												long last1 = e.cache.get(player, "last_hosting", 0);
												long diff1 = (System.currentTimeMillis() - last1) / 1000;
												if (diff1 < e.startCooldown && !player.hasPermission("events.bypass_cooldown"))
												{
												    diff1 = e.startCooldown - diff1;
												    player.closeInventory();
												    player.sendMessage(T.replace("&cYou must wait &e" + T.formatSeconds(diff1) + " &cto host this event."));
												}
												else {
												    e.initStart(player);
												    player.closeInventory();
												}
											}
											else
											{
												player.sendMessage(T.noPerm);
												player.closeInventory();
											}
										}
									});
									menu.addButton(button);
								}
								else {
									bracketslore.add(T.replace("&7Beat your opponents in a head to head battle!"));
									bracketslore.add(T.replace(" "));
									bracketslore.add(T.replace("&c&lYou don't have permission to host this event."));
									GUIButton button = new GUIButton(ItemBuilder.start(Material.DIAMOND_SWORD).name(replace1("&e&lBrackets")).lore(bracketslore).build());
									button.setListener(event -> {
										event.setCancelled(true);
									});
									menu.addButton(button);
								}
							}
						}
						if (!e.getSimpleName().equalsIgnoreCase("rod")) {
							if (!rodonce) {
								rodbroken = true;
							}
						}
						else {
							rodbroken = false;
							if (!rodbroken) {
								rodonce = true;
								if (player.hasPermission("events.host." + e.getSimpleName())) {
									rodlore.add(T.replace("&7The first player to make it to the end of the parkour wins!"));
									rodlore.add(T.replace(" "));
									long last = e.cache.get(player, "last_hosting", 0);
									long diff = (System.currentTimeMillis() - last) / 1000;
									if (diff < e.startCooldown && !player.hasPermission("events.bypass_cooldown"))
									{
									    diff = e.startCooldown - diff;
									    rodlore.add(T.replace("&cYou must wait &e" + T.formatSeconds(diff) + " &cto host this event."));
									}
									else {
									    rodlore.add(T.replace("&3&l[Left click to host]"));
									}
									GUIButton button = new GUIButton(ItemBuilder.start(Material.RAILS).name(replace1("&e&lRace of Death")).lore(rodlore).build());
									button.setListener(event -> {
										event.setCancelled(true);
										String mode = "rod";
	
										if (e.getSimpleName().equalsIgnoreCase(mode))
										{
											if (player.hasPermission("events.host." + e.getSimpleName()))
											{
												long last1 = e.cache.get(player, "last_hosting", 0);
												long diff1 = (System.currentTimeMillis() - last1) / 1000;
												if (diff1 < e.startCooldown && !player.hasPermission("events.bypass_cooldown"))
												{
												    diff1 = e.startCooldown - diff1;
												    player.closeInventory();
												    player.sendMessage(T.replace("&cYou must wait &e" + T.formatSeconds(diff1) + " &cto host this event."));
												}
												else {
												    e.initStart(player);
												    player.closeInventory();
												}
											}
											else
											{
												player.sendMessage(T.noPerm);
												player.closeInventory();
											}
										}
									});
									menu.addButton(button);
								}
								else {
									rodlore.add(T.replace("&7The first player to make it to the end of the parkour wins!"));
									rodlore.add(T.replace(" "));
									rodlore.add(T.replace("&c&lYou don't have permission to host this event."));
									GUIButton button = new GUIButton(ItemBuilder.start(Material.RAILS).name(replace1("&e&lRace of Death")).lore(rodlore).build());
									button.setListener(event -> {
										event.setCancelled(true);
									});
									menu.addButton(button);
								}
							}
						}
						if (!e.getSimpleName().equalsIgnoreCase("lms")) {
							if (!lmsonce) {
								lmsbroken = true;
							}
						}
						else {
							lmsbroken = false;
							if (!lmsbroken) {
								lmsonce = true;
								if (player.hasPermission("events.host." + e.getSimpleName())) {
									lmslore.add(T.replace("&7Be the last man standing! Kill all your opponents!"));
									lmslore.add(T.replace(" "));
									long last = e.cache.get(player, "last_hosting", 0);
									long diff = (System.currentTimeMillis() - last) / 1000;
									if (diff < e.startCooldown && !player.hasPermission("events.bypass_cooldown"))
									{
									    diff = e.startCooldown - diff;
									    lmslore.add(T.replace("&cYou must wait &e" + T.formatSeconds(diff) + " &cto host this event."));
									}
									else {
									    lmslore.add(T.replace("&3&l[Left click to host]"));
									}
									GUIButton button = new GUIButton(ItemBuilder.start(Material.IRON_SWORD).name(replace1("&e&lLast Man Standing")).lore(lmslore).build());
									button.setListener(event -> {
										event.setCancelled(true);
										String mode = "lms";
	
										if (e.getSimpleName().equalsIgnoreCase(mode))
										{
											if (player.hasPermission("events.host." + e.getSimpleName()))
											{
												long last1 = e.cache.get(player, "last_hosting", 0);
												long diff1 = (System.currentTimeMillis() - last1) / 1000;
												if (diff1 < e.startCooldown && !player.hasPermission("events.bypass_cooldown"))
												{
												    diff1 = e.startCooldown - diff1;
												    player.closeInventory();
												    player.sendMessage(T.replace("&cYou must wait &e" + T.formatSeconds(diff1) + " &cto host this event."));
												}
												else {
												    e.initStart(player);
												    player.closeInventory();
												}
											}
											else
											{
												player.sendMessage(T.noPerm);
												player.closeInventory();
											}
										}
									});
									menu.addButton(button);
								}
								else {
									lmslore.add(T.replace("&7Be the last man standing! Kill all your opponents!"));
									lmslore.add(T.replace(" "));
									lmslore.add(T.replace("&c&lYou don't have permission to host this event."));
									GUIButton button = new GUIButton(ItemBuilder.start(Material.IRON_SWORD).name(replace1("&e&lLast Man Standing")).lore(lmslore).build());
									button.setListener(event -> {
										event.setCancelled(true);
									});
									menu.addButton(button);
								}
							}
						}
						if (!e.getSimpleName().equalsIgnoreCase("maze")) {
							if (!mazeonce) {
								mazebroken = true;
							}
						}
						else {
							mazebroken = false;
							if (!mazebroken) {
								mazeonce = true;
								if (player.hasPermission("events.host." + e.getSimpleName())) {
									mazelore.add(T.replace("&7The first player to make it to the end of the maze wins!"));
									mazelore.add(T.replace(" "));
									long last = e.cache.get(player, "last_hosting", 0);
									long diff = (System.currentTimeMillis() - last) / 1000;
									if (diff < e.startCooldown && !player.hasPermission("events.bypass_cooldown"))
									{
									    diff = e.startCooldown - diff;
									    mazelore.add(T.replace("&cYou must wait &e" + T.formatSeconds(diff) + " &cto host this event."));
									}
									else {
									    mazelore.add(T.replace("&3&l[Left click to host]"));
									}
									GUIButton button = new GUIButton(ItemBuilder.start(Material.LEAVES).name(replace1("&e&lMaze")).lore(mazelore).build());
									button.setListener(event -> {
										event.setCancelled(true);
										String mode = "maze";
	
										if (e.getSimpleName().equalsIgnoreCase(mode))
										{
											if (player.hasPermission("events.host." + e.getSimpleName()))
											{
												long last1 = e.cache.get(player, "last_hosting", 0);
												long diff1 = (System.currentTimeMillis() - last1) / 1000;
												if (diff1 < e.startCooldown && !player.hasPermission("events.bypass_cooldown"))
												{
												    diff1 = e.startCooldown - diff1;
												    player.closeInventory();
												    player.sendMessage(T.replace("&cYou must wait &e" + T.formatSeconds(diff1) + " &cto host this event."));
												}
												else {
												    e.initStart(player);
												    player.closeInventory();
												}
											}
											else
											{
												player.sendMessage(T.noPerm);
												player.closeInventory();
											}
										}
									});
									menu.addButton(button);
								}
								else {
									mazelore.add(T.replace("&7The first player to make it to the end of the maze wins!"));
									mazelore.add(T.replace(" "));
									mazelore.add(T.replace("&c&lYou don't have permission to host this event."));
									GUIButton button = new GUIButton(ItemBuilder.start(Material.LEAVES).name(replace1("&e&lMaze")).lore(mazelore).build());
									button.setListener(event -> {
										event.setCancelled(true);
									});
									menu.addButton(button);
								}
							}
						}
						if (!e.getSimpleName().equalsIgnoreCase("waterdrop")) {
							if (!wdonce) {
								wdbroken = true;
							}
						}
						else {
							wdbroken = false;
							if (!wdbroken) {
								wdonce = true;
								if (player.hasPermission("events.host." + e.getSimpleName())) {
									waterdroplore.add(T.replace("&7The goal of this game is to jump into the water!"));
									waterdroplore.add(T.replace("&7It will get progressively harder, and the hole will get smaller."));
									waterdroplore.add(T.replace(" "));
									long last = e.cache.get(player, "last_hosting", 0);
									long diff = (System.currentTimeMillis() - last) / 1000;
									if (diff < e.startCooldown && !player.hasPermission("events.bypass_cooldown"))
									{
									    diff = e.startCooldown - diff;
									    waterdroplore.add(T.replace("&cYou must wait &e" + T.formatSeconds(diff) + " &cto host this event."));
									}
									else {
									    waterdroplore.add(T.replace("&3&l[Left click to host]"));
									}
									GUIButton button = new GUIButton(ItemBuilder.start(Material.WATER_BUCKET).name(replace1("&e&lWaterdrop")).lore(waterdroplore).build());
									button.setListener(event -> {
										event.setCancelled(true);
										String mode = "waterdrop";
	
										if (e.getSimpleName().equalsIgnoreCase(mode))
										{
											if (player.hasPermission("events.host." + e.getSimpleName()))
											{
												long last1 = e.cache.get(player, "last_hosting", 0);
												long diff1 = (System.currentTimeMillis() - last1) / 1000;
												if (diff1 < e.startCooldown && !player.hasPermission("events.bypass_cooldown"))
												{
												    diff1 = e.startCooldown - diff1;
												    player.closeInventory();
												    player.sendMessage(T.replace("&cYou must wait &e" + T.formatSeconds(diff1) + " &cto host this event."));
												}
												else {
												    e.initStart(player);
												    player.closeInventory();
												}
											}
											else
											{
												player.sendMessage(T.noPerm);
												player.closeInventory();
											}
										}
									});
									menu.addButton(button);
								}
								else {
									waterdroplore.add(T.replace("&7The goal of this game is to jump into the water!"));
									waterdroplore.add(T.replace("&7It will get progressively harder, and the hole will get smaller."));
									waterdroplore.add(T.replace(" "));
									waterdroplore.add(T.replace("&c&lYou don't have permission to host this event."));
									GUIButton button = new GUIButton(ItemBuilder.start(Material.WATER_BUCKET).name(replace1("&e&lWaterdrop")).lore(waterdroplore).build());
									button.setListener(event -> {
										event.setCancelled(true);
									});
									menu.addButton(button);
								}
							}
						}
						if (!e.getSimpleName().equalsIgnoreCase("node")) {
							if (!nodeonce) {
								nodebroken = true;
							}
						}
						else {
							nodebroken = false;
							if (!nodebroken) {
								nodeonce = true;
								if (player.hasPermission("events.host." + e.getSimpleName())) {
									nodelore.add(T.replace("&7Beat your opponents in a head to head battle with potions!"));
									nodelore.add(T.replace(" "));
									long last = e.cache.get(player, "last_hosting", 0);
									long diff = (System.currentTimeMillis() - last) / 1000;
									if (diff < e.startCooldown && !player.hasPermission("events.bypass_cooldown"))
									{
									    diff = e.startCooldown - diff;
									    nodelore.add(T.replace("&cYou must wait &e" + T.formatSeconds(diff) + " &cto host this event."));
									}
									else {
									    nodelore.add(T.replace("&3&l[Left click to host]"));
									}
									GUIButton button = new GUIButton(ItemBuilder.start(Material.POTION).data((short)16421).name(replace1("&e&lNodebuff")).lore(nodelore).build());
									button.setListener(event -> {
										event.setCancelled(true);
										String mode = "node";
	
										if (e.getSimpleName().equalsIgnoreCase(mode))
										{
											if (player.hasPermission("events.host." + e.getSimpleName()))
											{
												long last1 = e.cache.get(player, "last_hosting", 0);
												long diff1 = (System.currentTimeMillis() - last1) / 1000;
												if (diff1 < e.startCooldown && !player.hasPermission("events.bypass_cooldown"))
												{
												    diff1 = e.startCooldown - diff1;
												    player.closeInventory();
												    player.sendMessage(T.replace("&cYou must wait &e" + T.formatSeconds(diff1) + " &cto host this event."));
												}
												else {
												    e.initStart(player);
												    player.closeInventory();
												}
											}
											else
											{
												player.sendMessage(T.noPerm);
												player.closeInventory();
											}
										}
									});
									menu.addButton(button);
								}
								else {
									nodelore.add(T.replace("&7Beat your opponents in a head to head battle with potions!"));
									nodelore.add(T.replace(" "));
									nodelore.add(T.replace("&c&lYou don't have permission to host this event."));
									GUIButton button = new GUIButton(ItemBuilder.start(Material.POTION).data((short)16421).name(replace1("&e&lNodebuff")).lore(nodelore).build());
									button.setListener(event -> {
										event.setCancelled(true);
									});
									menu.addButton(button);
								}
							}
						}
					}
					if (sumobroken) {
						if (!sumoonce) {
							sumolore.add(T.replace("&7Knock your opponent off the platform!"));
							sumolore.add(T.replace(" "));
							sumolore.add(T.replace("&c&lThis event is currently disabled."));
							GUIButton button = new GUIButton(ItemBuilder.start(Material.LEASH).name(replace1("&e&lSumo")).lore(sumolore).build());
							button.setListener(event -> {
								event.setCancelled(true);
							});
							menu.addButton(button);
						}
					}
					if (rrbroken) {
						if (!rronce) {
							rrlore.add(T.replace("&7Try to cross the middle without dying!"));
							rrlore.add(T.replace(" "));
							rrlore.add(T.replace("&c&lThis event is currently disabled."));
							GUIButton button = new GUIButton(ItemBuilder.start(Material.REDSTONE_BLOCK).name(replace1("&e&lRedRover")).lore(rrlore).build());
							button.setListener(event -> {
								event.setCancelled(true);
							});
							menu.addButton(button);
						}
					}
					if (bracketsbroken) {
						if (!bracketsonce) {
							bracketslore.add(T.replace("&7Beat your opponents in a head to head battle!"));
							bracketslore.add(T.replace(" "));
							bracketslore.add(T.replace("&c&lThis event is currently disabled."));
							GUIButton button = new GUIButton(ItemBuilder.start(Material.DIAMOND_SWORD).name(replace1("&e&lBrackets")).lore(bracketslore).build());
							button.setListener(event -> {
								event.setCancelled(true);
							});
							menu.addButton(button);
						}
					}
					if (rodbroken) {
						if (!rodonce) {
							rodlore.add(T.replace("&7The first player to make it to the end of the parkour wins!"));
							rodlore.add(T.replace(" "));
							rodlore.add(T.replace("&c&lThis event is currently disabled."));
							GUIButton button = new GUIButton(ItemBuilder.start(Material.RAILS).name(replace1("&e&lRace of Death")).lore(rodlore).build());
							button.setListener(event -> {
								event.setCancelled(true);
							});
							menu.addButton(button);
						}
					}
					if (lmsbroken) {
						if (!lmsonce) {
							lmslore.add(T.replace("&7Be the last man standing! Kill all your opponents!"));
							lmslore.add(T.replace(" "));
							lmslore.add(T.replace("&c&lThis event is currently disabled."));
							GUIButton button = new GUIButton(ItemBuilder.start(Material.IRON_SWORD).name(replace1("&e&lLast Man Standing")).lore(lmslore).build());
							button.setListener(event -> {
								event.setCancelled(true);
							});
							menu.addButton(button);
						}
					}
					if (mazebroken) {
						if (!mazeonce) {
							mazelore.add(T.replace("&7The first player to make it to the end of the maze wins!"));
							mazelore.add(T.replace(" "));
							mazelore.add(T.replace("&c&lThis event is currently disabled."));
							GUIButton button = new GUIButton(ItemBuilder.start(Material.LEAVES).name(replace1("&e&lMaze")).lore(mazelore).build());
							button.setListener(event -> {
								event.setCancelled(true);
							});
							menu.addButton(button);
						}
					}
					if (wdbroken) {
						if (!wdonce) {
							waterdroplore.add(T.replace("&7The goal of this game is to jump into the water!"));
							waterdroplore.add(T.replace("&7It will get progressively harder, and the hole will get smaller."));
							waterdroplore.add(T.replace(" "));
							waterdroplore.add(T.replace("&c&lThis event is currently disabled."));
							GUIButton button = new GUIButton(ItemBuilder.start(Material.WATER_BUCKET).name(replace1("&e&lWaterdrop")).lore(waterdroplore).build());
							button.setListener(event -> {
								event.setCancelled(true);
							});
							menu.addButton(button);
						}
					}
					if (nodebroken) {
						if (!nodeonce) {
							nodelore.add(T.replace("&7Beat your opponents in a head to head battle with potions!"));
							nodelore.add(T.replace(" "));
							nodelore.add(T.replace("&c&lThis event is currently disabled."));
							GUIButton button = new GUIButton(ItemBuilder.start(Material.POTION).data((short)16421).name(replace1("&e&lNodebuff")).lore(nodelore).build());
							button.setListener(event -> {
								event.setCancelled(true);
							});
							menu.addButton(button);
						}
					}
					for (int i = 36; i < 45; i++) {
						menu.setButton(i, glass);
					}
					player.openInventory(menu.getInventory());
				}
			}
		}
//		else if (args[0].equalsIgnoreCase("host"))
//		{
//			if (args.length == 1)
//			{
//				boolean once = false;
//				for (Event e : Main.get().getEventManager().getEvents())
//				{
//					String perm = "events.host." + e.getSimpleName();
//					if (sender.hasPermission(perm))
//					{
//						once = true;
//					}
//					sendCmd(sender, "host " + e.getSimpleName(), perm);
//				}
//				if (!once)
//				{
//					sender.sendMessage(T.noPerm);
//					return false;
//				}
//			}
//			else
//			{
//				String mode = args[1].toLowerCase();
//
//				EventManager eman = Main.get().getEventManager();
//
//				if (eman.getActive() != null)
//				{
//					sender.sendMessage(T.main("An event is currently active.\nOnly one event can run at a time."));
//					return false;
//				}
//
//				for (Event e : eman.getEvents())
//				{
//					if (e.getSimpleName().equals(mode))
//					{
//						if (sender.hasPermission("events.host." + e.getSimpleName()))
//						{
//							e.initStart(sender);
//						}
//						else
//						{
//							sender.sendMessage(T.noPerm);
//						}
//						return true;
//					}
//				}
//				sender.sendMessage(T.main("Unknown event mode " + T.g(mode)));
//			}
//		}
		else if (args[0].equalsIgnoreCase("reload"))
		{
			if (!isPlayer(sender, true))
			{
				return false;
			}
			if (!sender.hasPermission("events.admin"))
			{
				sender.sendMessage(T.noPerm);
				return false;
			}
			Event active = Main.get().getEventManager().getActive();
			if (active != null)
			{
				T.sendMessage(sender, "&cYou cannot reload the plugin as there is an event currently running.");
			}
			else
			{
				Main.get().reloadConfig();
				Main.get().reloadFilemsgs();
				Main.get().reloadFileStats();
				T.sendMessage(sender, "&eConfig, Stats and Messages &dReloaded&e!");
			}
		}
		else if (args[0].equalsIgnoreCase("stop"))
		{
			if (!isPlayer(sender, true))
			{
				return false;
			}
			if (!sender.hasPermission("events.admin"))
			{
				sender.sendMessage(T.noPerm);
				return false;
			}
			Event active = Main.get().getEventManager().getActive();
			if (active != null)
			{
				T.sendMessage(sender, "&eThe running event &d" + active.getName() + " &ehas been forcefully stopped.");
				active.initFinish();
			}
			else
			{
				T.sendMessage(sender, "&cThere currently isn't any event active right now.");
			}
		}
		else if (args[0].equalsIgnoreCase("kick"))
		{
			if (!sender.hasPermission("events.admin"))
			{
				sender.sendMessage(T.noPerm);
				return false;
			}
			if (args.length == 1)
			{
				T.sendMessage(sender, "&eUsage: &d/event kick &f<player>");
				return false;
			}
			Event event = Main.get().getEventManager().getActive();
			if (event == null)
			{
				T.sendMessage(sender, "&cThere currently isn't any event active right now.");
				return false;
			}
			Player kick = Bukkit.getPlayerExact(args[1]);
			if (kick == null)
			{
				T.sendMessage(sender, "&cError: &e" + args[1] + " &cisn't a valid player.");
				return false;
			}
			if (!event.isPlaying(kick))
			{
				T.sendMessage(sender, "&cError: &e" + kick.getName() + " &cisn't participating in the current event.");
				return false;
			}
			T.sendMessage(sender, "&eKicked &d" + kick.getName() + " &efrom the current event!");
			event.removePlayer(kick);
		}
		else
		{
			T.sendMessage(sender, "&cNo command matched &e" + args[0] + "&c.");
			Bukkit.getServer().dispatchCommand(sender, "event");
		}
		return false;
	}

	private void sendCmd(CommandSender sender, String cmd, String perm)
	{
		if (perm != null && !sender.hasPermission(perm))
		{
			return;
		}
		if (cmd.equalsIgnoreCase("kick")) {
			T.sendMessage(sender, "&eevent " + cmd + " &d<player> &e- Kicks the player from the currently running event");
			return;
		}
		if (cmd.equalsIgnoreCase("spec")) {
			T.sendMessage(sender, "&eevent " + cmd + " - Spectates the currently running event");
			return;
		}
		if (cmd.equalsIgnoreCase("host")) {
			T.sendMessage(sender, "&eevent " + cmd + " - Host an event");
			return;
		}
		if (cmd.equalsIgnoreCase("leave")) {
			T.sendMessage(sender, "&eevent " + cmd + " - Leaves the event you're participating in");
			return;
		}
		if (cmd.equalsIgnoreCase("stop")) {
			T.sendMessage(sender, "&eevent " + cmd + " - Force ends the currently running event");
			return;
		}
		T.sendMessage(sender, "&eevent " + cmd);
	}

	public boolean isPlayer(CommandSender sender, boolean sendMsg)
	{
		boolean p = sender instanceof Player;
		if (!p && sendMsg)
		{
			T.sendMessage(sender, "&cYou need to be a player to use that command.");
		}
		return p;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
	{
		List<String> no = Collections.emptyList();
		if (args.length == 1)
		{
			List<String> base = new ArrayList<>();
			if (sender.hasPermission("events.join"))
			{
				base.add("join");
			}
			if (sender.hasPermission("events.spectate"))
			{
				base.add("spec");
			}
			base.add("leave");
			if (sender.hasPermission("events.admin"))
			{
				base.add("stop");
				base.add("kick");
			}
			return T.startsWith(args[0], base.toArray(new String[0]));
		}
		return no;
	}
	
	public static Inventory getInventory(Player player) {
		if (inventoryMap.containsKey(player)) {
			return inventoryMap.get(player);
		}
		else {
			return null;
		}
	}
	
	public static Inventory setupInventoryEvents(Inventory inventory, Player player) {
        inventoryMap.put(player, inventory);
        for (int i = 0; i < 27; i++) {
            if (events.contains(i)) {
                inventory.setItem(i, new ItemStack(Material.EMERALD, 1));
            }
        }
        return inventory;
    }

	
	public static String replace1(String message) {
		return message.replaceAll("&", "\u00A7");
	}
}
