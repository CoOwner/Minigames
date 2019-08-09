package com.zyrox.events;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class StatConfig implements CommandExecutor, Listener {
	
	static Main plugin;
	
	public StatConfig(Main instance) {
		plugin = instance;
	}
	
	private String usageWCB = "&eUsage: &d/eventstats [player]&e.";
	private static String noPerms = "&cYou do not have permission to use this!";
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("eventstats")) {
			if (args.length == 0) {
				T.sendMessage(sender, "&aYour event stats:");
				Player player = (Player) sender;
				for (Event e : Main.get().getEventManager().getEvents()) {
					if (Main.get().getFilestats().getConfigurationSection("Event-Stats." + player.getUniqueId().toString()) != null) {
						if (Main.get().getFilestats().get("Event-Stats." + player.getUniqueId().toString() + "." + e.getName()) != null) {
							int stats = Main.get().getFilestats().getInt("Event-Stats." + player.getUniqueId().toString() + "." + e.getName());
							T.sendMessage(sender, " &a" + e.getName() + ": &2" + stats);
						}
						else {
							T.sendMessage(sender, " &a" + e.getName() + ": &20");
						}
					}
					else {
						T.sendMessage(sender, " &a" + e.getName() + ": &20");
					}
				}
			}
			else if (args.length == 1) {
				if (Bukkit.getPlayer(args[0]) != null) {
					Player target = (Player) Bukkit.getPlayer(args[0]);
					T.sendMessage(sender, "&a" + target.getName() + "'s event stats:");
					for (Event e : Main.get().getEventManager().getEvents()) {
						if (Main.get().getFilestats().getConfigurationSection("Event-Stats." + target.getUniqueId().toString()) != null) {
							if (Main.get().getFilestats().get("Event-Stats." + target.getUniqueId().toString() + "." + e.getName()) != null) {
								int stats = Main.get().getFilestats().getInt("Event-Stats." + target.getUniqueId().toString() + "." + e.getName());
								T.sendMessage(sender, " &a" + e.getName() + ": &2" + stats);
							}
							else {
								T.sendMessage(sender, " &a" + e.getName() + ": &20");
							}
						}
						else {
							T.sendMessage(sender, " &a" + e.getName() + ": &20");
						}
					}
				}
				else if (Bukkit.getOfflinePlayer(args[0]).hasPlayedBefore() || Bukkit.getOfflinePlayer(args[0]) != null) {
					OfflinePlayer target = (OfflinePlayer) Bukkit.getOfflinePlayer(args[0]);
					T.sendMessage(sender, "&a" + target.getName() + "'s event stats:");
					for (Event e : Main.get().getEventManager().getEvents()) {
						if (Main.get().getFilestats().getConfigurationSection("Event-Stats." + target.getUniqueId().toString()) != null) {
							if (Main.get().getFilestats().get("Event-Stats." + target.getUniqueId().toString() + "." + e.getName()) != null) {
								int stats = Main.get().getFilestats().getInt("Event-Stats." + target.getUniqueId().toString() + "." + e.getName());
								T.sendMessage(sender, " &a" + e.getName() + ": &2" + stats);
							}
							else {
								T.sendMessage(sender, " &a" + e.getName() + ": &20");
							}
						}
						else {
							T.sendMessage(sender, " &a" + e.getName() + ": &20");
						}
					}
				}
			}
		}
		return true;
	}
}
