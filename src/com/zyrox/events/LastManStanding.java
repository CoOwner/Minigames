package com.zyrox.events;

import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class LastManStanding extends Event implements Listener
{
	
	public static boolean isWithinRegion(Player player, String region)
    { 
		return isWithinRegion(player.getLocation(), region);
    }

	public static boolean isWithinRegion(Block block, String region)
    { 
		return isWithinRegion(block.getLocation(), region); 
		}

	public static boolean isWithinRegion(Location loc, String region)
	{
		WorldGuardPlugin guard = getWorldGuard();
		com.sk89q.worldedit.Vector v = toVector(loc);
		RegionManager manager = guard.getRegionManager(loc.getWorld());
		ApplicableRegionSet set = manager.getApplicableRegions(v);
		for (ProtectedRegion each : set) {
			if (each.getId().equalsIgnoreCase(region)) {
				return true;
			}
		}
		return false;
	}
	
	private static WorldGuardPlugin getWorldGuard() {
	    Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
	 
	    // WorldGuard may not be loaded
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        System.out.println("Error: WorldGuard not installed!");
	        return null; // Maybe you want throw an exception instead
	    }
	 
	    return (WorldGuardPlugin) plugin;
	}

	public static class Arena
	{
		private String name;
		private String boardname;
		private Location p1;
		private Location p2;
		private Location spectate;

		public Arena(String name, String boardname, Location p1, Location p2, Location spectate)
		{
			this.name = name;
			this.boardname = boardname;
			this.p1 = p1;
			this.p2 = p2;
			this.spectate = spectate;

			if (p1 != null && p2 != null)
			{
				Vector diff = p2.toVector().subtract(p1.toVector());
				p1.setDirection(diff);
				p2.setDirection(diff.multiply(-1));
			}
		}

		public Arena(String name)
		{
			this.name = name;
		}

		public String getName()
		{
			return name;
		}

		public String getFirstInvalid()
		{
			if (p1 == null)
			{
				return "LMS Spawn Location 1 ";
			}
			if (p2 == null)
			{
				return "LMS Spawn Location 2";
			}
			if (spectate == null)
			{
				return "Spectate location";
			}
			return null;
		}

		public boolean isValid()
		{
			return getFirstInvalid() == null;
		}
	}

	private List<Player> alivePlayers = new ArrayList<>();
	private List<Player> deadPlayers = new ArrayList<>();
	private Arena arena;
	private List<Arena> arenas;
	private BukkitTask countdownTask;
	private boolean countingDown;
	private boolean pvpon;
	private boolean startgame;
	private String winnerCommand;
	private Random Rand = new Random(3);
//	private ScoreboardWrapper eventSB = new ScoreboardWrapper(ChatColor.RED + "" + ChatColor.BOLD + "Events");
//	{
//		eventSB.addLine(T.replace("&7&m----------------")); // 0
//		eventSB.addLine(T.replace("&ePlayers: &d" + (players.size() - deadPlayers.size()))); // 1
//		eventSB.addLine(T.replace("&Spectators: &d" + (specPlayers.size() + deadPlayers.size()))); // 2
//		eventSB.addLine(T.replace("&7&m----------------")); // 3
//	}
//	SidebarString line1 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//	SidebarString line2 = new SidebarString(ChatColor.YELLOW + "" + name.toString());
//	SidebarString line3 = new SidebarString("       ");
//	SidebarString line4 = new SidebarString(ChatColor.YELLOW + "PvP: " + ChatColor.LIGHT_PURPLE + seconds);
//	int getPlayer = alivePlayers.size();
//	int getDead = deadPlayers.size();
//	SidebarString line5 = new SidebarString(ChatColor.YELLOW + "Players: " + ChatColor.LIGHT_PURPLE + "" + getPlayer);
//	SidebarString line6 = new SidebarString(ChatColor.YELLOW + "Dead: " + ChatColor.LIGHT_PURPLE + "" + getDead);
//	SidebarString line7 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//	Sidebar event = new Sidebar(ChatColor.RED + "" + ChatColor.BOLD + arena.boardname, Main.get(), 60, line1, line2, line3, line4, line5, line6, line7);

	private ItemStack leaveItem;
	private ItemStack sword;
	private ItemStack bow;
	private ItemStack arrows;
	private ItemStack crapples;
	private ItemStack boots;
	private ItemStack legs;
	private ItemStack chest;
	private ItemStack helm;

	public LastManStanding(List<Arena> arenas, String winnerCommand)
	{
		super("LMS");
		this.arenas = arenas;
		this.winnerCommand = winnerCommand;
		if (winnerCommand == null)
		{
			Main.get().getLogger().warning("No winner command set for lms.");
		}

		leaveItem = new ItemStack(Material.NETHER_STAR);
		ItemMeta im = leaveItem.getItemMeta();
		im.setDisplayName("�7� �c�lLeave Event �7�");
		leaveItem.setItemMeta(im);
		sword = new ItemStack(Material.IRON_SWORD);
		ItemMeta im2 = sword.getItemMeta();
		im2.spigot().setUnbreakable(true);
		im2.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
		bow = new ItemStack(Material.BOW);
		arrows = new ItemStack(Material.ARROW);
		crapples = new ItemStack(Material.GOLDEN_APPLE);
		boots = new ItemStack(Material.IRON_BOOTS);
		legs = new ItemStack(Material.IRON_LEGGINGS);
		chest = new ItemStack(Material.IRON_CHESTPLATE);
		helm = new ItemStack(Material.IRON_HELMET);
		helm.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
		chest.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
		legs.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
		boots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
		crapples.setAmount(8);
		arrows.setAmount(15);
		sword.setItemMeta(im2);
	}

	public Arena getArena(String name)
	{
		return arenas.stream().filter(a -> a.name.equals(name)).findFirst().orElseGet(() -> null);
	}

	public void selectRandomArena(String name)
	{
		arena = null;
		List<Arena> validArenas = new ArrayList<>();
		for (Arena a : arenas)
		{
			if (a.isValid())
			{
				if (a.name.equals(name))
				{
					arena = a;
					return;
				}
				validArenas.add(a);
			}
		}
		if (!validArenas.isEmpty())
		{
			Random r = ThreadLocalRandom.current();
			arena = validArenas.get(r.nextInt(validArenas.size()));
		}
	}

	@Override
	public boolean start()
	{
		if (players.size() <= 1)
		{
			broadcast("&eNot enough players joined the event.");
			initFinish();
			return false;
		}
		alivePlayers.addAll(players);
		for (Player p : alivePlayers) {
			p.getOpenInventory().getTopInventory().clear();
			if (p.getOpenInventory() != null) {
				p.closeInventory();
			}
			p.getInventory().removeItem(leaveItem);
			p.getInventory().setItem(0, sword);
			p.getInventory().setItem(1, bow);
			p.getInventory().setItem(2, crapples);
			p.getInventory().setItem(3, arrows);
			p.getInventory().setBoots(boots);
			p.getInventory().setLeggings(legs);
			p.getInventory().setChestplate(chest);
			p.getInventory().setHelmet(helm);
			p.teleport(arena.p1);
		}
//		for (Player p : players) {
//			showEventBoard(p);
//		}
//		for (Player p : specPlayers) {
//			showEventBoard(p);
//		}
//		updateEventStatsP();
		next();
		startgame = false;
		pvpon = false;

		return true;
	}

	public boolean isCountingDown()
	{
		return countingDown;
	}
	
//	public void updateEventStatsP() {
//		eventSB.setLine(1, T.replace("&ePlayers: &d" + (players.size() - deadPlayers.size())));
//		eventSB.setLine(2, T.replace("&eSpectators: &d" + (specPlayers.size() + deadPlayers.size())));
//	}

	public void next()
	{
		if (alivePlayers.isEmpty())
		{
			initFinish();
			return;
		}
		
		if (!startgame) {
			for (Player p : alivePlayers) {
				p.getInventory().removeItem(leaveItem);
//				SidebarString line1 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//				SidebarString line2 = new SidebarString(ChatColor.YELLOW + "" + name.toString());
//				SidebarString line3 = new SidebarString("       ");
//				int getPlayer = alivePlayers.size();
//				int getDead = deadPlayers.size();
//				SidebarString line4 = new SidebarString(ChatColor.YELLOW + "Players: " + ChatColor.LIGHT_PURPLE + "" + getPlayer);
//				SidebarString line5 = new SidebarString(ChatColor.YELLOW + "Dead: " + ChatColor.LIGHT_PURPLE + "" + getDead);
//				SidebarString line6 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//				Sidebar event = new Sidebar(ChatColor.RED + "" + ChatColor.BOLD + arena.boardname, Main.get(), 60, line1, line2, line3, line4, line5, line6);
//				event.update();
//				event.showTo(p);
			}
			broadcast("&eStarting a " + name.toLowerCase() + " match.");
			countingDown = true;
			countdownTask = new BukkitRunnable()
			{
				int seconds = 5;
				
				@Override
				public void run() {
					if (seconds == 0) {
						pvpon = true;
						broadcast("&eThe match has started!");
						for (int i = 0; i < alivePlayers.size(); i++) {
							Player p = alivePlayers.get(i);
							p.playSound(p.getLocation(), Sound.FIREWORK_BLAST, 1, 1);
						}
						startgame = true;
						countingDown = false;
						this.cancel();
						return;
					}
					else {			
						if (seconds <= 5) {
							String formatBoardSec = T.formatSeconds(seconds);
							broadcast("&eThe match starts in &d" + formatBoardSec + " &eseconds...");
							for (int i = 0; i < alivePlayers.size(); i++) {
								Player p = alivePlayers.get(i);
								p.playSound(p.getLocation(), Sound.NOTE_PLING, 1, 2);
							}
							seconds--;
						}
					}
				}
			}.runTaskTimer(Main.get(), 0, 20);
		}
		
		if (alivePlayers.size() == 1 && !deadPlayers.isEmpty()) {
			initFinish();
			return;
		}
	}
	
	public void die(Player player)
	{
		if (alivePlayers.remove(player))
		{
			deadPlayers.add(player);
//			SidebarString line1 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//			SidebarString line2 = new SidebarString(ChatColor.YELLOW + "" + name.toString());
//			SidebarString line3 = new SidebarString("       ");
//			SidebarString line4 = new SidebarString(ChatColor.YELLOW + "PvP: " + ChatColor.LIGHT_PURPLE + "Enabled");
//			int getPlayer = alivePlayers.size();
//			int getDead = deadPlayers.size();
//			SidebarString line5 = new SidebarString(ChatColor.YELLOW + "Players: " + ChatColor.LIGHT_PURPLE + "" + getPlayer);
//			SidebarString line6 = new SidebarString(ChatColor.YELLOW + "Dead: " + ChatColor.LIGHT_PURPLE + "" + getDead);
//			SidebarString line7 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//			Sidebar event = new Sidebar(ChatColor.RED + "" + ChatColor.BOLD + arena.boardname, Main.get(), 60, line1, line2, line3, line4, line5, line6, line7);
//			for (int i = 0; i < alivePlayers.size(); i++) {
//				Player p = alivePlayers.get(i);
//				event.update();
//				event.showTo(p);
//				event.showTo(player);
//			}
			player.teleport(arena.spectate);
			player.getInventory().setItem(8, leaveItem);
			T.sendMessage(player, "&eYou have been eliminated.");
			T.sendMessage(player, "&eType " + T.g("&d/e leave") + " &eto leave.");
//			showEventBoard(player);
//			updateEventStatsP();
			next();
		}
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent event)
	{
		if (arena == null)
		{
			return;
		}
	}

	private boolean isFull(ItemStack... items)
	{
		for (ItemStack item : items)
		{
			if (item != null && item.getType() != Material.AIR)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void initStart(CommandSender host)
	{
		selectRandomArena(null);
		if (arena == null)
		{
			T.sendMessage(host, "&cNo valid arenas for " + name + " found.");
			return;
		}
		super.initStart(host);
	}

	@Override
	public boolean canJoin(Player player, boolean notify)
	{
		boolean join = super.canJoin(player, notify);
		if (!join)
		{
			return false;
		}
		
		if (players.contains(player))
		{
			if (notify)
			{
				T.sendMessage(player, "&cYou have already participating in the currently active event.");
			}
			return false;
		}
		if (specPlayers.contains(player))
		{
			if (notify)
			{
				T.sendMessage(player, "&cYou cannot participate as you're already spectating in the currently active event.");
			}
			return false;
		}
		
		if (player.isFlying()) {
			player.setFlying(false);
		}

		// XXX: Add offhand-support
		if (isFull(player.getInventory().getArmorContents()) || isFull(player.getInventory().getContents()))
		{
			if (notify)
			{
				T.sendMessage(player, "&cYou need to clear your inventory first");
			}
			return false;
		}
		return true;
	}
	
	@Override
	public boolean addSpec(Player player)
	{
		if (super.addSpec(player))
		{
			T.sendMessage(player, "&eYou are now spectating the event &d" + name + "&e.");
			player.teleport(arena.spectate);
			player.getOpenInventory().getTopInventory().clear();
			player.getInventory().clear();
			player.getInventory().setItem(8, leaveItem.clone());
			if (isRunning()) {
//				showEventBoard(player);
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean removeSpec(Player player)
	{
//		hideEventBoard(player);
		player.getInventory().remove(leaveItem);
		player.getOpenInventory().getTopInventory().clear();
		player.getInventory().clear();
		specPlayers.remove(player);
		player.teleport(Main.get().getEventManager().getMainSpawn());
		return super.removeSpec(player);
	}

    @Override
	public boolean canSpec(Player player, boolean notify)
	{
		boolean spec = super.canSpec(player, notify);
		if (!spec)
		{
			return false;
		}
		
		if (players.contains(player))
		{
			if (notify)
			{
				T.sendMessage(player, "&cYou cannot spectate as you're already participating in the currently active event.");
			}
			return false;
		}
		if (specPlayers.contains(player))
		{
			if (notify)
			{
				T.sendMessage(player, "&cYou have already spectating in the currently active event.");
			}
			return false;
		}
		
		if (player.isFlying()) {
			player.setFlying(false);
		}



		// XXX: Add offhand-support
		if (isFull(player.getInventory().getArmorContents()) || isFull(player.getInventory().getContents()))
		{
			if (notify)
			{
				T.sendMessage(player, "&cYou need to clear your inventory first");
			}
			return false;
		}
		return true;
	}

	public List<Arena> getArenas()
	{
		return Collections.unmodifiableList(arenas);
	}
//	
//	public void showEventBoard(Player player) {
//		player.setScoreboard(this.eventSB.getScoreboard());
//	}
//	
//	public void hideEventBoard(Player player) {
//		player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
//	}

	@Override
	public boolean addPlayer(Player player)
	{
		if (super.addPlayer(player))
		{
			if (Main.get().getFilemsgs().getString("Join-Event") != null) {
				T.bMsg(Main.get().getFilemsgs().getString("Join-Event").replaceAll("%player%", player.getName()));
			}
			else {
				T.bMsg("&a&l" + player.getName() + " has joined the event.");
			}
			player.teleport(arena.spectate);
			player.getOpenInventory().getTopInventory().clear();
			player.getInventory().clear();
			player.getInventory().setItem(8, leaveItem.clone());
			T.sendMessage(player, "&e&oThe &d&oLastManStanding &e&oEvent.");
			T.sendMessage(player, "&e&oFair &3&oFreeForAll &e&owith &d&oPvP &e&oKit in a Inclosed Arena.");
			T.sendMessage(player, "&e&oWho ever &d&osurvives &e&ois the &3&oWinner&e&o.");
//			SidebarString line1 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//			SidebarString line2 = new SidebarString(ChatColor.YELLOW + "" + name.toString());
//			SidebarString line3 = new SidebarString("       ");
//			int getPlayer = players.size();
//			SidebarString line4 = new SidebarString(ChatColor.YELLOW + "Players: " + ChatColor.LIGHT_PURPLE + "" + getPlayer);
//			SidebarString line5 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//			Sidebar event = new Sidebar(ChatColor.RED + "" + ChatColor.BOLD + arena.boardname, Main.get(), 60, line1, line2, line3, line4, line5);
//			for (int i = 0; i < players.size(); i++) {
//				Player p = players.get(i);
//				event.update();
//				event.showTo(p);
//				event.showTo(player);
//			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean removePlayer(Player player)
	{
		if (Main.get().getFilemsgs().getString("Leave-Event") != null) {
			broadcast(Main.get().getFilemsgs().getString("Leave-Event").replaceAll("%player%", player.getName()));
		}
		else {
			broadcast("&c&l" + player.getName() + " has left the event.");
		}
//		hideEventBoard(player);
//		updateEventStatsP();
		die(player);
		player.getInventory().remove(leaveItem);
		alivePlayers.remove(player);
		deadPlayers.remove(player);
		specPlayers.remove(player);
		player.teleport(Main.get().getEventManager().getMainSpawn(), TeleportCause.PLUGIN);
		player.getOpenInventory().getTopInventory().clear();
		player.getInventory().clear();
		player.getActivePotionEffects().clear();
		return super.removePlayer(player);
	}

	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event)
	{
		if (isPlaying(event.getPlayer()) || specPlayers.contains(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event)
	{
		if (leaveItem.equals(event.getItem()) && players.contains(event.getPlayer()))
		{
			removePlayer(event.getPlayer());
			event.getPlayer().getInventory().clear();
		}
		if (leaveItem.equals(event.getItem()) && specPlayers.contains(event.getPlayer()))
		{
			removeSpec(event.getPlayer());
			event.getPlayer().getInventory().clear();
		}
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event)
	{
		if (isPlaying(event.getEntity()) || specPlayers.contains(event.getEntity()))
		{
			event.getEntity().getInventory().remove(leaveItem);
			event.getEntity().spigot().respawn();
			event.getEntity().getInventory().clear();
			event.setKeepInventory(true);
			event.getEntity().getInventory().setBoots(new ItemStack(Material.AIR));
			event.getEntity().getInventory().setLeggings(new ItemStack(Material.AIR));
			event.getEntity().getInventory().setChestplate(new ItemStack(Material.AIR));
			event.getEntity().getInventory().setHelmet(new ItemStack(Material.AIR));
			event.getEntity().getInventory().clear();
			die(event.getEntity());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRespawn(PlayerRespawnEvent event)
	{
		if (isPlaying(event.getPlayer()) || specPlayers.contains(event.getPlayer()))
		{
			if (alivePlayers.contains(event.getPlayer()) && isCountingDown())
			{
				event.setRespawnLocation(event.getPlayer().getLocation());
			}
			else
			{
				event.setRespawnLocation(arena.spectate);
//				event.getPlayer().setScoreboard(eventSB.getScoreboard());
				if (event.getPlayer().getInventory().getSize() == 0) {
					event.getPlayer().getInventory().setItem(8, leaveItem);
				}
			}
		}
	}

	@EventHandler
	public void onAlivePlayerDamage(EntityDamageByEntityEvent event)
	{
		if (event.getDamager() instanceof Player)
		{
			if (event.getCause() == DamageCause.CUSTOM || event.getCause() == DamageCause.VOID)
			{
				return;
			}

			Player p = (Player) event.getDamager();
			if (event.getEntity() instanceof Player) {
				Player victim = (Player) event.getEntity();
				if (specPlayers.contains(p))
				{
					event.setCancelled(true);
				}
				if (specPlayers.contains(p) && isPlaying(victim)) {
					event.setCancelled(true);
				}
				if (isPlaying(p))
				{
					if (alivePlayers.contains(p))
					{
						if (pvpon)
						{
							Damageable dvictim = victim;
							if (dvictim.getHealth() == 0) {
								broadcast("&d" + victim.getName() + " &ewas eliminated by &d" + p.getName() + "&e. (&d" + (players.size() - deadPlayers.size()) + "&e)");
							}
							return;
						}
						else
						{
							event.setCancelled(true);
						}
					}
					else
					{
						event.setCancelled(true);
					}
				}
			}
		}
	}

	@Override
	public void finish()
	{
		if (alivePlayers.size() == 1)
		{
			Player winner = alivePlayers.get(0);
			int joins = Main.get().getFilestats().getInt("Event-Stats." + winner.getUniqueId().toString() + "." + name) + 1;
			Main.get().getFilestats().set("Event-Stats." + winner.getUniqueId().toString() + "." + name, joins);
			Main.get().saveFileStats();
			Main.get().reloadFileStats();
			winner.getInventory().clear();
			winner.getInventory().setBoots(new ItemStack(Material.AIR));
			winner.getInventory().setLeggings(new ItemStack(Material.AIR));
			winner.getInventory().setChestplate(new ItemStack(Material.AIR));
			winner.getInventory().setHelmet(new ItemStack(Material.AIR));
			winner.getInventory().clear();
			
			//COMMAND AND MESSAGE
			if (Main.get().getFilemsgs().getString("Award-Message") != null) {
				String msg = Main.get().getFilemsgs().getString("Award-Message").replaceAll("%player%", winner.getName());
				T.sendMessage(winner, msg);
			}
			String cmd = winnerCommand;
			if (cmd != null)
			{
				cmd = winnerCommand.replace("{name}", winner.getName()).replace("{uuid}", winner.getUniqueId().toString());
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
			}
			
			//ANNOUNCE
			for (Player bg : Bukkit.getOnlinePlayers()) {
				T.sendMessage(bg, "&d" + winner.getName() + " &ewon the &d" + name + " &eevent!");
				T.sendMessage(bg, "&d" + winner.getName() + " &ewon the &d" + name + " &eevent!");
				T.sendMessage(bg, "&d" + winner.getName() + " &ewon the &d" + name + " &eevent!");
				T.sendMessage(bg, "&d" + winner.getName() + " &ewon the &d" + name + " &eevent!");
			}
		}
		for (int i = 0; i < specPlayers.size(); i++) {
			Player p = specPlayers.get(i);
//			hideEventBoard(p);
			p.getInventory().remove(leaveItem);
			p.getOpenInventory().getTopInventory().clear();
			if (p.getOpenInventory() != null) {
				p.closeInventory();
			}
			p.getInventory().clear();
			p.teleport(Main.get().getEventManager().getMainSpawn());
			for (PotionEffect effect : p.getActivePotionEffects()) {
				p.removePotionEffect(effect.getType());
			}
			p.teleport(Main.get().getEventManager().getMainSpawn());
			p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		}
		for (Player p : players)
		{
			p.getInventory().remove(leaveItem);
			p.getInventory().setBoots(new ItemStack(Material.AIR));
			p.getInventory().setLeggings(new ItemStack(Material.AIR));
			p.getInventory().setChestplate(new ItemStack(Material.AIR));
			p.getInventory().setHelmet(new ItemStack(Material.AIR));
			p.setHealth(20);
			p.getOpenInventory().getTopInventory().clear();
			if (p.getOpenInventory() != null) {
				p.closeInventory();
			}
			p.getInventory().clear();
			p.teleport(Main.get().getEventManager().getMainSpawn());
			for (PotionEffect effect : p.getActivePotionEffects()) {
				p.removePotionEffect(effect.getType());
			}
//			hideEventBoard(p);
//			SidebarString line1 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//			SidebarString line2 = new SidebarString(ChatColor.YELLOW + "" + name.toString());
//			SidebarString line3 = new SidebarString("       ");
//			int getPlayer = players.size();
//			SidebarString line4 = new SidebarString(ChatColor.YELLOW + "Players: " + ChatColor.LIGHT_PURPLE + "" + getPlayer);
//			SidebarString line5 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//			Sidebar event = new Sidebar(ChatColor.RED + "" + ChatColor.BOLD + arena.boardname, Main.get(), 60, line1, line2, line3, line4, line5);
//			event.hideFrom(p);
			p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		}
		alivePlayers.clear();
		deadPlayers.clear();
		specPlayers.clear();
		players.clear();
		if (countdownTask != null)
		{
			countdownTask.cancel();
		}
		countingDown = false;
		startgame = false;
		pvpon = false;
	}

}

