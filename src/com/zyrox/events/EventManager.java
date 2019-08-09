package com.zyrox.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

public class EventManager
{
	private List<Event> events = new ArrayList<>();

	private Location mainSpawn;

	public EventManager()
	{
		Main pl = Main.get();
		try
		{
			mainSpawn = readLocation(pl.getConfig(), "main_spawn");
		}
		catch (Exception e)
		{
			Main.get().getLogger().log(Level.WARNING, "Error while loading main spawn location: " + e.getLocalizedMessage(), e);
			return;
		}
		tryLoad("Sumo", () ->
		{
			String cmd = pl.getConfig().getString("sumo.winner_command");
			events.add(new Sumo(readArenas("sumo", false), cmd));
		});
		tryLoad("RedRover", () ->
		{
			String cmd = pl.getConfig().getString("redrover.winner_command");
			events.add(new RedRover(readArenas1("redrover", false), cmd));
		});
		tryLoad("Brackets", () ->
		{
			String cmd = pl.getConfig().getString("brackets.winner_command");
			events.add(new Brackets(readArenas2("brackets", false), cmd));
		});
		tryLoad("RoD", () ->
		{
			String cmd = pl.getConfig().getString("rod.winner_command");
			events.add(new RoD(readArenas3("rod", false), cmd));
		});
		tryLoad("LMS", () ->
		{
			String cmd = pl.getConfig().getString("lms.winner_command");
			events.add(new LastManStanding(readArenas4("lms", false), cmd));
		});
		tryLoad("Maze", () ->
		{
			String cmd = pl.getConfig().getString("maze.winner_command");
			events.add(new Maze(readArenas5("maze", false), cmd));
		});
		tryLoad("WaterDrop", () ->
		{
			String cmd = pl.getConfig().getString("wd.winner_command");
			events.add(new WaterDrop(readArenas6("wd", false), cmd));
		});
		tryLoad("Node", () ->
		{
			String cmd = pl.getConfig().getString("node.winner_command");
			events.add(new Node(readArenas7("node", false), cmd));
		});
	}

	public Location getMainSpawn()
	{
		return mainSpawn;
	}

	public List<Event> getEvents()
	{
		return Collections.unmodifiableList(events);
	}

	public static Location readLocation(ConfigurationSection config, String path)
	{
		String[] split = config.getString(path, "").split(",");
		if (split.length != 6)
		{
			throw new IllegalArgumentException("Invalid location format for config key: " + path + ", value: " + config.getString(path));
		}
		World world = Bukkit.getWorld(split[0]);
		if (world == null)
		{
			throw new IllegalArgumentException("No world found by name " + split[0]);
		}
		double x = Double.parseDouble(split[1]);
		double y = Double.parseDouble(split[2]);
		double z = Double.parseDouble(split[3]);
		float yaw = Float.parseFloat(split[4]);
		float pitch = Float.parseFloat(split[5]);
		return new Location(world, x, y, z, yaw, pitch);
	}
	
	public static String readScoreboardString(ConfigurationSection config, String path)
	{
		String[] split = config.getString(path, "").split(",");
		if (split.length != 1)
		{
			throw new IllegalArgumentException("Invalid Scoreboard format for config key: " + path + ", value: " + config.getString(path));
		}
		String boardname = split[1];
		return new String(boardname);
	}

	public static void setLocation(Configuration config, String path, Location value)
	{
		String v = value.getWorld().getName() + "," + value.getX() + "," + value.getY() + "," + value.getZ() + "," + value.getYaw() + "," + value.getPitch();
		config.set(path, v);
	}
	
	public static void setBoardName(Configuration config, String path, String name)
	{
		String v = name.toString();
		config.set(path, v);
	}

	public List<Sumo.Arena> readArenas(String path, boolean silent)
	{
		ConfigurationSection sumo = Main.get().getConfig().getConfigurationSection("sumo");
		List<Sumo.Arena> arenas = new ArrayList<>();
		for (String arena : sumo.getKeys(false))
		{
			if (arena.equals("winner_command"))
			{
				continue;
			}
			String boardname = null;
			Location p1 = null;
			Location p2 = null;
			Location spectate = null;
			double y = Double.NaN;
			try
			{
				boardname = sumo.getString(arena + ".boardname");
				p1 = readLocation(sumo, arena + ".p1");
				p2 = readLocation(sumo, arena + ".p2");
				spectate = readLocation(sumo, arena + ".spectate");
				y = sumo.getDouble(arena + ".minY", Double.NaN);
				if (Double.isNaN(y))
				{
					throw new IllegalArgumentException("MinY not set for " + arena + ".minY");
				}
			}
			catch (Exception e)
			{
				if (!silent)
				{
					Main.get().getLogger().log(Level.WARNING, "Error loading sumo arena " + arena + ": " + e.getLocalizedMessage(), e);
				}
			}
			arenas.add(new Sumo.Arena(arena, boardname, p1, p2, spectate, y));
		}
		return arenas;
	}
	
	public List<RedRover.Arena> readArenas1(String path, boolean silent)
	{
		ConfigurationSection redrover = Main.get().getConfig().getConfigurationSection("redrover");
		List<RedRover.Arena> arenas1 = new ArrayList<>();
		for (String arena : redrover.getKeys(false))
		{
			if (arena.equals("winner_command"))
			{
				continue;
			}
			String boardname = null;
			Location p1 = null;
			Location p2 = null;
			Location b1 = null;
			Location b2 = null;
			Location spectate = null;
			try
			{
				boardname = redrover.getString(arena + ".boardname");
				p1 = readLocation(redrover, arena + ".p1");
				p2 = readLocation(redrover, arena + ".p2");
				b1 = readLocation(redrover, arena + ".b1");
				b2 = readLocation(redrover, arena + ".b2");
				spectate = readLocation(redrover, arena + ".spectate");
				}
			catch (Exception e)
			{
				if (!silent)
				{
					Main.get().getLogger().log(Level.WARNING, "Error loading redrover arena " + arena + ": " + e.getLocalizedMessage(), e);
				}
			}
			arenas1.add(new RedRover.Arena(arena, boardname, p1, p2, b1, b2, spectate));
		}
		return arenas1;
	}
	
	public List<Brackets.Arena> readArenas2(String path, boolean silent)
	{
		ConfigurationSection brackets = Main.get().getConfig().getConfigurationSection("brackets");
		List<Brackets.Arena> arenas2 = new ArrayList<>();
		for (String arena : brackets.getKeys(false))
		{
			if (arena.equals("winner_command"))
			{
				continue;
			}
			String boardname = null;
			Location p1 = null;
			Location p2 = null;
			Location spectate = null;
			try
			{
				boardname = brackets.getString(arena + ".boardname");
				p1 = readLocation(brackets, arena + ".p1");
				p2 = readLocation(brackets, arena + ".p2");
				spectate = readLocation(brackets, arena + ".spectate");
				}
			catch (Exception e)
			{
				if (!silent)
				{
					Main.get().getLogger().log(Level.WARNING, "Error loading brackets arena " + arena + ": " + e.getLocalizedMessage(), e);
				}
			}
			arenas2.add(new Brackets.Arena(arena, boardname, p1, p2, spectate));
		}
		return arenas2;
	}
	
	public List<RoD.Arena> readArenas3(String path, boolean silent)
	{
		ConfigurationSection rod = Main.get().getConfig().getConfigurationSection("rod");
		List<RoD.Arena> arenas1 = new ArrayList<>();
		for (String arena : rod.getKeys(false))
		{
			if (arena.equals("winner_command"))
			{
				continue;
			}
			String boardname = null;
			Location p1 = null;
			Location p2 = null;
			Location spectate = null;
			try
			{
				boardname = rod.getString(arena + ".boardname");
				p1 = readLocation(rod, arena + ".p1");
				p2 = readLocation(rod, arena + ".p2");
				spectate = readLocation(rod, arena + ".spectate");
				}
			catch (Exception e)
			{
				if (!silent)
				{
					Main.get().getLogger().log(Level.WARNING, "Error loading rod arena " + arena + ": " + e.getLocalizedMessage(), e);
				}
			}
			arenas1.add(new RoD.Arena(arena, boardname, p1, p2, spectate));
		}
		return arenas1;
	}
	
	public List<LastManStanding.Arena> readArenas4(String path, boolean silent)
	{
		ConfigurationSection lms = Main.get().getConfig().getConfigurationSection("lms");
		List<LastManStanding.Arena> arenas1 = new ArrayList<>();
		for (String arena : lms.getKeys(false))
		{
			if (arena.equals("winner_command"))
			{
				continue;
			}
			String boardname = null;
			Location p1 = null;
			Location p2 = null;
			Location spectate = null;
			try
			{
				boardname = lms.getString(arena + ".boardname");
				p1 = readLocation(lms, arena + ".p1");
				p2 = readLocation(lms, arena + ".p2");
				spectate = readLocation(lms, arena + ".spectate");
				}
			catch (Exception e)
			{
				if (!silent)
				{
					Main.get().getLogger().log(Level.WARNING, "Error loading lms arena " + arena + ": " + e.getLocalizedMessage(), e);
				}
			}
			arenas1.add(new LastManStanding.Arena(arena, boardname, p1, p2, spectate));
		}
		return arenas1;
	}
	
	public List<Maze.Arena> readArenas5(String path, boolean silent)
	{
		ConfigurationSection maze = Main.get().getConfig().getConfigurationSection("maze");
		List<Maze.Arena> arenas1 = new ArrayList<>();
		for (String arena : maze.getKeys(false))
		{
			if (arena.equals("winner_command"))
			{
				continue;
			}
			String boardname = null;
			Location p1 = null;
			Location p2 = null;
			Location spectate = null;
			try
			{
				boardname = maze.getString(arena + ".boardname");
				p1 = readLocation(maze, arena + ".p1");
				p2 = readLocation(maze, arena + ".p2");
				spectate = readLocation(maze, arena + ".spectate");
				}
			catch (Exception e)
			{
				if (!silent)
				{
					Main.get().getLogger().log(Level.WARNING, "Error loading maze arena " + arena + ": " + e.getLocalizedMessage(), e);
				}
			}
			arenas1.add(new Maze.Arena(arena, boardname, p1, p2, spectate));
		}
		return arenas1;
	}
	
	public List<WaterDrop.Arena> readArenas6(String path, boolean silent)
	{
		ConfigurationSection wd = Main.get().getConfig().getConfigurationSection("wd");
		List<WaterDrop.Arena> arenas1 = new ArrayList<>();
		for (String arena : wd.getKeys(false))
		{
			if (arena.equals("winner_command"))
			{
				continue;
			}
			String boardname = null;
			Location p1 = null;
			Location p2 = null;
			Location b1 = null;
			Location b2 = null;
			Location spectate = null;
			try
			{
				boardname = wd.getString(arena + ".boardname");
				p1 = readLocation(wd, arena + ".p1");
				p2 = readLocation(wd, arena + ".p2");
				b1 = readLocation(wd, arena + ".b1");
				b2 = readLocation(wd, arena + ".b2");
				spectate = readLocation(wd, arena + ".spectate");
				}
			catch (Exception e)
			{
				if (!silent)
				{
					Main.get().getLogger().log(Level.WARNING, "Error loading waterdrop arena " + arena + ": " + e.getLocalizedMessage(), e);
				}
			}
			arenas1.add(new WaterDrop.Arena(arena, boardname, p1, p2, b1, b2, spectate));
		}
		return arenas1;
	}
	public List<Node.Arena> readArenas7(String path, boolean silent)
	{
		ConfigurationSection node = Main.get().getConfig().getConfigurationSection("node");
		List<Node.Arena> arenas2 = new ArrayList<>();
		for (String arena : node.getKeys(false))
		{
			if (arena.equals("winner_command"))
			{
				continue;
			}
			String boardname = null;
			Location p1 = null;
			Location p2 = null;
			Location spectate = null;
			try
			{
				boardname = node.getString(arena + ".boardname");
				p1 = readLocation(node, arena + ".p1");
				p2 = readLocation(node, arena + ".p2");
				spectate = readLocation(node, arena + ".spectate");
				}
			catch (Exception e)
			{
				if (!silent)
				{
					Main.get().getLogger().log(Level.WARNING, "Error loading node arena " + arena + ": " + e.getLocalizedMessage(), e);
				}
			}
			arenas2.add(new Node.Arena(arena, boardname, p1, p2, spectate));
		}
		return arenas2;
	}

	public void tryLoad(String name, Runnable r)
	{
		try
		{
			r.run();
		}
		catch (Exception e)
		{
			Main.get().getLogger().log(Level.WARNING, "Error while loading eventmode " + name + ": " + e.getLocalizedMessage(), e);
		}
	}

	public Event getActive()
	{
		for (Event e : events)
		{
			if (e.isInited())
			{
				return e;
			}
		}
		return null;
	}

}
