package com.zyrox.events;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public abstract class Event implements Listener
{

	String kills = "%killstats_kills%";
	String deaths = "%killstats_deaths%";
	String streak = "%killstats_streak%";
	String ratio = "%killstats_kdr%";
	protected String name;
	private Main main;
	private int secondsBeforeStart;
	public int sbseconds;
	private boolean sbshown = false;
	public long startCooldown;

	protected Cache cache;

	private boolean running;

	protected boolean supportJoinMidEvent = false;

	public List<Player> players = new ArrayList<>();
//	ScoreboardWrapper initSB1 = new ScoreboardWrapper(ChatColor.RED + "" + ChatColor.BOLD + "Events");
//	{
//		initSB1.addLine(T.replace("&7&m----------------"));
//		initSB1.addLine(ChatColor.YELLOW + "Players: " + ChatColor.LIGHT_PURPLE + "" + players.size());
//		initSB1.addBlankSpace();
//		initSB1.addLine(ChatColor.YELLOW + "Starting in: " + ChatColor.LIGHT_PURPLE + "" + 45);
//		initSB1.addLine(T.replace("&7&m----------------"));		
//	}
	public List<Player> specPlayers = new ArrayList<>();
	protected List<Integer> eventwins = new ArrayList<>();
	private boolean countingDown;
	private BukkitTask countdownTask;
	private BukkitTask scoreboard;
	private BukkitTask runsb;
	public Event(String name)
	{
		this(name, 45, 24 * 60 * 60);
	}

	public Event(String name, int secondsBeforeStart, long startCooldown)
	{
		this.name = name;
		this.secondsBeforeStart = secondsBeforeStart;
		this.startCooldown = startCooldown;

		cache = new Cache(name);

		Bukkit.getPluginManager().registerEvents(this, Main.get());
	}

	public String getName()
	{
		return name;
	}

	public String getSimpleName()
	{
		return getName().toLowerCase().replace(' ', '_');
	}

	public boolean isInited()
	{
		return countingDown || running;
	}

	public boolean isRunning()
	{
		return running;
	}

	public void initStart(CommandSender host)
	{
		if (Main.get().getEventManager().getActive() != null)
		{
			T.sendMessage(host, "&cAnother event is currently running, please wait.");
			return;
		}

		countingDown = true;

		if (host instanceof Player)
		{
			Player player = (Player) host;
			if (!canJoin(player, true))
//				 || !addPlayer(player)
			{
				countingDown = false;
				return;
			}

			long last = cache.get(player, "last_hosting", 0);
			long diff = (System.currentTimeMillis() - last) / 1000;
			if (diff < startCooldown && !host.hasPermission("events.bypass_cooldown"))
			{
				countingDown = false;
				diff = startCooldown - diff;
				T.sendMessage(host, "&cYou must wait another &d" + T.formatSeconds(diff) + "\nbefore you can host again.");
				return;
			}
		}
		
//		Bukkit.broadcastMessage("§c§l" + host.getName() + " is hosting a " + name + " §cEvent!");
//		Bukkit.broadcastMessage("§c§lDo /event join to Join or ");
		TextComponent a = new TextComponent(T.replace("&c&l" + host.getName() + " &c&lis hosting a " + name + " event!"));
		TextComponent b = new TextComponent(T.replace("&c&lStarting &cin 45 seconds! &a[Click to join]"));
		a.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(T.replace("&aClick to join")).create()));
		b.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(T.replace("&aClick to join")).create()));
		a.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/event join"));
		b.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/event join"));
		startSb();
		for (Player online : Bukkit.getOnlinePlayers()) {
			online.spigot().sendMessage(a);
			online.spigot().sendMessage(b);
		}

		countdownTask = new BukkitRunnable()
		{
			int seconds = secondsBeforeStart;
			int sbseconds = 46;

			@Override
			public void run()
			{
				if (seconds <= 0)
				{
					countingDown = false;
					this.cancel();
					running = true;
					if (start())
					{
						if (host instanceof Player)
						{
							cache.set((Player) host, "last_hosting", System.currentTimeMillis());
						}
					}
					else
					{
						initFinish();
					}
				}
				else if (seconds == 1) {
					TextComponent a = new TextComponent(T.replace("&c&l" + host.getName() + " &c&lis hosting a " + name + " event!"));
					TextComponent b = new TextComponent(T.replace("&c&lStarting &cin " + seconds + " second! &a[Click to join]"));
					a.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(T.replace("&aClick to join")).create()));
					b.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(T.replace("&aClick to join")).create()));
					a.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/event join"));
					b.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/event join"));
					for (Player online : Bukkit.getOnlinePlayers()) {
						online.spigot().sendMessage(a);
						online.spigot().sendMessage(b);
					}
					for (Player p : players) {
					   p.playSound(p.getLocation(), Sound.NOTE_PLING, 1, 2);
					}
				}
				else if (seconds <= 5 && seconds != 1)
				{
					TextComponent a = new TextComponent(T.replace("&c&l" + host.getName() + " &c&lis hosting a " + name + " event!"));
					TextComponent b = new TextComponent(T.replace("&c&lStarting &cin " + seconds + " seconds! &a[Click to join]"));
					a.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(T.replace("&aClick to join")).create()));
					b.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(T.replace("&aClick to join")).create()));
					a.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/event join"));
					b.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/event join"));
					for (Player online : Bukkit.getOnlinePlayers()) {
						online.spigot().sendMessage(a);
						online.spigot().sendMessage(b);
					}
					for (Player p : players) {
					   p.playSound(p.getLocation(), Sound.NOTE_PLING, 1, 2);
					}
				}
				else if (seconds % 15 == 0 && seconds != 45)
				{
					TextComponent a = new TextComponent(T.replace("&c&l" + host.getName() + " &c&lis hosting a " + name + " event!"));
					TextComponent b = new TextComponent(T.replace("&c&lStarting &cin " + seconds + " seconds! &a[Click to join]"));
					a.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(T.replace("&aClick to join")).create()));
					b.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(T.replace("&aClick to join")).create()));
					a.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/event join"));
					b.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/event join"));
					for (Player online : Bukkit.getOnlinePlayers()) {
						online.spigot().sendMessage(a);
						online.spigot().sendMessage(b);
					}
				}
				seconds--;
			}
		}.runTaskTimer(Main.get(), 0L, 20L);
	}

	public boolean isSupportJoinMidEvent()
	{
		return supportJoinMidEvent;
	}

	public abstract boolean start();

	public abstract void finish();

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		if (players.contains(event.getPlayer()))
		{
			hideInitBoard(event.getPlayer());
			removePlayer(event.getPlayer());
			Player player = event.getPlayer();
			for (PotionEffect effect : player.getActivePotionEffects()) {
		        player.removePotionEffect(effect.getType());
			}
		}
	}

	public void initFinish()
	{
		finish();
		for (Player p : players)
		{
			hideInitBoard(p);
			p.teleport(Main.get().getEventManager().getMainSpawn());
			for (PotionEffect effect : p.getActivePotionEffects()) {
		        p.removePotionEffect(effect.getType());
			}
			p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		}
		for (Player p : specPlayers)
		{
			hideInitBoard(p);
			p.teleport(Main.get().getEventManager().getMainSpawn());
			p.getInventory().clear();
			for (PotionEffect effect : p.getActivePotionEffects()) {
		        p.removePotionEffect(effect.getType());
			}
			p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		}
		players.clear();
		specPlayers.clear();
		if (countdownTask != null)
		{
			countdownTask.cancel();
		}
		running = false;
		countingDown = false;
		scoreboard.cancel();
		
	}
	
	private boolean isVanished(Player player) {
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }
        return false;
}
	
	public void showInitBoard(Player player) {
//		player.setScoreboard(initSB1.getScoreboard());
	}
	
	public void hideInitBoard(Player player) {
//		player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
	}
	
	public void updateSb(int sec) {
		String formatBoardSec = T.formatSeconds(sec);
//		initSB1.setLine(3, ChatColor.YELLOW + "Starting in: " + ChatColor.LIGHT_PURPLE + "" + formatBoardSec);
		return;
	}
	
	public void startSb() {
		scoreboard = Bukkit.getScheduler().runTaskTimer(Main.get(), new Runnable() {
			int seconds = 45;
			@Override
			public void run() {
				if (seconds > 0) {
					seconds--;
					updateSb(seconds + 1);
				}
				else {
					scoreboard.cancel();
					return;
				}
			}
		}, 0L, 20L);
	}

	public void broadcast(String msg)
	{
		for (Player p : players)
		{
			T.sendMessage(p, msg);
		}
	}

	public boolean canJoin(Player player, boolean notify)
	{
		if (!supportJoinMidEvent && isRunning())
		{
			if (notify)
			{
				T.sendMessage(player, "&cThe event has already started.");

			}
			return false;
		}
		if (isVanished(player))
		{
			if (notify)
			{
				T.sendMessage(player, "&cYou cannot join the event while vanished.");
			}
			return false;
		}
		return true;
	}
	
	public boolean canSpec(Player player, boolean notify)
	{
		if (isRunning())
		{
			if (isVanished(player))
			{
				if (notify)
				{
					T.sendMessage(player, "&cYou cannot join the event while vanished.");
				}
				return false;
			}
		}
		return true;
	}
	
	public boolean addSpec(Player player)
	{
		if (!canSpec(player, false))
		{
			return false;
		}
		specPlayers.add(player);
		player.getInventory().clear();
		if (player.isFlying()) {
			player.setFlying(false);
		}
		if (!isRunning()) {
			showInitBoard(player);
			updateInitStats();
		}
		for (PotionEffect effect : player.getActivePotionEffects()) {
	        player.removePotionEffect(effect.getType());
		}
		return true;
	}
	
	public boolean removeSpec(Player player)
	{
		if (specPlayers.contains(player))
		{
			specPlayers.remove(player);
			if (player.isFlying()) {
				player.setFlying(false);
			}
			player.getInventory().clear();
			hideInitBoard(player);
			player.teleport(Main.get().getEventManager().getMainSpawn());
			for (PotionEffect effect : player.getActivePotionEffects()) {
		        player.removePotionEffect(effect.getType());
			}
			return true;
		}
		return false;
	}
	
	public void updateInitStats() {
//		initSB1.setLine(1, ChatColor.YELLOW + "Players: " + ChatColor.LIGHT_PURPLE + "" + players.size());;
	}
	
	public boolean addPlayer(Player player)
	{
		if (!canJoin(player, false))
		{
			return false;
		}
		player.getInventory().clear();
		if (player.isFlying()) {
			player.setFlying(false);
		}
		players.add(player);
		showInitBoard(player);
		updateInitStats();
		for (PotionEffect effect : player.getActivePotionEffects()) {
	        player.removePotionEffect(effect.getType());
		}
		return true;
	}
	
	public boolean removePlayer(Player player)
	{
		if (players.contains(player))
		{
			players.remove(player);
			if (player.isFlying()) {
				player.setFlying(false);
			}
			player.getInventory().clear();
			hideInitBoard(player);
			updateInitStats();
			player.getActivePotionEffects().clear();
			for (PotionEffect effect : player.getActivePotionEffects()) {
		        player.removePotionEffect(effect.getType());
			}
			player.teleport(Main.get().getEventManager().getMainSpawn());
			return true;
		}
		return false;
	}

	public boolean isPlaying(Player player)
	{
		return players.contains(player);
	}
	
	public boolean isSpectating(Player player)
	{
		return specPlayers.contains(player);
	}
	
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event)
	{
		if (isPlaying(event.getPlayer()))
		{
			String plname = Main.get().getDescription().getName().toLowerCase();
			String cmd = event.getMessage().split(" ")[0].toLowerCase();
			if (cmd.isEmpty())
			{
				return;
			}
			cmd = cmd.substring(1);
			if (cmd.startsWith(plname + ":"))
			{
				cmd = cmd.substring(plname.length() + 1);
			}
			PluginCommand command = Main.get().getCommand("events");

			boolean match = command.getName().equalsIgnoreCase(cmd);

			if (!match)
			{
				for (String a : command.getAliases())
				{
					if (cmd.equalsIgnoreCase(a) || (cmd.equalsIgnoreCase("sv") || cmd.equalsIgnoreCase("ban") || cmd.equalsIgnoreCase("kick") || cmd.equalsIgnoreCase("fly") || cmd.equalsIgnoreCase("gm") || cmd.equalsIgnoreCase("pex") || cmd.equalsIgnoreCase("msg") || cmd.equalsIgnoreCase("r") || cmd.equalsIgnoreCase("tpm") || cmd.equalsIgnoreCase("ping")))
					{
						match = true;
						break;
					}
				}
			}

			if (!match)
			{
				event.setCancelled(true);
				T.sendMessage(event.getPlayer(), "&cYou're not alllowed to use that command while in an event.");
			}
		}
		if (isSpectating(event.getPlayer()))
		{
			String plname = Main.get().getDescription().getName().toLowerCase();
			String cmd = event.getMessage().split(" ")[0].toLowerCase();
			if (cmd.isEmpty())
			{
				return;
			}
			cmd = cmd.substring(1);
			if (cmd.startsWith(plname + ":"))
			{
				cmd = cmd.substring(plname.length() + 1);
			}
			PluginCommand command = Main.get().getCommand("events");

			boolean match = command.getName().equalsIgnoreCase(cmd);

			if (!match)
			{
				for (String a : command.getAliases())
				{
					if (cmd.equalsIgnoreCase(a) || (cmd.equalsIgnoreCase("sv") || cmd.equalsIgnoreCase("ban") || cmd.equalsIgnoreCase("kick") || cmd.equalsIgnoreCase("fly") || cmd.equalsIgnoreCase("gm") || cmd.equalsIgnoreCase("pex") || cmd.equalsIgnoreCase("msg") || cmd.equalsIgnoreCase("r") || cmd.equalsIgnoreCase("tpm") || cmd.equalsIgnoreCase("sc") || cmd.equalsIgnoreCase("adminchat") || cmd.equalsIgnoreCase("managerchat") || cmd.equalsIgnoreCase("ping") || cmd.equalsIgnoreCase("staffmode")))
					{
						match = true;
						break;
					}
				}
			}

			if (!match)
			{
				event.setCancelled(true);
				T.sendMessage(event.getPlayer(), "&cYou're not alllowed to use that command while in an event.");
			}
		}
	}

}
