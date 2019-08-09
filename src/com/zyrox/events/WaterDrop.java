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
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
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
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import net.minecraft.server.v1_7_R4.AxisAlignedBB;
import net.minecraft.server.v1_7_R4.EntityPlayer;


public class WaterDrop extends Event implements Listener
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
		private Location b1;
		private Location b2;
		private Location spectate;

		public Arena(String name, String boardname, Location p1, Location p2, Location b1, Location b2, Location spectate)
		{
			this.name = name;
			this.boardname = boardname;
			this.p1 = p1;
			this.p2 = p2;
			this.b1 = b1;
			this.b2 = b2;
			this.spectate = spectate;

			if (p1 != null && p2 != null)
			{
				Vector diff = p2.toVector().subtract(p1.toVector());
				p1.setDirection(diff);
				p2.setDirection(diff.multiply(-1));
			}
			if (b1 != null && b2 != null)
			{
				Vector diff = b2.toVector().subtract(b1.toVector());
				b1.setDirection(diff);
				b2.setDirection(diff.multiply(-1));
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
				return "Spawn 1 location";
			}
			if (p2 == null)
			{
				return "Spawn 2 location";
			}
			if (b1 == null)
			{
				return "Block 1 location";
			}
			if (b2 == null)
			{
				return "Block 2 location";
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
	private List<Player> passedPlayers = new ArrayList<>();
	private List<Player> deadPlayers = new ArrayList<>();
	private Arena arena;
	private List<Arena> arenas;
	private BukkitTask countdownTask;
	private BukkitTask positionTask;
	private boolean countingDown;
	private String winnerCommand;
	private Random Rand = new Random(3);
	private boolean startgame;
	private boolean round1to2;
	private boolean round2to10;
	private boolean round10up;
	private int round;
	private boolean block1;
	private boolean block2;
	private boolean block3;
	private boolean block4;
	private boolean block5;
	private boolean block6;
	private boolean block7;
	private boolean block8;
	private boolean block9;
	private ScoreboardWrapper eventSB = new ScoreboardWrapper(ChatColor.RED + "" + ChatColor.BOLD + "Events");
	{
		eventSB.addLine(T.replace("&7&m-------------------")); // 0
		eventSB.addBlankSpace(); // 1
		eventSB.addBlankSpace(); // 2
		eventSB.addBlankSpace(); // 3
		eventSB.addLine(T.replace("&ePlayers: &d" + (players.size() - deadPlayers.size()))); // 4
		eventSB.addLine(T.replace("&eSpectators: &d" + (specPlayers.size() + deadPlayers.size()))); // 5
		eventSB.addLine(T.replace("&7&m-------------------")); // 6
		
	}
//	SidebarString line1 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//	SidebarString line2 = new SidebarString(ChatColor.YELLOW + "" + name.toString());
//	SidebarString line3 = new SidebarString("       ");
//	SidebarString line4 = new SidebarString(ChatColor.YELLOW + "Round: " + ChatColor.LIGHT_PURPLE + "" + round);
//	int getPlayer = alivePlayers.size();
//	int getDead = deadPlayers.size();
//	SidebarString line5 = new SidebarString(ChatColor.YELLOW + "Time: " + ChatColor.LIGHT_PURPLE + formatBoardSec);
//	SidebarString line6 = new SidebarString(ChatColor.YELLOW + "Players: " + ChatColor.LIGHT_PURPLE + "" + getPlayer);
//	SidebarString line7 = new SidebarString(ChatColor.YELLOW + "Dead: " + ChatColor.LIGHT_PURPLE + "" + getDead);
//	SidebarString line8 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//	Sidebar event = new Sidebar(ChatColor.RED + "" + ChatColor.BOLD + arena.boardname, Main.get(), 60, line1, line2, line3, line4, line5, line6, line7, line8);

	private ItemStack leaveItem;

	public WaterDrop(List<Arena> arenas, String winnerCommand)
	{
		super("WaterDrop");
		this.arenas = arenas;
		this.winnerCommand = winnerCommand;
		if (winnerCommand == null)
		{
			Main.get().getLogger().warning("No winner command set for waterdrop.");
		}

		leaveItem = new ItemStack(Material.NETHER_STAR);
		ItemMeta im = leaveItem.getItemMeta();
		im.setDisplayName(T.replace("&c&lLeave Event"));
		leaveItem.setItemMeta(im);
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
		if (Bukkit.getWorld("waterdrop") == null) {
			for (Player p : players)
			{
				p.teleport(Main.get().getEventManager().getMainSpawn());
				broadcast(ChatColor.BOLD + T.gold + "The world waterdrop does not exist. Create the world waterdrop all in lowercase!");
			}
			players.clear();
			alivePlayers.clear();
			deadPlayers.clear();
			if (countdownTask != null)
			{
				countdownTask.cancel();
			}
			countingDown = false;
			countdownTask.cancel();
			return false;
		}
		if (players.size() <= 1)
		{
			broadcast("&eNot enough players joined the event.");
			initFinish();
			return false;
		}
		Collections.shuffle(players);
		alivePlayers.addAll(players);
		startgame = false;
		round1to2 = false;
		round2to10 = false;
		round10up = false;
		round = 1;
		block1 = true;
		block2 = false;
		block3 = false;
		block4 = false;
		block5 = false;
		block6 = false;
		block7 = false;
		block8 = false;
		block9 = false;
		for (Player p : alivePlayers) {
			p.teleport(arena.p1);
			p.getOpenInventory().getTopInventory().clear();
			if (p.getOpenInventory() != null) {
				p.closeInventory();
			}
		}
		for (Player p : players) {
			showEventBoard(p);
		}
		for (Player p : specPlayers) {
			showEventBoard(p);
		}
		updateEventStatsP();
		next();

		return true;
	}

	public boolean isCountingDown()
	{
		return countingDown;
	}
	
	public void updateEventStatsP() {
		eventSB.setLine(4, T.replace("&ePlayers: &d" + (players.size() - deadPlayers.size())));
		eventSB.setLine(5, T.replace("&eSpectators: &d" + (specPlayers.size() + deadPlayers.size())));
	}

	public void next()
	{
		if (startgame) {
			if (alivePlayers.size() == 1 && !deadPlayers.isEmpty())
			{
				initFinish();
				countdownTask.cancel();
				return;
			}
			if (alivePlayers.size() == 1)
			{
				initFinish();
				countdownTask.cancel();
				return;
			}
			if (alivePlayers.isEmpty())
			{
				broadcast(ChatColor.BOLD + T.gold + "No one won WaterDrop!");
				initFinish();
				countdownTask.cancel();
				return;
			}
			startTimerandBlock();
		}
		else {
			for (Player p : alivePlayers) {
				p.teleport(arena.p1, TeleportCause.PLUGIN);
				p.playSound(p.getLocation(), Sound.FIREWORK_BLAST, 1, 1);
			}
			broadcast("&eStarting round &d" + round + "&e.");
			round1to2 = true;
			round2to10 = false;
			round10up = false;
			startTimerandBlock();
//			startCheckingPos();
			startgame = true;
			return;
		}
	}
	
	public void die(Player player)
	{
		if (alivePlayers.remove(player))
		{
			deadPlayers.add(player);
			showEventBoard(player);
			updateEventStatsP();
			player.teleport(arena.spectate);
			player.getInventory().setItem(8, leaveItem);
			T.sendMessage(player, "&eYou have been eliminated.");
			T.sendMessage(player, "&eType " + T.g("&d/e leave") + " &eto leave.");
			broadcast("&d" + player.getName() + " &ewas eliminated. (&d" + (players.size() - deadPlayers.size()) + "&e)");
		}
	}
//	
//	@EventHandler
//	public void onMove(PlayerMoveEvent event)
//	{
//		if (arena == null)
//		{
//			return;
//		}
//	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		CraftPlayer p = (CraftPlayer) event.getPlayer();
		Location playerloc = event.getPlayer().getLocation();
		AxisAlignedBB bb = ((CraftPlayer) p).getHandle().boundingBox;
		double maxY = bb.e;
		double minY = bb.b;
		Block blockDown = player.getLocation().clone().getBlock().getRelative(BlockFace.DOWN);
		if (alivePlayers.size() > 0) {
			if (alivePlayers.contains(player) && !passedPlayers.contains(player)) {
				if (isWithinRegion(player.getLocation(), "inwater")) {
					Material block = player.getLocation().getBlock().getType();
					double minX = bb.a;
					double minZ = bb.c;
					double maxX = bb.d;
					double maxZ = bb.f;
					double playerX = player.getLocation().getX();
					double playerY = player.getLocation().getY();
					double playerZ = player.getLocation().getZ();
					if (minX == playerX && minY == playerY && minZ == playerZ && block == Material.WATER || block == Material.STATIONARY_WATER) {						
						passedPlayers.add(player);
						broadcast("&f" + p.getName() + " &ehas successfully jumped.");
					}
				}
			}
		}
	}
	
	public void spawnBlock() {
		int x1 = arena.b1.getBlockX();
		int y1 = arena.b1.getBlockY();
		int z1 = arena.b1.getBlockZ();
		if (Bukkit.getWorld("waterdrop") != null) {
			Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 - 1).setType(Material.STATIONARY_WATER);
			Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1).setType(Material.STATIONARY_WATER);
			Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 + 1).setType(Material.STATIONARY_WATER);
			Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 + 1).setType(Material.STATIONARY_WATER);
			Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 + 1).setType(Material.STATIONARY_WATER);
			Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1).setType(Material.STATIONARY_WATER);
			Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 - 1).setType(Material.STATIONARY_WATER);
			Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 - 1).setType(Material.STATIONARY_WATER);
			Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1).setType(Material.STATIONARY_WATER);
			
			if (round1to2) { 
				return;
			}
			if (round2to10) {
				int random = ThreadLocalRandom.current().nextInt(1, 11);
				if (random == 1) {
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1).setType(Material.REDSTONE_BLOCK);
				}
				else if (random == 2) {
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
				}
				else if (random == 3) {
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1).setType(Material.REDSTONE_BLOCK);
				}
				else if (random == 4) {
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
				}
				else if (random == 5) {
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
				}
				else if (random == 6) {
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
				}
				else if (random == 7) {
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
				}
				else if (random == 8) {
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
				}
				else if (random == 9) {
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1).setType(Material.REDSTONE_BLOCK);
				}
				else if (random == 10) {
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1).setType(Material.REDSTONE_BLOCK);
				}
				return;
			}
			if (round10up) {
				int random = ThreadLocalRandom.current().nextInt(1, 15);
				if (random == 1) {
					// MIDDLE
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
				}
				else if (random == 2) {
					// TOP RIGHT
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
				}
				else if (random == 3) {
					// TOP LEFT
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
				}
				else if (random == 4) {
					// BOTTOM LEFT
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
				}
				else if (random == 5) {
					// BOTTOM RIGHT
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1).setType(Material.REDSTONE_BLOCK);
				}
				else if (random == 6) {
					// 1 1 DOWN
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
				}
				else if (random == 7) {
					// 1 -1 DOWN
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
				}
				else if (random == 8) {
					// -1 -1 UP
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
				}
				else if (random == 9) {
					// -1 1 UP
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1).setType(Material.REDSTONE_BLOCK);
				}
				else if (random == 10) {
					// 1 1 LEFT
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
				}
				else if (random == 11) {
					// 1 -1 RIGHT
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
				}
				else if (random == 12) {
					// -1 1 LEFT
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
				}
				else if (random == 13) {
					// -1 -1 RIGHT
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
				}
				else if (random == 14) {
					Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 - 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 + 1).setType(Material.REDSTONE_BLOCK);
					Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1).setType(Material.REDSTONE_BLOCK);
				}
				return;
			}
		}
		else {
			broadcast("&eThe world waterdrop does not exist. Create the world waterdrop all in lowercase!");
		}
	}
	
//	public void startCheckingPos() {
//		positionTask = Main.get().getServer().getScheduler().runTaskTimer(Main.get(), new Runnable() {
//			
//			@Override
//			public void run() {
//				for (Player player : players) {
//					CraftPlayer p = (CraftPlayer) player;
//					Location playerloc = player.getLocation();
//					AxisAlignedBB bb = ((CraftPlayer) p).getHandle().getBoundingBox();
//					double maxY = bb.e;
//					double minY = bb.b;
//					Block blockDown = player.getLocation().clone().getBlock().getRelative(BlockFace.DOWN);
//					if (alivePlayers.size() > 0) {
//						if (alivePlayers.contains(player) && !passedPlayers.contains(player)) {
//							if (isWithinRegion(player.getLocation(), "inwater")) {
//								Block block = player.getLocation().getBlock();
//								double minX = bb.a;
//								double minZ = bb.c;
//								double maxX = bb.d;
//								double maxZ = bb.f;
//								if (player.getLocation().getY() == minY && player.getLocation().add(0, -1, 0).getBlock().getType() != Material.WATER || player.getLocation().add(0, -1, 0).getBlock().getType() != Material.STATIONARY_WATER) {
//									if (player.getLocation().getY() == maxY - 1 && player.getLocation().add(0, -1, 0).getBlock().getType() != Material.WATER || player.getLocation().add(0, -1, 0).getBlock().getType() != Material.STATIONARY_WATER) {
//										broadcast(ChatColor.BOLD + T.gold + player.getName() + " Failed Round: " + round);
//										die(player);
//									}
//								}
//								if (player.getLocation().getX() == minX && player.getLocation().getBlock().getType() == Material.WATER || player.getLocation().getBlock().getType() == Material.STATIONARY_WATER) {
//									if (player.getLocation().getZ() == minZ && player.getLocation().getBlock().getType() == Material.WATER || player.getLocation().getBlock().getType() == Material.STATIONARY_WATER) {
//										if (player.getLocation().getX() == maxX && player.getLocation().getBlock().getType() == Material.WATER || player.getLocation().getBlock().getType() == Material.STATIONARY_WATER) {
//											if (player.getLocation().getZ() == maxZ && player.getLocation().getBlock().getType() == Material.WATER || player.getLocation().getBlock().getType() == Material.STATIONARY_WATER) {
//												passedPlayers.add(player);
//												broadcast(ChatColor.BOLD + T.gold + player.getName() + " Passed Round: " + round);
//											}
//										}
//									}
//								}
//	//							if (playerloc.getBlock().getType() == Material.WATER || playerloc.getBlock().getType() == Material.STATIONARY_WATER) {
//	//								broadcast(ChatColor.BOLD + T.gold + player.getName() + " Failed Round: " + round);
//	//								die(player);
//	//							}
//	//							else {
//	//								passedPlayers.add(player);
//	//								broadcast(ChatColor.BOLD + T.gold + player.getName() + " Passed Round: " + round);
//	//							}
//							}
//							if (isWithinRegion(playerloc, "notinwater1") || isWithinRegion(playerloc, "notinwater2") || isWithinRegion(playerloc, "notinwater3") || isWithinRegion(playerloc, "notinwater4")) {
//								Block block = player.getLocation().getBlock();
//								if (blockDown.getType() != Material.STATIONARY_WATER || blockDown.getType() != Material.WATER) {
//									broadcast(ChatColor.BOLD + T.gold + player.getName() + " Failed Round: " + round);
//									die(player);
//								}
//							}
//						}
//					}
//				}
//			}
//		}, 0L, 20L);
//	}
//	
//	public void stopCheckingPos() {
//		if (positionTask != null) {
//			Main.get().getServer().getScheduler().cancelTask(positionTask.getTaskId());
//			positionTask.cancel();
//		}
//		
//	}
	
	public void startTimerandBlock()
	{
		countingDown = true;
//		SidebarString line1 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//		SidebarString line2 = new SidebarString(ChatColor.YELLOW + "" + name.toString());
//		SidebarString line3 = new SidebarString("       ");
//		SidebarString line4 = new SidebarString(ChatColor.YELLOW + "Round: " + ChatColor.LIGHT_PURPLE + "" + round);
//		int getPlayer = alivePlayers.size();
//		int getDead = deadPlayers.size();
//		SidebarString line6 = new SidebarString(ChatColor.YELLOW + "Players: " + ChatColor.LIGHT_PURPLE + "" + getPlayer);
//		SidebarString line7 = new SidebarString(ChatColor.YELLOW + "Dead: " + ChatColor.LIGHT_PURPLE + "" + getDead);
//		SidebarString line8 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//		Sidebar event = new Sidebar(ChatColor.RED + "" + ChatColor.BOLD + arena.boardname, Main.get(), 60, line1, line2, line3, line4, line4, line7);
//		for (int i = 0; i < alivePlayers.size(); i++) {
//			Player p = alivePlayers.get(i);
//			event.update();
//			event.showTo(p);
//		}
		countdownTask = new BukkitRunnable()
		{
			int seconds = 16;
			int seconds2 = 6;
			boolean spawnBlockStop = false;
			
			@Override
			public void run() {
				String formatBoardSec = T.formatSeconds(seconds);
				if (round1to2) {
					if (round == 1) {
						if (seconds == 0) {
							for (int i = 0; i < alivePlayers.size(); i++) {
								Player p = alivePlayers.get(i);
								if (!passedPlayers.contains(p)) {
									die(p);
								}
							}
							for (int i = 0; i < passedPlayers.size(); i++) {
								Player p = passedPlayers.get(i);
								p.teleport(arena.p1, TeleportCause.PLUGIN);
							}
							passedPlayers.clear();
							round++;
							broadcast("&eStarting round &d" + round + "&e.");
							this.cancel();
							countdownTask.cancel();
							next();
							return;
						}
						eventSB.setLine(1, ChatColor.YELLOW + "Round: " + ChatColor.LIGHT_PURPLE + "" + round);
						eventSB.setLine(2, ChatColor.YELLOW + "Time Remaining: " + ChatColor.LIGHT_PURPLE + formatBoardSec);
//						SidebarString line1 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//						SidebarString line2 = new SidebarString(ChatColor.YELLOW + "" + name.toString());
//						SidebarString line3 = new SidebarString("       ");
//						SidebarString line4 = new SidebarString(ChatColor.YELLOW + "Round: " + ChatColor.LIGHT_PURPLE + "" + round);
//						int getPlayer = alivePlayers.size();
//						int getDead = deadPlayers.size();
//						SidebarString line5 = new SidebarString(ChatColor.YELLOW + "Time: " + ChatColor.LIGHT_PURPLE + formatBoardSec);
//						SidebarString line6 = new SidebarString(ChatColor.YELLOW + "Players: " + ChatColor.LIGHT_PURPLE + "" + getPlayer);
//						SidebarString line7 = new SidebarString(ChatColor.YELLOW + "Dead: " + ChatColor.LIGHT_PURPLE + "" + getDead);
//						SidebarString line8 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//						Sidebar event = new Sidebar(ChatColor.RED + "" + ChatColor.BOLD + arena.boardname, Main.get(), 60, line1, line2, line3, line4, line5, line6, line7, line8);
//						for (int i = 0; i < players.size(); i++) {
//							event.showTo(players.get(i));
//						}
						seconds--;
					}
					if (round == 2) {
						if (seconds == 0) {
							for (int i = 0; i < alivePlayers.size(); i++) {
								Player p = alivePlayers.get(i);
								if (!passedPlayers.contains(p)) {
									die(p);	
								}
							}
							for (int i = 0; i < passedPlayers.size(); i++) {
								Player p = passedPlayers.get(i);
								p.teleport(arena.p1, TeleportCause.PLUGIN);
							}
							passedPlayers.clear();
							round++;
							broadcast("&eStarting round &d" + round + "&e.");
							this.cancel();
							round1to2 = false;
							round2to10 = true;
							countdownTask.cancel();
							next();
							return;
						}
						eventSB.setLine(1, ChatColor.YELLOW + "Round: " + ChatColor.LIGHT_PURPLE + "" + round);
						eventSB.setLine(2, ChatColor.YELLOW + "Time Remaining: " + ChatColor.LIGHT_PURPLE + formatBoardSec);
//						SidebarString line1 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//						SidebarString line2 = new SidebarString(ChatColor.YELLOW + "" + name.toString());
//						SidebarString line3 = new SidebarString("       ");
//						SidebarString line4 = new SidebarString(ChatColor.YELLOW + "Round: " + ChatColor.LIGHT_PURPLE + "" + round);
//						int getPlayer = alivePlayers.size();
//						int getDead = deadPlayers.size();
//						SidebarString line5 = new SidebarString(ChatColor.YELLOW + "Time: " + ChatColor.LIGHT_PURPLE + formatBoardSec);
//						SidebarString line6 = new SidebarString(ChatColor.YELLOW + "Players: " + ChatColor.LIGHT_PURPLE + "" + getPlayer);
//						SidebarString line7 = new SidebarString(ChatColor.YELLOW + "Dead: " + ChatColor.LIGHT_PURPLE + "" + getDead);
//						SidebarString line8 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//						Sidebar event = new Sidebar(ChatColor.RED + "" + ChatColor.BOLD + arena.boardname, Main.get(), 60, line1, line2, line3, line4, line5, line6, line7, line8);
//						for (int i = 0; i < players.size(); i++) {
//							event.showTo(players.get(i));
//						}
						seconds--;
					}
				}
				if (round2to10) {
					if (round == 3) {
						if (seconds == 0) {
							for (int i = 0; i < alivePlayers.size(); i++) {
								Player p = alivePlayers.get(i);
								if (!passedPlayers.contains(p)) {
									die(p);
								}
							}
							for (int i = 0; i < passedPlayers.size(); i++) {
								Player p = passedPlayers.get(i);
								p.teleport(arena.p1, TeleportCause.PLUGIN);
							}
							passedPlayers.clear();
							round++;
							broadcast("&eStarting round &d" + round + "&e.");
							this.cancel();
							countdownTask.cancel();
							next();
							return;
						}
						eventSB.setLine(1, ChatColor.YELLOW + "Round: " + ChatColor.LIGHT_PURPLE + "" + round);
						eventSB.setLine(2, ChatColor.YELLOW + "Time Remaining: " + ChatColor.LIGHT_PURPLE + formatBoardSec);
//						SidebarString line1 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//						SidebarString line2 = new SidebarString(ChatColor.YELLOW + "" + name.toString());
//						SidebarString line3 = new SidebarString("       ");
//						SidebarString line4 = new SidebarString(ChatColor.YELLOW + "Round: " + ChatColor.LIGHT_PURPLE + "" + round);
//						int getPlayer = alivePlayers.size();
//						int getDead = deadPlayers.size();
//						SidebarString line5 = new SidebarString(ChatColor.YELLOW + "Time: " + ChatColor.LIGHT_PURPLE + formatBoardSec);
//						SidebarString line6 = new SidebarString(ChatColor.YELLOW + "Players: " + ChatColor.LIGHT_PURPLE + "" + getPlayer);
//						SidebarString line7 = new SidebarString(ChatColor.YELLOW + "Dead: " + ChatColor.LIGHT_PURPLE + "" + getDead);
//						SidebarString line8 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//						Sidebar event = new Sidebar(ChatColor.RED + "" + ChatColor.BOLD + arena.boardname, Main.get(), 60, line1, line2, line3, line4, line5, line6, line7, line8);
//						for (int i = 0; i < players.size(); i++) {
//							event.showTo(players.get(i));
//						}
						seconds--;
						if (!spawnBlockStop) {
							spawnBlock();
							spawnBlockStop = true;
						}
					}
					if (round == 4) {
						if (seconds == 0) {
							for (int i = 0; i < alivePlayers.size(); i++) {
								Player p = alivePlayers.get(i);
								if (!passedPlayers.contains(p)) {
									die(p);
								}
							}
							for (int i = 0; i < passedPlayers.size(); i++) {
								Player p = passedPlayers.get(i);
								p.teleport(arena.p1, TeleportCause.PLUGIN);
							}
							passedPlayers.clear();
							round++;
							broadcast("&eStarting round &d" + round + "&e.");
							this.cancel();
							countdownTask.cancel();
							next();
							return;
						}
						eventSB.setLine(1, ChatColor.YELLOW + "Round: " + ChatColor.LIGHT_PURPLE + "" + round);
						eventSB.setLine(2, ChatColor.YELLOW + "Time Remaining: " + ChatColor.LIGHT_PURPLE + formatBoardSec);
//						SidebarString line1 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//						SidebarString line2 = new SidebarString(ChatColor.YELLOW + "" + name.toString());
//						SidebarString line3 = new SidebarString("       ");
//						SidebarString line4 = new SidebarString(ChatColor.YELLOW + "Round: " + ChatColor.LIGHT_PURPLE + "" + round);
//						int getPlayer = alivePlayers.size();
//						int getDead = deadPlayers.size();
//						SidebarString line5 = new SidebarString(ChatColor.YELLOW + "Time: " + ChatColor.LIGHT_PURPLE + formatBoardSec);
//						SidebarString line6 = new SidebarString(ChatColor.YELLOW + "Players: " + ChatColor.LIGHT_PURPLE + "" + getPlayer);
//						SidebarString line7 = new SidebarString(ChatColor.YELLOW + "Dead: " + ChatColor.LIGHT_PURPLE + "" + getDead);
//						SidebarString line8 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//						Sidebar event = new Sidebar(ChatColor.RED + "" + ChatColor.BOLD + arena.boardname, Main.get(), 60, line1, line2, line3, line4, line5, line6, line7, line8);
//						for (int i = 0; i < players.size(); i++) {
//							event.showTo(players.get(i));
//						}
						seconds--;
						if (!spawnBlockStop) {
							spawnBlock();
							spawnBlockStop = true;
						}
					}
					if (round == 5) {
						if (seconds == 0) {
							for (int i = 0; i < alivePlayers.size(); i++) {
								Player p = alivePlayers.get(i);
								if (!passedPlayers.contains(p)) {
									die(p);
								}
							}
							for (int i = 0; i < passedPlayers.size(); i++) {
								Player p = passedPlayers.get(i);
								p.teleport(arena.p1, TeleportCause.PLUGIN);
							}
							passedPlayers.clear();
							round++;
							broadcast("&eStarting round &d" + round + "&e.");
							this.cancel();
							countdownTask.cancel();
							next();
							return;
						}
						eventSB.setLine(1, ChatColor.YELLOW + "Round: " + ChatColor.LIGHT_PURPLE + "" + round);
						eventSB.setLine(2, ChatColor.YELLOW + "Time Remaining: " + ChatColor.LIGHT_PURPLE + formatBoardSec);
//						SidebarString line1 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//						SidebarString line2 = new SidebarString(ChatColor.YELLOW + "" + name.toString());
//						SidebarString line3 = new SidebarString("       ");
//						SidebarString line4 = new SidebarString(ChatColor.YELLOW + "Round: " + ChatColor.LIGHT_PURPLE + "" + round);
//						int getPlayer = alivePlayers.size();
//						int getDead = deadPlayers.size();
//						SidebarString line5 = new SidebarString(ChatColor.YELLOW + "Time: " + ChatColor.LIGHT_PURPLE + formatBoardSec);
//						SidebarString line6 = new SidebarString(ChatColor.YELLOW + "Players: " + ChatColor.LIGHT_PURPLE + "" + getPlayer);
//						SidebarString line7 = new SidebarString(ChatColor.YELLOW + "Dead: " + ChatColor.LIGHT_PURPLE + "" + getDead);
//						SidebarString line8 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//						Sidebar event = new Sidebar(ChatColor.RED + "" + ChatColor.BOLD + arena.boardname, Main.get(), 60, line1, line2, line3, line4, line5, line6, line7, line8);
//						for (int i = 0; i < players.size(); i++) {
//							event.showTo(players.get(i));
//						}
						seconds--;
						if (!spawnBlockStop) {
							spawnBlock();
							spawnBlockStop = true;
						}
					}
					if (round == 6) {
						if (seconds == 0) {
							for (int i = 0; i < alivePlayers.size(); i++) {
								Player p = alivePlayers.get(i);
								if (!passedPlayers.contains(p)) {
									die(p);
								}
							}
							for (int i = 0; i < passedPlayers.size(); i++) {
								Player p = passedPlayers.get(i);
								p.teleport(arena.p1, TeleportCause.PLUGIN);
							}
							passedPlayers.clear();
							round++;
							broadcast("&eStarting round &d" + round + "&e.");
							this.cancel();
							countdownTask.cancel();
							next();
							return;
						}
						eventSB.setLine(1, ChatColor.YELLOW + "Round: " + ChatColor.LIGHT_PURPLE + "" + round);
						eventSB.setLine(2, ChatColor.YELLOW + "Time Remaining: " + ChatColor.LIGHT_PURPLE + formatBoardSec);
//						SidebarString line1 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//						SidebarString line2 = new SidebarString(ChatColor.YELLOW + "" + name.toString());
//						SidebarString line3 = new SidebarString("       ");
//						SidebarString line4 = new SidebarString(ChatColor.YELLOW + "Round: " + ChatColor.LIGHT_PURPLE + "" + round);
//						int getPlayer = alivePlayers.size();
//						int getDead = deadPlayers.size();
//						SidebarString line5 = new SidebarString(ChatColor.YELLOW + "Time: " + ChatColor.LIGHT_PURPLE + formatBoardSec);
//						SidebarString line6 = new SidebarString(ChatColor.YELLOW + "Players: " + ChatColor.LIGHT_PURPLE + "" + getPlayer);
//						SidebarString line7 = new SidebarString(ChatColor.YELLOW + "Dead: " + ChatColor.LIGHT_PURPLE + "" + getDead);
//						SidebarString line8 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//						Sidebar event = new Sidebar(ChatColor.RED + "" + ChatColor.BOLD + arena.boardname, Main.get(), 60, line1, line2, line3, line4, line5, line6, line7, line8);
//						for (int i = 0; i < players.size(); i++) {
//							event.showTo(players.get(i));
//						}
						seconds--;
						if (!spawnBlockStop) {
							spawnBlock();
							spawnBlockStop = true;
						}
					}
					if (round == 7) {
						if (seconds == 0) {
							for (int i = 0; i < alivePlayers.size(); i++) {
								Player p = alivePlayers.get(i);
								if (!passedPlayers.contains(p)) {
									die(p);
								}
							}
							for (int i = 0; i < passedPlayers.size(); i++) {
								Player p = passedPlayers.get(i);
								p.teleport(arena.p1, TeleportCause.PLUGIN);
							}
							passedPlayers.clear();
							round++;
							broadcast("&eStarting round &d" + round + "&e.");
							this.cancel();
							countdownTask.cancel();
							next();
							return;
						}
						eventSB.setLine(1, ChatColor.YELLOW + "Round: " + ChatColor.LIGHT_PURPLE + "" + round);
						eventSB.setLine(2, ChatColor.YELLOW + "Time Remaining: " + ChatColor.LIGHT_PURPLE + formatBoardSec);
//						SidebarString line1 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//						SidebarString line2 = new SidebarString(ChatColor.YELLOW + "" + name.toString());
//						SidebarString line3 = new SidebarString("       ");
//						SidebarString line4 = new SidebarString(ChatColor.YELLOW + "Round: " + ChatColor.LIGHT_PURPLE + "" + round);
//						int getPlayer = alivePlayers.size();
//						int getDead = deadPlayers.size();
//						SidebarString line5 = new SidebarString(ChatColor.YELLOW + "Time: " + ChatColor.LIGHT_PURPLE + formatBoardSec);
//						SidebarString line6 = new SidebarString(ChatColor.YELLOW + "Players: " + ChatColor.LIGHT_PURPLE + "" + getPlayer);
//						SidebarString line7 = new SidebarString(ChatColor.YELLOW + "Dead: " + ChatColor.LIGHT_PURPLE + "" + getDead);
//						SidebarString line8 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//						Sidebar event = new Sidebar(ChatColor.RED + "" + ChatColor.BOLD + arena.boardname, Main.get(), 60, line1, line2, line3, line4, line5, line6, line7, line8);
//						for (int i = 0; i < players.size(); i++) {
//							event.showTo(players.get(i));
//						}
						seconds--;
						if (!spawnBlockStop) {
							spawnBlock();
							spawnBlockStop = true;
						}
					}
					if (round == 8) {
						if (seconds == 0) {
							for (int i = 0; i < alivePlayers.size(); i++) {
								Player p = alivePlayers.get(i);
								if (!passedPlayers.contains(p)) {
									die(p);
								}
							}
							for (int i = 0; i < passedPlayers.size(); i++) {
								Player p = passedPlayers.get(i);
								p.teleport(arena.p1, TeleportCause.PLUGIN);
							}
							passedPlayers.clear();
							round++;
							broadcast("&eStarting round &d" + round + "&e.");
							this.cancel();
							countdownTask.cancel();
							next();
							return;
						}
						eventSB.setLine(1, ChatColor.YELLOW + "Round: " + ChatColor.LIGHT_PURPLE + "" + round);
						eventSB.setLine(2, ChatColor.YELLOW + "Time Remaining: " + ChatColor.LIGHT_PURPLE + formatBoardSec);
//						SidebarString line1 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//						SidebarString line2 = new SidebarString(ChatColor.YELLOW + "" + name.toString());
//						SidebarString line3 = new SidebarString("       ");
//						SidebarString line4 = new SidebarString(ChatColor.YELLOW + "Round: " + ChatColor.LIGHT_PURPLE + "" + round);
//						int getPlayer = alivePlayers.size();
//						int getDead = deadPlayers.size();
//						SidebarString line5 = new SidebarString(ChatColor.YELLOW + "Time: " + ChatColor.LIGHT_PURPLE + formatBoardSec);
//						SidebarString line6 = new SidebarString(ChatColor.YELLOW + "Players: " + ChatColor.LIGHT_PURPLE + "" + getPlayer);
//						SidebarString line7 = new SidebarString(ChatColor.YELLOW + "Dead: " + ChatColor.LIGHT_PURPLE + "" + getDead);
//						SidebarString line8 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//						Sidebar event = new Sidebar(ChatColor.RED + "" + ChatColor.BOLD + arena.boardname, Main.get(), 60, line1, line2, line3, line4, line5, line6, line7, line8);
//						for (int i = 0; i < players.size(); i++) {
//							event.showTo(players.get(i));
//						}
						seconds--;
						if (!spawnBlockStop) {
							spawnBlock();
							spawnBlockStop = true;
						}
					}
					if (round == 9) {
						if (seconds == 0) {
							for (int i = 0; i < alivePlayers.size(); i++) {
								Player p = alivePlayers.get(i);
								if (!passedPlayers.contains(p)) {
									die(p);
								}
							}
							for (int i = 0; i < passedPlayers.size(); i++) {
								Player p = passedPlayers.get(i);
								p.teleport(arena.p1, TeleportCause.PLUGIN);
							}
							passedPlayers.clear();
							round++;
							broadcast("&eStarting round &d" + round + "&e.");
							this.cancel();
							countdownTask.cancel();
							next();
							return;
						}
						eventSB.setLine(1, ChatColor.YELLOW + "Round: " + ChatColor.LIGHT_PURPLE + "" + round);
						eventSB.setLine(2, ChatColor.YELLOW + "Time Remaining: " + ChatColor.LIGHT_PURPLE + formatBoardSec);
//						SidebarString line1 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//						SidebarString line2 = new SidebarString(ChatColor.YELLOW + "" + name.toString());
//						SidebarString line3 = new SidebarString("       ");
//						SidebarString line4 = new SidebarString(ChatColor.YELLOW + "Round: " + ChatColor.LIGHT_PURPLE + "" + round);
//						int getPlayer = alivePlayers.size();
//						int getDead = deadPlayers.size();
//						SidebarString line5 = new SidebarString(ChatColor.YELLOW + "Time: " + ChatColor.LIGHT_PURPLE + formatBoardSec);
//						SidebarString line6 = new SidebarString(ChatColor.YELLOW + "Players: " + ChatColor.LIGHT_PURPLE + "" + getPlayer);
//						SidebarString line7 = new SidebarString(ChatColor.YELLOW + "Dead: " + ChatColor.LIGHT_PURPLE + "" + getDead);
//						SidebarString line8 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//						Sidebar event = new Sidebar(ChatColor.RED + "" + ChatColor.BOLD + arena.boardname, Main.get(), 60, line1, line2, line3, line4, line5, line6, line7, line8);
//						for (int i = 0; i < players.size(); i++) {
//							event.showTo(players.get(i));
//						}
						seconds--;
						if (!spawnBlockStop) {
							spawnBlock();
							spawnBlockStop = true;
						}
					}
					if (round == 10) {
						if (seconds == 0) {
							for (int i = 0; i < alivePlayers.size(); i++) {
								Player p = alivePlayers.get(i);
								if (!passedPlayers.contains(p)) {
									die(p);
								}
							}
							for (int i = 0; i < passedPlayers.size(); i++) {
								Player p = passedPlayers.get(i);
								p.teleport(arena.p1, TeleportCause.PLUGIN);
							}
							passedPlayers.clear();
							round++;
							broadcast("&eStarting round &d" + round + "&e.");
							this.cancel();
							round2to10 = false;
							round10up = true;
							countdownTask.cancel();
							next();
							return;
						}
						eventSB.setLine(1, ChatColor.YELLOW + "Round: " + ChatColor.LIGHT_PURPLE + "" + round);
						eventSB.setLine(2, ChatColor.YELLOW + "Time Remaining: " + ChatColor.LIGHT_PURPLE + formatBoardSec);
//						SidebarString line1 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//						SidebarString line2 = new SidebarString(ChatColor.YELLOW + "" + name.toString());
//						SidebarString line3 = new SidebarString("       ");
//						SidebarString line4 = new SidebarString(ChatColor.YELLOW + "Round: " + ChatColor.LIGHT_PURPLE + "" + round);
//						int getPlayer = alivePlayers.size();
//						int getDead = deadPlayers.size();
//						SidebarString line5 = new SidebarString(ChatColor.YELLOW + "Time: " + ChatColor.LIGHT_PURPLE + formatBoardSec);
//						SidebarString line6 = new SidebarString(ChatColor.YELLOW + "Players: " + ChatColor.LIGHT_PURPLE + "" + getPlayer);
//						SidebarString line7 = new SidebarString(ChatColor.YELLOW + "Dead: " + ChatColor.LIGHT_PURPLE + "" + getDead);
//						SidebarString line8 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//						Sidebar event = new Sidebar(ChatColor.RED + "" + ChatColor.BOLD + arena.boardname, Main.get(), 60, line1, line2, line3, line4, line5, line6, line7, line8);
//						for (int i = 0; i < players.size(); i++) {
//							event.showTo(players.get(i));
//						}
						seconds--;
						if (!spawnBlockStop) {
							spawnBlock();
							spawnBlockStop = true;
						}
					}
				}
				if (round10up) {
					if (round > 10) {
						if (seconds2 == 0) {
							for (int i = 0; i < alivePlayers.size(); i++) {
								Player p = alivePlayers.get(i);
								if (!passedPlayers.contains(p)) {
									die(p);
								}
							}
							for (int i = 0; i < passedPlayers.size(); i++) {
								Player p = passedPlayers.get(i);
								p.teleport(arena.p1, TeleportCause.PLUGIN);
							}
							passedPlayers.clear();
							round++;
							broadcast("&eStarting round &d" + round + "&e.");
							this.cancel();
							countdownTask.cancel();
							next();
							return;
						}
						String formatBoardSecc = T.formatSeconds(seconds2);
						eventSB.setLine(1, ChatColor.YELLOW + "Round: " + ChatColor.LIGHT_PURPLE + "" + round);
						eventSB.setLine(2, ChatColor.YELLOW + "Time Remaining: " + ChatColor.LIGHT_PURPLE + formatBoardSecc);
//						SidebarString line1 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//						SidebarString line2 = new SidebarString(ChatColor.YELLOW + "" + name.toString());
//						SidebarString line3 = new SidebarString("       ");
//						SidebarString line4 = new SidebarString(ChatColor.YELLOW + "Round: " + ChatColor.LIGHT_PURPLE + "" + round);
//						int getPlayer = alivePlayers.size();
//						int getDead = deadPlayers.size();
//						SidebarString line5 = new SidebarString(ChatColor.YELLOW + "Time: " + ChatColor.LIGHT_PURPLE + formatBoardSecc);
//						SidebarString line6 = new SidebarString(ChatColor.YELLOW + "Players: " + ChatColor.LIGHT_PURPLE + "" + getPlayer);
//						SidebarString line7 = new SidebarString(ChatColor.YELLOW + "Dead: " + ChatColor.LIGHT_PURPLE + "" + getDead);
//						SidebarString line8 = new SidebarString(ChatColor.translateAlternateColorCodes('&', "&7&m-------------------"));
//						Sidebar event = new Sidebar(ChatColor.RED + "" + ChatColor.BOLD + arena.boardname, Main.get(), 60, line1, line2, line3, line4, line5, line6, line7, line8);
//						for (int i = 0; i < players.size(); i++) {
//							event.showTo(players.get(i));
//						}
						seconds2--;
						if (!spawnBlockStop) {
							spawnBlock();
							spawnBlockStop = true;
						}
					}
				}
			}
		}.runTaskTimer(Main.get(), 0, 20);
	}
	
	public void showEventBoard(Player player) {
		player.setScoreboard(this.eventSB.getScoreboard());
	}
	
	public void hideEventBoard(Player player) {
		player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
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
			T.sendMessage(player, "&e&oThe &d&oWaterDrop &e&oEvent.");
			T.sendMessage(player, "&e&oJump from the top and land in the &d&oWater &e&oto survive, landing anywhere but the &d&oWater &e&owill cause failure.");
			T.sendMessage(player, "&e&oThe &d&oPlayer &e&othat doesn't land on anything but water and is the last to stand is the &3&oWinner&e&o.");
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
				showEventBoard(player);
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean removeSpec(Player player)
	{
		hideEventBoard(player);
		player.getInventory().remove(leaveItem);
		player.getOpenInventory().getTopInventory().clear();
		if (player.getOpenInventory() != null) {
			player.closeInventory();
		}
		specPlayers.remove(player);
		player.getOpenInventory().getTopInventory().clear();
		player.getInventory().clear();
		player.teleport(Main.get().getEventManager().getMainSpawn());
		return super.removeSpec(player);
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
		die(player);
		player.getInventory().remove(leaveItem);
		alivePlayers.remove(player);
		passedPlayers.remove(player);
		deadPlayers.remove(player);
		specPlayers.remove(player);
		hideEventBoard(player);
		updateEventStatsP();
		player.teleport(Main.get().getEventManager().getMainSpawn(), TeleportCause.PLUGIN);
		player.getOpenInventory().getTopInventory().clear();
		player.getInventory().clear();
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
			event.setKeepInventory(true);
			event.getEntity().getInventory().remove(leaveItem);
			event.getEntity().spigot().respawn();
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
				event.getPlayer().setScoreboard(eventSB.getScoreboard());
				event.getPlayer().getInventory().setItem(8, leaveItem);
			}
		}
	}

	@EventHandler
	public void onAlivePlayerDamage(EntityDamageEvent event)
	{
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (specPlayers.contains(p))
			{
				if (event.getCause() == DamageCause.FALL) {
					event.setCancelled(true);
					return;
				}
			}
			if (isPlaying(p))
			{
				if (deadPlayers.contains(p)) {
					if (event.getCause() == DamageCause.FALL) {
						event.setCancelled(true);
						return;
					}
				}
				if (passedPlayers.contains(p)) {
					if (event.getCause() == DamageCause.FALL) {
						event.setCancelled(true);
						return;
					}
				}
				if (alivePlayers.contains(p) && !passedPlayers.contains(p))
				{
					if (event.getCause() == DamageCause.FALL) {
						die(p);
						broadcast("&f" + p.getName() + " &chas failed!");
						event.setCancelled(true);
						return;
					}
				}
				if (event.getCause() == DamageCause.FALL) {
					event.setCancelled(true);
					return;
				}
			}
		}
	}

	@Override
	public void finish()
	{
		if (!alivePlayers.isEmpty() && alivePlayers.size() == 1)
		{
			for (int i = 0; i < alivePlayers.size(); i++) {
				Player p = alivePlayers.get(i);
				Player winner = p;
				int joins = Main.get().getFilestats().getInt("Event-Stats." + winner.getUniqueId().toString() + "." + name) + 1;
				Main.get().getFilestats().set("Event-Stats." + winner.getUniqueId().toString() + "." + name, joins);
				Main.get().saveFileStats();
				Main.get().reloadFileStats();
				//ANNOUNCE
				for (Player bg : Bukkit.getOnlinePlayers()) {
					T.sendMessage(bg, "&d" + winner.getName() + " &ewon the &d" + name + " &eevent!");
					T.sendMessage(bg, "&d" + winner.getName() + " &ewon the &d" + name + " &eevent!");
					T.sendMessage(bg, "&d" + winner.getName() + " &ewon the &d" + name + " &eevent!");
					T.sendMessage(bg, "&d" + winner.getName() + " &ewon the &d" + name + " &eevent!");
				}
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
			}
		}
		for (Player p : players)
		{
			hideEventBoard(p);
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
		for (int i = 0; i < specPlayers.size(); i++) {
			Player p = specPlayers.get(i);
			hideEventBoard(p);
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
			p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		}
//		stopCheckingPos();
		int x1 = arena.b1.getBlockX();
		int y1 = arena.b1.getBlockY();
		int z1 = arena.b1.getBlockZ();
		
		if (Bukkit.getWorld("waterdrop") != null) {
			Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 - 1).setType(Material.STATIONARY_WATER);
			Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1).setType(Material.STATIONARY_WATER);
			Bukkit.getWorld("waterdrop").getBlockAt(x1 + 1, y1, z1 + 1).setType(Material.STATIONARY_WATER);
			Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 + 1).setType(Material.STATIONARY_WATER);
			Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 + 1).setType(Material.STATIONARY_WATER);
			Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1).setType(Material.STATIONARY_WATER);
			Bukkit.getWorld("waterdrop").getBlockAt(x1 - 1, y1, z1 - 1).setType(Material.STATIONARY_WATER);
			Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1 - 1).setType(Material.STATIONARY_WATER);
			Bukkit.getWorld("waterdrop").getBlockAt(x1, y1, z1).setType(Material.STATIONARY_WATER);
		}
		else {
			broadcast(ChatColor.BOLD + T.gold + "The world waterdrop does not exist. Create the world waterdrop all in lowercase!");
		}
		round = 1;
		round1to2 = false;
		round2to10 = false;
		round10up = false;
		startgame = false;
		alivePlayers.clear();
		deadPlayers.clear();
		passedPlayers.clear();
		specPlayers.clear();
		players.clear();
		if (countdownTask != null)
		{
			countdownTask.cancel();
		}
		countingDown = false;
	}

}