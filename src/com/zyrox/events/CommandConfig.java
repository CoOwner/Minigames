package com.zyrox.events;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class CommandConfig implements CommandExecutor, TabCompleter
{

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (!sender.hasPermission("events.config"))
		{
			sender.sendMessage(T.noPerm);
			return false;
		}

		Main pl = Main.get();
		FileConfiguration config = pl.getConfig();

		if (args.length == 0)
		{
			sender.sendMessage(T.replace("&eMain spawn: " + T.g("/eventsconfig setMainSpawn")));
			sender.sendMessage(T.replace("&eUsage: " + T.g("/eventsconfig <eventmode> <sub commands...>") + "\nAvailable modes:"));
			String[] modes = new String[]{ "Sumo", "RedRover", "Brackets", "RoD", "LMS", "Maze", "WaterDrop", "Node" };
			for (String m : modes)
			{
				sender.sendMessage(T.replace("&e - " + T.g(m)));
			}
		}
		else if (args[0].equals("setMainSpawn"))
		{
			if (sender instanceof Player)
			{
				Player p = (Player) sender;
				Location loc = p.getLocation();
				cleanLocation(loc);
				EventManager.setLocation(config, "main_spawn", loc);
				pl.saveConfig();
				pl.reloadConfig();
				p.sendMessage(T.replace("&e&eSet main spawn " + loc));
			}
			else
			{
				sender.sendMessage(T.replace("&eOnly players can execute that command"));
			}
		}
		else if (args[0].equals("sumo"))
		{
			if (args.length < 2)
			{
				sender.sendMessage(T.replace("&eAvailable sub commands:"));
				sender.sendMessage(T.replace("&e - " + T.g("list")));
				sender.sendMessage(T.replace("&e - " + T.g("setWinnerCommand <cmd...>")));
				String[] modes = new String[]{ "delete", "setPlayer1", "setPlayer2", "setSpectate", "setMinY <Y>", "setScoreBoardName" };
				for (String m : modes)
				{
					sender.sendMessage(T.replace("&e - " + T.g(m + " <arena>")));
				}
				return true;
			}
			else if (args[1].equalsIgnoreCase("list"))
			{
				List<Sumo.Arena> arenas = pl.getEventManager().readArenas("sumo", true);
				if (arenas.isEmpty())
				{
					sender.sendMessage(T.replace("&eNo arenas are set up for Sumo yet."));
				}
				else
				{
					sender.sendMessage(T.replace("&eAvailable arenas:"));
					for (Sumo.Arena a : arenas)
					{
						String error = a.getFirstInvalid();
						String color = error == null ? ChatColor.GREEN + "" : ChatColor.RED + "";
						if (error == null)
						{
							error = "";
						}
						else
						{
							error = " - " + error;
						}
						sender.sendMessage(T.replace("&e - " + color + a.getName() + error));
					}

				}
				return true;
			}
			else if (args[1].equalsIgnoreCase("setWinnerCommand"))
			{
				if (args.length < 3)
				{
					sender.sendMessage(T.replace("&eUsage: " + T.g("setWinnerCommand <cmd...>")));
					sender.sendMessage(T.replace("&eYou can use {name} for the winners name\nand {uuid} for their uuid"));
					return false;
				}
				String wcmd = args[2];
				for (int i = 3; i < args.length; i++)
				{
					wcmd += " " + args[i];
				}
				config.set("sumo.winner_command", wcmd);
				Main.get().saveConfig();
				Main.get().reloadConfig();
				sender.sendMessage(T.replace("&eSet command executed for winner to:\n" + T.gold + wcmd));
			}
			else if (args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("del") || args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("rem"))
			{
				if (args.length < 3)
				{
					sender.sendMessage(T.replace("&eUsage: " + T.gold + "del <arena>"));
				}
				Set<String> keys = config.getConfigurationSection("sumo").getKeys(false);
				if (!keys.contains(args[2]))
				{
					sender.sendMessage(T.replace("&eUnknown arena " + T.gold + args[2]));
					return false;
				}
				config.set("sumo." + args[2], null);
				pl.saveConfig();
				pl.reloadConfig();
				sender.sendMessage(T.replace("&eDeleted arena " + T.gold + args[2]));
			}
			else if (args[1].equalsIgnoreCase("setPlayer1") || args[1].equalsIgnoreCase("setPlayer2") || args[1].equalsIgnoreCase("setSpectate"))
			{
				if (sender instanceof Player)
				{
					Player p = (Player) sender;
					Location loc = p.getLocation();
					cleanLocation(loc);
					String key;
					String name;
					String argcmd;
					switch (args[1].toLowerCase())
					{
						case "setplayer1":
							key = "p1";
							name = "spawn for player 1";
							argcmd = "setPlayer1";
							break;
						case "setplayer2":
							key = "p2";
							name = "spawn for player 1";
							argcmd = "setPlayer2";
							break;
						case "setspectate":
						default:
							key = "spectate";
							name = "spectate location";
							argcmd = "setSpectate";
							break;
					}
					if (args.length < 3)
					{
						sender.sendMessage(T.replace("&eUsage: " + T.gold + argcmd + " <arena>"));
						return false;
					}
					String arena = args[2];
					EventManager.setLocation(pl.getConfig(), "sumo." + arena + "." + key, loc);
					pl.saveConfig();
					pl.reloadConfig();
					p.sendMessage(T.replace("&eSet " + name + " for arena " + arena + " to " + loc));
				}
				else
				{
					sender.sendMessage(T.replace("&eOnly players can execute that command"));
				}

			}
			else if (args[1].equalsIgnoreCase("setMinY"))
			{
				if (args.length < 4)
				{
					sender.sendMessage(T.replace("&eUsage: " + T.g("setY <Y> <arena>")));
					return false;
				}
				try
				{
					double y = Double.parseDouble(args[2]);
					String arena = args[3];
					config.set("sumo." + arena + ".minY", y);
					pl.saveConfig();
					pl.reloadConfig();
					sender.sendMessage(T.replace("&eSet min y for arena " + arena + " to " + T.g(y + "")));
				}
				catch (NumberFormatException e)
				{
					sender.sendMessage(T.replace("&eInvalid number " + T.g(args[2])));
				}
			}
			else if (args[1].equalsIgnoreCase("setScoreBoardName"))
			{
				if (args.length < 4)
				{
					sender.sendMessage(T.replace("&eUsage: " + T.g("setScoreBoardName <name> <arena>")));
					return false;
				}
				try
				{
					Player p = (Player) sender;
					String name = args[2];
					String arena = args[3];
					config.set("sumo." + arena + ".boardname", name);
					pl.saveConfig();
					pl.reloadConfig();
					sender.sendMessage(T.replace("&eSet boardname for arena " + arena + " to " + T.g(name + "")));
				}
				catch (NumberFormatException e)
				{
					sender.sendMessage(T.replace("&eInvalid number " + T.g(args[2])));
				}
			}
			else
			{
				sender.sendMessage(T.replace("&eUnknown subcommand " + T.g(args[1])));
			}
		}
	else if (args[0].equals("redrover"))
	{
		if (args.length < 2)
		{
			sender.sendMessage(T.replace("&eAvailable sub commands:"));
			sender.sendMessage(T.replace("&e - " + T.g("list")));
			sender.sendMessage(T.replace("&e - " + T.g("setWinnerCommand <cmd...>")));
			String[] modes = new String[]{ "delete", "setPlayer1", "setPlayer2", "setBlock1", "setBlock2", "setSpectate", "setScoreBoardName" };
			for (String m : modes)
			{
				sender.sendMessage(T.replace("&e - " + T.g(m + " <arena>")));
			}
			return true;
		}
		else if (args[1].equalsIgnoreCase("list"))
		{
			List<RedRover.Arena> arenas1 = pl.getEventManager().readArenas1("redrover", true);
			if (arenas1.isEmpty())
			{
				sender.sendMessage(T.replace("&eNo arenas are set up for RedRover yet."));
			}
			else
			{
				sender.sendMessage(T.replace("&eAvailable arenas:"));
				for (RedRover.Arena a : arenas1)
				{
					String error = a.getFirstInvalid();
					String color = error == null ? ChatColor.GREEN + "" : ChatColor.RED + "";
					if (error == null)
					{
						error = "";
					}
					else
					{
						error = " - " + error;
					}
					sender.sendMessage(T.replace("&e - " + color + a.getName() + error));
				}

			}
			return true;
		}
		else if (args[1].equalsIgnoreCase("setWinnerCommand"))
		{
			if (args.length < 3)
			{
				sender.sendMessage(T.replace("&eUsage: " + T.g("setWinnerCommand <cmd...>")));
				sender.sendMessage(T.replace("&eYou can use {name} for the winners name\nand {uuid} for their uuid"));
				return false;
			}
			String wcmd = args[2];
			for (int i = 3; i < args.length; i++)
			{
				wcmd += " " + args[i];
			}
			config.set("redrover.winner_command", wcmd);
			Main.get().saveConfig();
			pl.reloadConfig();
			sender.sendMessage(T.replace("&eSet command executed for winner to:\n" + T.gold + wcmd));
		}
		else if (args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("del") || args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("rem"))
		{
			if (args.length < 3)
			{
				sender.sendMessage(T.replace("&eUsage: " + T.gold + "del <arena>"));
			}
			Set<String> keys = config.getConfigurationSection("redrover").getKeys(false);
			if (!keys.contains(args[2]))
			{
				sender.sendMessage(T.replace("&eUnknown arena " + T.gold + args[2]));
				return false;
			}
			config.set("redrover." + args[2], null);
			pl.saveConfig();
			pl.reloadConfig();
			sender.sendMessage(T.replace("&eDeleted arena " + T.gold + args[2]));
		}
		else if (args[1].equalsIgnoreCase("setPlayer1") || args[1].equalsIgnoreCase("setPlayer2") || args[1].equalsIgnoreCase("setBlock1") || args[1].equalsIgnoreCase("setBlock2") || args[1].equalsIgnoreCase("setSpectate"))
		{
			if (sender instanceof Player)
			{
				Player p = (Player) sender;
				Location loc = p.getLocation();
				cleanLocation(loc);
				String key;
				String name;
				String argcmd;
				switch (args[1].toLowerCase())
				{
					case "setplayer1":
						key = "p1";
						name = "spawn for player 1";
						argcmd = "setPlayer1";
						break;
					case "setplayer2":
						key = "p2";
						name = "spawn for player 1";
						argcmd = "setPlayer2";
						break;
					case "setblock1":
						key = "b1";
						name = "block spawn 1";
						argcmd = "setBlock1";
						break;
					case "setblock2":
						key = "b2";
						name = "block spawn 1";
						argcmd = "setBlock2";
						break;
					case "setspectate":
					default:
						key = "spectate";
						name = "spectate location";
						argcmd = "setSpectate";
						break;
				}
				if (args.length < 3)
				{
					sender.sendMessage(T.replace("&eUsage: " + T.gold + argcmd + " <arena>"));
					return false;
				}
				String arena = args[2];
				EventManager.setLocation(pl.getConfig(), "redrover." + arena + "." + key, loc);
				pl.saveConfig();
				pl.reloadConfig();
				p.sendMessage(T.replace("&eSet " + name + " for arena " + arena + " to " + loc));
			}
			else
			{
				sender.sendMessage(T.replace("&eOnly players can execute that command"));
			}
		}
		else if (args[1].equalsIgnoreCase("setScoreBoardName"))
		{
			if (args.length < 4)
			{
				sender.sendMessage(T.replace("&eUsage: " + T.g("setScoreBoardName <name> <arena>")));
				return false;
			}
			try
			{
				Player p = (Player) sender;
				String name = args[2];
				String arena = args[3];
				config.set("redrover." + arena + ".boardname", name);
				pl.saveConfig();
				pl.reloadConfig();
				sender.sendMessage(T.replace("&eSet boardname for arena " + arena + " to " + T.g(name + "")));
			}
			catch (NumberFormatException e)
			{
				sender.sendMessage(T.replace("&eInvalid number " + T.g(args[2])));
			}
		}
		else
		{
			sender.sendMessage(T.replace("&eUnknown subcommand " + T.g(args[1])));
		}
	}
	else if (args[0].equals("brackets"))
	{
		if (args.length < 2)
		{
			sender.sendMessage(T.replace("&eAvailable sub commands:"));
			sender.sendMessage(T.replace("&e - " + T.g("list")));
			sender.sendMessage(T.replace("&e - " + T.g("setWinnerCommand <cmd...>")));
			String[] modes = new String[]{ "delete", "setPlayer1", "setPlayer2", "setSpectate", "setScoreBoardName" };
			for (String m : modes)
			{
				sender.sendMessage(T.replace("&e - " + T.g(m + " <arena>")));
			}
			return true;
		}
		else if (args[1].equalsIgnoreCase("list"))
		{
			List<Brackets.Arena> arenas2 = pl.getEventManager().readArenas2("brackets", true);
			if (arenas2.isEmpty())
			{
				sender.sendMessage(T.replace("&eNo arenas are set up for Brackets yet."));
			}
			else
			{
				sender.sendMessage(T.replace("&eAvailable arenas:"));
				for (Brackets.Arena a : arenas2)
				{
					String error = a.getFirstInvalid();
					String color = error == null ? ChatColor.GREEN + "" : ChatColor.RED + "";
					if (error == null)
					{
						error = "";
					}
					else
					{
						error = " - " + error;
					}
					sender.sendMessage(T.replace("&e - " + color + a.getName() + error));
				}

			}
			return true;
		}
		else if (args[1].equalsIgnoreCase("setWinnerCommand"))
		{
			if (args.length < 3)
			{
				sender.sendMessage(T.replace("&eUsage: " + T.g("setWinnerCommand <cmd...>")));
				sender.sendMessage(T.replace("&eYou can use {name} for the winners name\nand {uuid} for their uuid"));
				return false;
			}
			String wcmd = args[2];
			for (int i = 3; i < args.length; i++)
			{
				wcmd += " " + args[i];
			}
			config.set("brackets.winner_command", wcmd);
			Main.get().saveConfig();
			pl.reloadConfig();
			sender.sendMessage(T.replace("&eSet command executed for winner to:\n" + T.gold + wcmd));
		}
		else if (args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("del") || args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("rem"))
		{
			if (args.length < 3)
			{
				sender.sendMessage(T.replace("&eUsage: " + T.gold + "del <arena>"));
			}
			Set<String> keys = config.getConfigurationSection("brackets").getKeys(false);
			if (!keys.contains(args[2]))
			{
				sender.sendMessage(T.replace("&eUnknown arena " + T.gold + args[2]));
				return false;
			}
			config.set("brackets." + args[2], null);
			pl.saveConfig();
			pl.reloadConfig();
			sender.sendMessage(T.replace("&eDeleted arena " + T.gold + args[2]));
		}
		else if (args[1].equalsIgnoreCase("setPlayer1") || args[1].equalsIgnoreCase("setPlayer2") || args[1].equalsIgnoreCase("setSpectate"))
		{
			if (sender instanceof Player)
			{
				Player p = (Player) sender;
				Location loc = p.getLocation();
				cleanLocation(loc);
				String key;
				String name;
				String argcmd;
				switch (args[1].toLowerCase())
				{
					case "setplayer1":
						key = "p1";
						name = "spawn for player 1";
						argcmd = "setPlayer1";
						break;
					case "setplayer2":
						key = "p2";
						name = "spawn for player 1";
						argcmd = "setPlayer2";
						break;
					case "setspectate":
					default:
						key = "spectate";
						name = "spectate location";
						argcmd = "setSpectate";
						break;
				}
				if (args.length < 3)
				{
					sender.sendMessage(T.replace("&eUsage: " + T.gold + argcmd + " <arena>"));
					return false;
				}
				String arena = args[2];
				EventManager.setLocation(pl.getConfig(), "brackets." + arena + "." + key, loc);
				pl.saveConfig();
				pl.reloadConfig();
				p.sendMessage(T.replace("&eSet " + name + " for arena " + arena + " to " + loc));
			}
			else
			{
				sender.sendMessage(T.replace("&eOnly players can execute that command"));
			}
		}
		else if (args[1].equalsIgnoreCase("setScoreBoardName"))
		{
			if (args.length < 4)
			{
				sender.sendMessage(T.replace("&eUsage: " + T.g("setScoreBoardName <name> <arena>")));
				return false;
			}
			try
			{
				Player p = (Player) sender;
				String name = args[2];
				String arena = args[3];
				config.set("brackets." + arena + ".boardname", name);
				pl.saveConfig();
				pl.reloadConfig();
				sender.sendMessage(T.replace("&eSet boardname for arena " + arena + " to " + T.g(name + "")));
			}
			catch (NumberFormatException e)
			{
				sender.sendMessage(T.replace("&eInvalid number " + T.g(args[2])));
			}
		}
		else
		{
			sender.sendMessage(T.replace("&eUnknown subcommand " + T.g(args[1])));
		}
	}
	else if (args[0].equals("node"))
	{
		if (args.length < 2)
		{
			sender.sendMessage(T.replace("&eAvailable sub commands:"));
			sender.sendMessage(T.replace("&e - " + T.g("list")));
			sender.sendMessage(T.replace("&e - " + T.g("setWinnerCommand <cmd...>")));
			String[] modes = new String[]{ "delete", "setPlayer1", "setPlayer2", "setSpectate", "setScoreBoardName" };
			for (String m : modes)
			{
				sender.sendMessage(T.replace("&e - " + T.g(m + " <arena>")));
			}
			return true;
		}
		else if (args[1].equalsIgnoreCase("list"))
		{
			List<Node.Arena> arenas2 = pl.getEventManager().readArenas7("node", true);
			if (arenas2.isEmpty())
			{
				sender.sendMessage(T.replace("&eNo arenas are set up for Node yet."));
			}
			else
			{
				sender.sendMessage(T.replace("&eAvailable arenas:"));
				for (Node.Arena a : arenas2)
				{
					String error = a.getFirstInvalid();
					String color = error == null ? ChatColor.GREEN + "" : ChatColor.RED + "";
					if (error == null)
					{
						error = "";
					}
					else
					{
						error = " - " + error;
					}
					sender.sendMessage(T.replace("&e - " + color + a.getName() + error));
				}

			}
			return true;
		}
		else if (args[1].equalsIgnoreCase("setWinnerCommand"))
		{
			if (args.length < 3)
			{
				sender.sendMessage(T.replace("&eUsage: " + T.g("setWinnerCommand <cmd...>")));
				sender.sendMessage(T.replace("&eYou can use {name} for the winners name\nand {uuid} for their uuid"));
				return false;
			}
			String wcmd = args[2];
			for (int i = 3; i < args.length; i++)
			{
				wcmd += " " + args[i];
			}
			config.set("node.winner_command", wcmd);
			Main.get().saveConfig();
			pl.reloadConfig();
			sender.sendMessage(T.replace("&eSet command executed for winner to:\n" + T.gold + wcmd));
		}
		else if (args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("del") || args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("rem"))
		{
			if (args.length < 3)
			{
				sender.sendMessage(T.replace("&eUsage: " + T.gold + "del <arena>"));
			}
			Set<String> keys = config.getConfigurationSection("node").getKeys(false);
			if (!keys.contains(args[2]))
			{
				sender.sendMessage(T.replace("&eUnknown arena " + T.gold + args[2]));
				return false;
			}
			config.set("node." + args[2], null);
			pl.saveConfig();
			pl.reloadConfig();
			sender.sendMessage(T.replace("&eDeleted arena " + T.gold + args[2]));
		}
		else if (args[1].equalsIgnoreCase("setPlayer1") || args[1].equalsIgnoreCase("setPlayer2") || args[1].equalsIgnoreCase("setSpectate"))
		{
			if (sender instanceof Player)
			{
				Player p = (Player) sender;
				Location loc = p.getLocation();
				cleanLocation(loc);
				String key;
				String name;
				String argcmd;
				switch (args[1].toLowerCase())
				{
					case "setplayer1":
						key = "p1";
						name = "spawn for player 1";
						argcmd = "setPlayer1";
						break;
					case "setplayer2":
						key = "p2";
						name = "spawn for player 1";
						argcmd = "setPlayer2";
						break;
					case "setspectate":
					default:
						key = "spectate";
						name = "spectate location";
						argcmd = "setSpectate";
						break;
				}
				if (args.length < 3)
				{
					sender.sendMessage(T.replace("&eUsage: " + T.gold + argcmd + " <arena>"));
					return false;
				}
				String arena = args[2];
				EventManager.setLocation(pl.getConfig(), "node." + arena + "." + key, loc);
				pl.saveConfig();
				pl.reloadConfig();
				p.sendMessage(T.replace("&eSet " + name + " for arena " + arena + " to " + loc));
			}
			else
			{
				sender.sendMessage(T.replace("&eOnly players can execute that command"));
			}
		}
		else if (args[1].equalsIgnoreCase("setScoreBoardName"))
		{
			if (args.length < 4)
			{
				sender.sendMessage(T.replace("&eUsage: " + T.g("setScoreBoardName <name> <arena>")));
				return false;
			}
			try
			{
				Player p = (Player) sender;
				String name = args[2];
				String arena = args[3];
				config.set("node." + arena + ".boardname", name);
				pl.saveConfig();
				pl.reloadConfig();
				sender.sendMessage(T.replace("&eSet boardname for arena " + arena + " to " + T.g(name + "")));
			}
			catch (NumberFormatException e)
			{
				sender.sendMessage(T.replace("&eInvalid number " + T.g(args[2])));
			}
		}
		else
		{
			sender.sendMessage(T.replace("&eUnknown subcommand " + T.g(args[1])));
		}
	}
	else if (args[0].equals("rod"))
	{
		if (args.length < 2)
		{
			sender.sendMessage(T.replace("&eAvailable sub commands:"));
			sender.sendMessage(T.replace("&e - " + T.g("list")));
			sender.sendMessage(T.replace("&e - " + T.g("setWinnerCommand <cmd...>")));
			String[] modes = new String[]{ "delete", "setPlayer1", "setPlayer2", "setSpectate", "setScoreBoardName" };
			for (String m : modes)
			{
				sender.sendMessage(T.replace("&e - " + T.g(m + " <arena>")));
			}
			return true;
		}
		else if (args[1].equalsIgnoreCase("list"))
		{
			List<RoD.Arena> arenas1 = pl.getEventManager().readArenas3("rod", true);
			if (arenas1.isEmpty())
			{
				sender.sendMessage(T.replace("&eNo arenas are set up for RoD yet."));
			}
			else
			{
				sender.sendMessage(T.replace("&eAvailable arenas:"));
				for (RoD.Arena a : arenas1)
				{
					String error = a.getFirstInvalid();
					String color = error == null ? ChatColor.GREEN + "" : ChatColor.RED + "";
					if (error == null)
					{
						error = "";
					}
					else
					{
						error = " - " + error;
					}
					sender.sendMessage(T.replace("&e - " + color + a.getName() + error));
				}

			}
			return true;
		}
		else if (args[1].equalsIgnoreCase("setWinnerCommand"))
		{
			if (args.length < 3)
			{
				sender.sendMessage(T.replace("&eUsage: " + T.g("setWinnerCommand <cmd...>")));
				sender.sendMessage(T.replace("&eYou can use {name} for the winners name\nand {uuid} for their uuid"));
				return false;
			}
			String wcmd = args[2];
			for (int i = 3; i < args.length; i++)
			{
				wcmd += " " + args[i];
			}
			config.set("rod.winner_command", wcmd);
			Main.get().saveConfig();
			pl.reloadConfig();
			sender.sendMessage(T.replace("&eSet command executed for winner to:\n" + T.gold + wcmd));
		}
		else if (args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("del") || args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("rem"))
		{
			if (args.length < 3)
			{
				sender.sendMessage(T.replace("&eUsage: " + T.gold + "del <arena>"));
			}
			Set<String> keys = config.getConfigurationSection("rod").getKeys(false);
			if (!keys.contains(args[2]))
			{
				sender.sendMessage(T.replace("&eUnknown arena " + T.gold + args[2]));
				return false;
			}
			config.set("rod." + args[2], null);
			pl.saveConfig();
			pl.reloadConfig();
			sender.sendMessage(T.replace("&eDeleted arena " + T.gold + args[2]));
		}
		else if (args[1].equalsIgnoreCase("setPlayer1") || args[1].equalsIgnoreCase("setPlayer2") || args[1].equalsIgnoreCase("setSpectate"))
		{
			if (sender instanceof Player)
			{
				Player p = (Player) sender;
				Location loc = p.getLocation();
				cleanLocation(loc);
				String key;
				String name;
				String argcmd;
				switch (args[1].toLowerCase())
				{
					case "setplayer1":
						key = "p1";
						name = "spawn for player 1";
						argcmd = "setPlayer1";
						break;
					case "setplayer2":
						key = "p2";
						name = "spawn for player 1";
						argcmd = "setPlayer2";
						break;
					case "setspectate":
					default:
						key = "spectate";
						name = "spectate location";
						argcmd = "setSpectate";
						break;
				}
				if (args.length < 3)
				{
					sender.sendMessage(T.replace("&eUsage: " + T.gold + argcmd + " <arena>"));
					return false;
				}
				String arena = args[2];
				EventManager.setLocation(pl.getConfig(), "rod." + arena + "." + key, loc);
				pl.saveConfig();
				pl.reloadConfig();
				p.sendMessage(T.replace("&eSet " + name + " for arena " + arena + " to " + loc));
			}
			else
			{
				sender.sendMessage(T.replace("&eOnly players can execute that command"));
			}
		}
		else if (args[1].equalsIgnoreCase("setScoreBoardName"))
		{
			if (args.length < 4)
			{
				sender.sendMessage(T.replace("&eUsage: " + T.g("setScoreBoardName <name> <arena>")));
				return false;
			}
			try
			{
				Player p = (Player) sender;
				String name = args[2];
				String arena = args[3];
				config.set("rod." + arena + ".boardname", name);
				pl.saveConfig();
				pl.reloadConfig();
				sender.sendMessage(T.replace("&eSet boardname for arena " + arena + " to " + T.g(name + "")));
			}
			catch (NumberFormatException e)
			{
				sender.sendMessage(T.replace("&eInvalid number " + T.g(args[2])));
			}
		}
		else
		{
			sender.sendMessage(T.replace("&eUnknown subcommand " + T.g(args[1])));
		}
	}
	else if (args[0].equals("lms"))
	{
		if (args.length < 2)
		{
			sender.sendMessage(T.replace("&eAvailable sub commands:"));
			sender.sendMessage(T.replace("&e - " + T.g("list")));
			sender.sendMessage(T.replace("&e - " + T.g("setWinnerCommand <cmd...>")));
			String[] modes = new String[]{ "delete", "setPlayer1", "setPlayer2", "setSpectate", "setScoreBoardName" };
			for (String m : modes)
			{
				sender.sendMessage(T.replace("&e - " + T.g(m + " <arena>")));
			}
			return true;
		}
		else if (args[1].equalsIgnoreCase("list"))
		{
			List<LastManStanding.Arena> arenas2 = pl.getEventManager().readArenas4("lms", true);
			if (arenas2.isEmpty())
			{
				sender.sendMessage(T.replace("&eNo arenas are set up for LMS yet."));
			}
			else
			{
				sender.sendMessage(T.replace("&eAvailable arenas:"));
				for (LastManStanding.Arena a : arenas2)
				{
					String error = a.getFirstInvalid();
					String color = error == null ? ChatColor.GREEN + "" : ChatColor.RED + "";
					if (error == null)
					{
						error = "";
					}
					else
					{
						error = " - " + error;
					}
					sender.sendMessage(T.replace("&e - " + color + a.getName() + error));
				}

			}
			return true;
		}
		else if (args[1].equalsIgnoreCase("setWinnerCommand"))
		{
			if (args.length < 3)
			{
				sender.sendMessage(T.replace("&eUsage: " + T.g("setWinnerCommand <cmd...>")));
				sender.sendMessage(T.replace("&eYou can use {name} for the winners name\nand {uuid} for their uuid"));
				return false;
			}
			String wcmd = args[2];
			for (int i = 3; i < args.length; i++)
			{
				wcmd += " " + args[i];
			}
			config.set("lms.winner_command", wcmd);
			Main.get().saveConfig();
			pl.reloadConfig();
			sender.sendMessage(T.replace("&eSet command executed for winner to:\n" + T.gold + wcmd));
		}
		else if (args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("del") || args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("rem"))
		{
			if (args.length < 3)
			{
				sender.sendMessage(T.replace("&eUsage: " + T.gold + "del <arena>"));
			}
			Set<String> keys = config.getConfigurationSection("lms").getKeys(false);
			if (!keys.contains(args[2]))
			{
				sender.sendMessage(T.replace("&eUnknown arena " + T.gold + args[2]));
				return false;
			}
			config.set("lms." + args[2], null);
			pl.saveConfig();
			pl.reloadConfig();
			sender.sendMessage(T.replace("&eDeleted arena " + T.gold + args[2]));
		}
		else if (args[1].equalsIgnoreCase("setPlayer1") || args[1].equalsIgnoreCase("setPlayer2") || args[1].equalsIgnoreCase("setSpectate"))
		{
			if (sender instanceof Player)
			{
				Player p = (Player) sender;
				Location loc = p.getLocation();
				cleanLocation(loc);
				String key;
				String name;
				String argcmd;
				switch (args[1].toLowerCase())
				{
					case "setplayer1":
						key = "p1";
						name = "spawn for player 1";
						argcmd = "setPlayer1";
						break;
					case "setplayer2":
						key = "p2";
						name = "spawn for player 1";
						argcmd = "setPlayer2";
						break;
					case "setspectate":
					default:
						key = "spectate";
						name = "spectate location";
						argcmd = "setSpectate";
						break;
				}
				if (args.length < 3)
				{
					sender.sendMessage(T.replace("&eUsage: " + T.gold + argcmd + " <arena>"));
					return false;
				}
				String arena = args[2];
				EventManager.setLocation(pl.getConfig(), "lms." + arena + "." + key, loc);
				pl.saveConfig();
				pl.reloadConfig();
				p.sendMessage(T.replace("&eSet " + name + " for arena " + arena + " to " + loc));
			}
			else
			{
				sender.sendMessage(T.replace("&eOnly players can execute that command"));
			}
		}
		else if (args[1].equalsIgnoreCase("setScoreBoardName"))
		{
			if (args.length < 4)
			{
				sender.sendMessage(T.replace("&eUsage: " + T.g("setScoreBoardName <name> <arena>")));
				return false;
			}
			try
			{
				Player p = (Player) sender;
				String name = args[2];
				String arena = args[3];
				config.set("lms." + arena + ".boardname", name);
				pl.saveConfig();
				pl.reloadConfig();
				sender.sendMessage(T.replace("&eSet boardname for arena " + arena + " to " + T.g(name + "")));
			}
			catch (NumberFormatException e)
			{
				sender.sendMessage(T.replace("&eInvalid number " + T.g(args[2])));
			}
		}
		else
		{
			sender.sendMessage(T.replace("&eUnknown subcommand " + T.g(args[1])));
		}
	}
	else if (args[0].equals("maze"))
	{
		if (args.length < 2)
		{
			sender.sendMessage(T.replace("&eAvailable sub commands:"));
			sender.sendMessage(T.replace("&e - " + T.g("list")));
			sender.sendMessage(T.replace("&e - " + T.g("setWinnerCommand <cmd...>")));
			String[] modes = new String[]{ "delete", "setPlayer1", "setPlayer2", "setSpectate", "setScoreBoardName" };
			for (String m : modes)
			{
				sender.sendMessage(T.replace("&e - " + T.g(m + " <arena>")));
			}
			return true;
		}
		else if (args[1].equalsIgnoreCase("list"))
		{
			List<Maze.Arena> arenas2 = pl.getEventManager().readArenas5("maze", true);
			if (arenas2.isEmpty())
			{
				sender.sendMessage(T.replace("&eNo arenas are set up for Maze yet."));
			}
			else
			{
				sender.sendMessage(T.replace("&eAvailable arenas:"));
				for (Maze.Arena a : arenas2)
				{
					String error = a.getFirstInvalid();
					String color = error == null ? ChatColor.GREEN + "" : ChatColor.RED + "";
					if (error == null)
					{
						error = "";
					}
					else
					{
						error = " - " + error;
					}
					sender.sendMessage(T.replace("&e - " + color + a.getName() + error));
				}

			}
			return true;
		}
		else if (args[1].equalsIgnoreCase("setWinnerCommand"))
		{
			if (args.length < 3)
			{
				sender.sendMessage(T.replace("&eUsage: " + T.g("setWinnerCommand <cmd...>")));
				sender.sendMessage(T.replace("&eYou can use {name} for the winners name\nand {uuid} for their uuid"));
				return false;
			}
			String wcmd = args[2];
			for (int i = 3; i < args.length; i++)
			{
				wcmd += " " + args[i];
			}
			config.set("maze.winner_command", wcmd);
			Main.get().saveConfig();
			pl.reloadConfig();
			sender.sendMessage(T.replace("&eSet command executed for winner to:\n" + T.gold + wcmd));
		}
		else if (args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("del") || args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("rem"))
		{
			if (args.length < 3)
			{
				sender.sendMessage(T.replace("&eUsage: " + T.gold + "del <arena>"));
			}
			Set<String> keys = config.getConfigurationSection("maze").getKeys(false);
			if (!keys.contains(args[2]))
			{
				sender.sendMessage(T.replace("&eUnknown arena " + T.gold + args[2]));
				return false;
			}
			config.set("maze." + args[2], null);
			pl.saveConfig();
			pl.reloadConfig();
			sender.sendMessage(T.replace("&eDeleted arena " + T.gold + args[2]));
		}
		else if (args[1].equalsIgnoreCase("setPlayer1") || args[1].equalsIgnoreCase("setPlayer2") || args[1].equalsIgnoreCase("setSpectate"))
		{
			if (sender instanceof Player)
			{
				Player p = (Player) sender;
				Location loc = p.getLocation();
				cleanLocation(loc);
				String key;
				String name;
				String argcmd;
				switch (args[1].toLowerCase())
				{
					case "setplayer1":
						key = "p1";
						name = "spawn for player 1";
						argcmd = "setPlayer1";
						break;
					case "setplayer2":
						key = "p2";
						name = "spawn for player 1";
						argcmd = "setPlayer2";
						break;
					case "setspectate":
					default:
						key = "spectate";
						name = "spectate location";
						argcmd = "setSpectate";
						break;
				}
				if (args.length < 3)
				{
					sender.sendMessage(T.replace("&eUsage: " + T.gold + argcmd + " <arena>"));
					return false;
				}
				String arena = args[2];
				EventManager.setLocation(pl.getConfig(), "maze." + arena + "." + key, loc);
				pl.saveConfig();
				pl.reloadConfig();
				p.sendMessage(T.replace("&eSet " + name + " for arena " + arena + " to " + loc));
			}
			else
			{
				sender.sendMessage(T.replace("&eOnly players can execute that command"));
			}
		}
		else if (args[1].equalsIgnoreCase("setScoreBoardName"))
		{
			if (args.length < 4)
			{
				sender.sendMessage(T.replace("&eUsage: " + T.g("setScoreBoardName <name> <arena>")));
				return false;
			}
			try
			{
				Player p = (Player) sender;
				String name = args[2];
				String arena = args[3];
				config.set("maze." + arena + ".boardname", name);
				pl.saveConfig();
				pl.reloadConfig();
				sender.sendMessage(T.replace("&eSet boardname for arena " + arena + " to " + T.g(name + "")));
			}
			catch (NumberFormatException e)
			{
				sender.sendMessage(T.replace("&eInvalid number " + T.g(args[2])));
			}
		}
		else
		{
			sender.sendMessage(T.replace("&eUnknown subcommand " + T.g(args[1])));
		}
	}
	else if (args[0].equals("waterdrop"))
	{
		if (args.length < 2)
		{
			sender.sendMessage(T.replace("&eAvailable sub commands:"));
			sender.sendMessage(T.replace("&e - " + T.g("list")));
			sender.sendMessage(T.replace("&e - " + T.g("setWinnerCommand <cmd...>")));
			String[] modes = new String[]{ "delete", "setPlayer1", "setPlayer2", "setBlock1", "setBlock2", "setSpectate", "setScoreBoardName" };
			for (String m : modes)
			{
				sender.sendMessage(T.replace("&e - " + T.g(m + " <arena>")));
			}
			return true;
		}
		else if (args[1].equalsIgnoreCase("list"))
		{
			List<WaterDrop.Arena> arenas2 = pl.getEventManager().readArenas6("wd", true);
			if (arenas2.isEmpty())
			{
				sender.sendMessage(T.replace("&eNo arenas are set up for WaterDrop yet."));
			}
			else
			{
				sender.sendMessage(T.replace("&eAvailable arenas:"));
				for (WaterDrop.Arena a : arenas2)
				{
					String error = a.getFirstInvalid();
					String color = error == null ? ChatColor.GREEN + "" : ChatColor.RED + "";
					if (error == null)
					{
						error = "";
					}
					else
					{
						error = " - " + error;
					}
					sender.sendMessage(T.replace("&e - " + color + a.getName() + error));
				}

			}
			return true;
		}
		else if (args[1].equalsIgnoreCase("setWinnerCommand"))
		{
			if (args.length < 3)
			{
				sender.sendMessage(T.replace("&eUsage: " + T.g("setWinnerCommand <cmd...>")));
				sender.sendMessage(T.replace("&eYou can use {name} for the winners name\nand {uuid} for their uuid"));
				return false;
			}
			String wcmd = args[2];
			for (int i = 3; i < args.length; i++)
			{
				wcmd += " " + args[i];
			}
			config.set("wd.winner_command", wcmd);
			Main.get().saveConfig();
			pl.reloadConfig();
			sender.sendMessage(T.replace("&eSet command executed for winner to:\n" + T.gold + wcmd));
		}
		else if (args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("del") || args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("rem"))
		{
			if (args.length < 3)
			{
				sender.sendMessage(T.replace("&eUsage: " + T.gold + "del <arena>"));
			}
			Set<String> keys = config.getConfigurationSection("wd").getKeys(false);
			if (!keys.contains(args[2]))
			{
				sender.sendMessage(T.replace("&eUnknown arena " + T.gold + args[2]));
				return false;
			}
			config.set("wd." + args[2], null);
			pl.saveConfig();
			pl.reloadConfig();
			sender.sendMessage(T.replace("&eDeleted arena " + T.gold + args[2]));
		}
		else if (args[1].equalsIgnoreCase("setPlayer1") || args[1].equalsIgnoreCase("setPlayer2") || args[1].equalsIgnoreCase("setBlock1") || args[1].equalsIgnoreCase("setBlock2") || args[1].equalsIgnoreCase("setSpectate"))
		{
			if (sender instanceof Player)
			{
				Player p = (Player) sender;
				Location loc = p.getLocation();
				cleanLocation(loc);
				String key;
				String name;
				String argcmd;
				switch (args[1].toLowerCase())
				{
					case "setplayer1":
						key = "p1";
						name = "spawn for player 1";
						argcmd = "setPlayer1";
						break;
					case "setplayer2":
						key = "p2";
						name = "spawn for player 1";
						argcmd = "setPlayer2";
						break;
					case "setblock1":
						key = "b1";
						name = "spawn for block 1";
						argcmd = "setBlock1";
						break;
					case "setblock2":
						key = "b2";
						name = "spawn for block 1";
						argcmd = "setBlock2";
						break;
					case "setspectate":
					default:
						key = "spectate";
						name = "spectate location";
						argcmd = "setSpectate";
						break;
				}
				if (args.length < 3)
				{
					sender.sendMessage(T.replace("&eUsage: " + T.gold + argcmd + " <arena>"));
					return false;
				}
				String arena = args[2];
				EventManager.setLocation(pl.getConfig(), "wd." + arena + "." + key, loc);
				pl.saveConfig();
				pl.reloadConfig();
				p.sendMessage(T.replace("&eSet " + name + " for arena " + arena + " to " + loc));
			}
			else
			{
				sender.sendMessage(T.replace("&eOnly players can execute that command"));
			}
		}
		else if (args[1].equalsIgnoreCase("setScoreBoardName"))
		{
			if (args.length < 4)
			{
				sender.sendMessage(T.replace("&eUsage: " + T.g("setScoreBoardName <name> <arena>")));
				return false;
			}
			try
			{
				Player p = (Player) sender;
				String name = args[2];
				String arena = args[3];
				config.set("wd." + arena + ".boardname", name);
				pl.saveConfig();
				pl.reloadConfig();
				sender.sendMessage(T.replace("&eSet boardname for arena " + arena + " to " + T.g(name + "")));
			}
			catch (NumberFormatException e)
			{
				sender.sendMessage(T.replace("&eInvalid number " + T.g(args[2])));
			}
		}
		else
		{
			sender.sendMessage(T.replace("&eUnknown subcommand " + T.g(args[1])));
		}
	}
	else
	{
		sender.sendMessage(T.replace("&eUnknown event mode " + T.g(args[0])));
	}
	return false;
	}

	public void cleanLocation(Location loc)
	{
		int x2 = (int) (loc.getX() * 2);
		int y2 = (int) (loc.getY() * 2);
		int z2 = (int) (loc.getZ() * 2);
		loc.setX(x2 / 2.0);
		loc.setY(y2 / 2.0);
		loc.setZ(z2 / 2.0);
		int yaw = (int) (loc.getYaw() / 15);
		int pitch = (int) (loc.getPitch() / 15);
		loc.setYaw(yaw * 15);
		loc.setPitch(pitch * 15);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
	{
		List<String> no = Collections.emptyList();
		if (args.length == 1)
		{
			return T.startsWith(args[0], "sumo", "redrover", "brackets", "rod", "lms", "maze", "waterdrop", "setMainSpawn");
		}
		else if (args[0].equalsIgnoreCase("sumo"))
		{
			if (args.length == 2)
			{
				return T.startsWith(args[1], "list", "delete", "setWinnerCommand", "setPlayer1", "setPlayer2", "setSpectate", "setMinY", "setScoreBoardName");
			}
			else if (args[1].equalsIgnoreCase("setMinY"))
			{
				if (sender instanceof Player)
				{
					return T.startsWith(args[2], "0.0", ((Player) sender).getLocation().getY() + "");
				}
				return T.startsWith(args[2], "0.0");
			}
			else
			{
				return no;
			}
		}
		else if (args[0].equalsIgnoreCase("redrover"))
		{
			if (args.length == 2)
			{
				return T.startsWith(args[1], "list", "delete", "setWinnerCommand", "setPlayer1", "setPlayer2", "setBlock1", "setBlock2", "setSpectate", "setScoreBoardName");
			}
			else
			{
				return no;
			}
		}
		else if (args[0].equalsIgnoreCase("brackets"))
		{
			if (args.length == 2)
			{
				return T.startsWith(args[1], "list", "delete", "setWinnerCommand", "setPlayer1", "setPlayer2", "setSpectate", "setScoreBoardName");
			}
			else
			{
				return no;
			}
		}
		else if (args[0].equalsIgnoreCase("node"))
		{
			if (args.length == 2)
			{
				return T.startsWith(args[1], "list", "delete", "setWinnerCommand", "setPlayer1", "setPlayer2", "setSpectate", "setScoreBoardName");
			}
			else
			{
				return no;
			}
		}
		else if (args[0].equalsIgnoreCase("rod"))
		{
			if (args.length == 2)
			{
				return T.startsWith(args[1], "list", "delete", "setWinnerCommand", "setPlayer1", "setPlayer2", "setSpectate", "setScoreBoardName");
			}
			else
			{
				return no;
			}
		}
		else if (args[0].equalsIgnoreCase("lms"))
		{
			if (args.length == 2)
			{
				return T.startsWith(args[1], "list", "delete", "setWinnerCommand", "setPlayer1", "setPlayer2", "setSpectate", "setScoreBoardName");
			}
			else
			{
				return no;
			}
		}
		else if (args[0].equalsIgnoreCase("maze"))
		{
			if (args.length == 2)
			{
				return T.startsWith(args[1], "list", "delete", "setWinnerCommand", "setPlayer1", "setPlayer2", "setSpectate", "setScoreBoardName");
			}
			else
			{
				return no;
			}
		}
		else if (args[0].equalsIgnoreCase("waterdrop"))
		{
			if (args.length == 2)
			{
				return T.startsWith(args[1], "list", "delete", "setWinnerCommand", "setPlayer1", "setPlayer2", "setBlock1", "setBlock2", "setSpectate", "setScoreBoardName");
			}
			else
			{
				return no;
			}
		}
		else
		{
			return no;
		}
	}

}
