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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class RedRover extends Event implements Listener
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
				return "Runners location";
			}
			if (p2 == null)
			{
				return "Killers location";
			}
			if (b2 == null)
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
	private List<Player> killer = new ArrayList<>();
	private List<Player> deadPlayers = new ArrayList<>();
	private Arena arena;
	private List<Arena> arenas;
	private BukkitTask countdownTask;
	private boolean countingDown;
	private String winnerCommand;
	private boolean red = false;
	private boolean blue = false;
	private boolean round1;
	private boolean block1;
	private boolean block2;
	private boolean block3;
	private boolean block4;
	private boolean block5;
	private boolean block6;
	private boolean block7;
	private boolean block8;
	private boolean block9;
	private Random Rand = new Random(3);
	private int round;
	private ScoreboardWrapper eventSB = new ScoreboardWrapper(ChatColor.RED + "" + ChatColor.BOLD + "Events");
	{
		eventSB.addLine(T.replace("&7&m-------------------")); // 0
		eventSB.addBlankSpace(); // 1
		eventSB.addBlankSpace(); // 2
		eventSB.addLine(ChatColor.YELLOW + "Run to: " + ChatColor.RED + "RED"); // 3
		eventSB.addBlankSpace(); // 4
		eventSB.addLine(T.replace("&ePlayers: &d" + (players.size() - deadPlayers.size()))); // 5
		eventSB.addLine(T.replace("&eSpectators: &d" + (specPlayers.size() + deadPlayers.size()))); // 6
		eventSB.addLine(T.replace("&7&m-------------------")); // 7
	}
	
	private ScoreboardWrapper eventSB1 = new ScoreboardWrapper(ChatColor.RED + "" + ChatColor.BOLD + "Events");
	{
		eventSB1.addLine(T.replace("&7&m-------------------")); // 0
		eventSB1.addBlankSpace(); // 1
		eventSB1.addBlankSpace(); // 2
		eventSB1.addLine(ChatColor.YELLOW + "Run to: " + ChatColor.BLUE + "BLUE"); // 3
		eventSB1.addBlankSpace(); // 4
		eventSB1.addLine(T.replace("&ePlayers: &d" + (players.size() - deadPlayers.size()))); // 5
		eventSB1.addLine(T.replace("&eSpectators: &d" + (specPlayers.size() + deadPlayers.size()))); // 6
		eventSB1.addLine(T.replace("&7&m-------------------")); // 7
	}

	private ItemStack leaveItem;
	private ItemStack sword;

	public RedRover(List<Arena> arenas, String winnerCommand)
	{
		super("RedRover");
		this.arenas = arenas;
		this.winnerCommand = winnerCommand;
		if (winnerCommand == null)
		{
			Main.get().getLogger().warning("No winner command set for redrover.");
		}

		sword = new ItemStack(Material.DIAMOND_SWORD);
		ItemMeta im2 = sword.getItemMeta();
		im2.spigot().setUnbreakable(true);
		sword.setItemMeta(im2);
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
		if (players.size() <= 2)
		{
			broadcast("&eNot enough players joined the event.");
			initFinish();
			return false;
		}
		Collections.shuffle(players);
		alivePlayers.addAll(players);
		Player p = alivePlayers.remove(0);
		killer.add(p);
		for (int i = 0; i < killer.size(); i++) {
			Player p1 = killer.get(i);
			p1.getOpenInventory().getTopInventory().clear();
			if (p1.getOpenInventory() != null) {
				p1.closeInventory();
			}
			killer.get(0).getInventory().removeItem(leaveItem);
			killer.get(0).getInventory().setItem(0, sword);
			killer.get(0).getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
			killer.get(0).getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
			killer.get(0).getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
			killer.get(0).getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
			showEventBoard1(p1);
		}
		for (Player p1 : alivePlayers) {
			p1.getOpenInventory().getTopInventory().clear();
			if (p1.getOpenInventory() != null) {
				p1.closeInventory();
			}
			showEventBoard1(p1);
		}
		for (Player p1 : specPlayers) {
			showEventBoard1(p1);
		}
		round1 = false;
		block1 = true;
		block2 = false;
		block3 = false;
		block4 = false;
		block5 = false;
		block6 = false;
		block7 = false;
		block8 = false;
		block9 = false;
		round = 0;
		updateEventStatsP1();
		updateEventStatsP2();
		next();

		return true;
	}

	public boolean isCountingDown()
	{
		return countingDown;
	}
	
	public boolean nextRound()
	{
		return countingDown;
	}
	
	public void updateEventStatsP1() {
		eventSB.setLine(5, T.replace("&ePlayers: &d" + (players.size() - deadPlayers.size())));
		eventSB.setLine(6, T.replace("&eSpectators: &d" + (specPlayers.size() + deadPlayers.size())));
	}
	
	public void updateEventStatsP2() {
		eventSB1.setLine(5, T.replace("&ePlayers: &d" + (players.size() - deadPlayers.size())));
		eventSB1.setLine(6, T.replace("&eSpectators: &d" + (specPlayers.size() + deadPlayers.size())));
	}

	public void next()
	{
		if (alivePlayers.size() == 1 && !deadPlayers.isEmpty())
		{
			broadcast("&d" + killer.get(0).getName() + " &ehas been eliminated. (&d" + (players.size() - deadPlayers.size()) + "&e)");
			killer.get(0).getInventory().clear();
			killer.get(0).getInventory().setBoots(new ItemStack(Material.AIR));
			killer.get(0).getInventory().setLeggings(new ItemStack(Material.AIR));
			killer.get(0).getInventory().setChestplate(new ItemStack(Material.AIR));
			killer.get(0).getInventory().setHelmet(new ItemStack(Material.AIR));
			initFinish();
			countdownTask.cancel();
			int x1 = arena.b1.getBlockX();
			int y1 = arena.b1.getBlockY();
			int z1 = arena.b1.getBlockZ();
			if (Bukkit.getWorld("redrover") != null) {
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 2).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 3).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 4).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 5).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 6).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 7).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 8).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 9).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 10).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 11).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 2).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 3).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 4).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 5).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 6).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 7).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 8).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 9).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 10).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 11).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 2).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 3).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 4).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 5).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 6).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 7).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 8).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 9).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 10).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 11).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 2).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 3).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 4).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 5).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 6).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 7).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 8).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 9).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 10).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 11).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 2).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 3).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 4).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 5).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 6).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 7).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 8).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 9).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 10).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 11).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 2).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 3).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 4).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 5).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 6).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 7).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 8).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 9).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 10).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 11).setType(Material.AIR); // End of middle slice
			}
			return;
		}
		if (alivePlayers.isEmpty())
		{
			if (killer instanceof Player)
			{
				Player p = (Player) killer;
				p.getOpenInventory().getTopInventory().clear();
				if (p.getOpenInventory() != null) {
					p.closeInventory();
				}
				p.getInventory().clear();
				p.getInventory().setBoots(new ItemStack(Material.AIR));
				p.getInventory().setLeggings(new ItemStack(Material.AIR));
				p.getInventory().setChestplate(new ItemStack(Material.AIR));
				p.getInventory().setHelmet(new ItemStack(Material.AIR));
			}
			initFinish();
			countdownTask.cancel();
			int x1 = arena.b1.getBlockX();
			int y1 = arena.b1.getBlockY();
			int z1 = arena.b1.getBlockZ();
			if (Bukkit.getWorld("redrover") != null) {
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 2).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 3).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 4).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 5).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 6).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 7).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 8).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 9).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 10).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 11).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 2).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 3).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 4).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 5).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 6).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 7).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 8).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 9).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 10).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 11).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 2).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 3).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 4).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 5).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 6).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 7).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 8).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 9).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 10).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 11).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 2).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 3).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 4).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 5).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 6).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 7).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 8).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 9).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 10).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 11).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 2).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 3).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 4).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 5).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 6).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 7).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 8).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 9).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 10).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 11).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 2).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 3).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 4).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 5).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 6).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 7).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 8).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 9).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 10).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 11).setType(Material.AIR); // End of middle slice
			}
			return;
		}
		if (!round1) {
			killer.get(0).teleport(arena.p2);
			killer.get(0).getInventory().removeItem(leaveItem);
			killer.get(0).playSound(killer.get(0).getLocation(), Sound.FIREWORK_BLAST, 1, 1);
			for (Player p : alivePlayers) {
				p.teleport(arena.p1);
				p.getInventory().removeItem(leaveItem);
				p.playSound(p.getLocation(), Sound.NOTE_PLING, 1, 1);
			}
//			for (Player p : deadPlayers) {
//				showEventBoard1(p);
//				eventSB1.setLine(3, ChatColor.YELLOW + "Round: " + ChatColor.DARK_PURPLE + round);
//			}
			broadcast("&eStarting round &d" + round + "&e.");
			broadcast("&eYou must run to the &cRed &eside!");
			red();
			red = true;
			round1 = true;
			return;
		}
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent event)
	{
		if (arena == null)
		{
			return;
		}
		
		for (int i = 0; i < killer.size(); i++) {
			Player p = killer.get(i);
			if (isWithinRegion(p.getLocation(), "red") || isWithinRegion(p.getLocation(), "blue")) {
				p.teleport(arena.p2);
			}
			else {
				if (!isWithinRegion(p.getLocation(), "middle")) {
					p.teleport(arena.p2);
				}
			}
		}

		if (event.getPlayer().isDead())
		{
			die(event.getPlayer());
		}
	}
	
	public void blue()
	{
		round++;
		countingDown = true;
		countdownTask = new BukkitRunnable()
		{
			int seconds = 15;
			boolean spawnBlockStop = false;
				
			@Override
			public void run()
			{
				red = false;
				blue = true;
				if (seconds == 0)
				{
					for (int i = 0; i < alivePlayers.size(); i++) {
						Player p = alivePlayers.get(i);
						if (isWithinRegion(p, "red") || isWithinRegion(p, "middle"))
					    {
					    	die(p);
					    }
						String formatBoardSec = T.formatSeconds(seconds);
						showEventBoard1(p);
					}
					for (int i = 0; i < deadPlayers.size(); i++) {
						Player p = deadPlayers.get(i);
						showEventBoard1(p);
					}
					for (int i = 0; i < specPlayers.size(); i++) {
						Player p = specPlayers.get(i);
						showEventBoard1(p);
					}
					for (int i = 0; i < killer.size(); i++) {
						Player p = killer.get(i);
						showEventBoard1(p);
					}
					broadcast("&eStarting round &d" + round + "&e.");
					broadcast("&eYou must run to the &cRed &eside!");
					this.cancel();
					countdownTask.cancel();
					if (alivePlayers.isEmpty() && killer.size() == 1) {
						next();
					}
					else {
					if (alivePlayers.size() == 1) {
						next();
					}
					else {
						red();
						red = true;
						blue = false;
					}
					if (!spawnBlockStop) {
						spawnBlock();
						spawnBlockStop = true;
					}
					return;
					}
				}
				else if (seconds <= 15) {
					String formatBoardSec = T.formatSeconds(seconds);
					seconds--;
					eventSB1.setLine(1, ChatColor.YELLOW + "Round: " + ChatColor.LIGHT_PURPLE + round);
					eventSB1.setLine(2, ChatColor.YELLOW + "Time remaining: " + ChatColor.LIGHT_PURPLE + formatBoardSec);
					eventSB.setLine(1, ChatColor.YELLOW + "Round: " + ChatColor.LIGHT_PURPLE + round);
					eventSB.setLine(2, ChatColor.YELLOW + "Time remaining: " + ChatColor.LIGHT_PURPLE + formatBoardSec);
				}
			}
		}.runTaskTimer(Main.get(), 0, 20);
	}
	
	public void red()
	{
		round++;
		countingDown = true;
		countdownTask = new BukkitRunnable()
		{
			int seconds = 15;
			boolean spawnBlockStop = false;
				
			@Override
			public void run() {
				red = true;
				blue = false;
				if (seconds == 0) {
					for (int i = 0; i < alivePlayers.size(); i++) {
						Player p = alivePlayers.get(i);
						if (isWithinRegion(p, "blue") || isWithinRegion(p, "middle")) {
					    	die(p);
						}
						showEventBoard2(p);
					}
					for (int i = 0; i < deadPlayers.size(); i++) {
						Player p = deadPlayers.get(i);
						showEventBoard2(p);
					}
					for (int i = 0; i < specPlayers.size(); i++) {
						Player p = specPlayers.get(i);
						showEventBoard2(p);
					}
					for (int i = 0; i < killer.size(); i++) {
						Player p = killer.get(i);
						showEventBoard2(p);
					}
					broadcast("&eStarting round &d" + round + "&e.");
					broadcast("&eYou must run to the &9Blue &eside!");
					this.cancel();
					countdownTask.cancel();
					if (alivePlayers.isEmpty() && killer.size() == 1) {
						next();
					}
					else {
					if (alivePlayers.size() == 1) {
						next();
					}
					else {
						blue();
						blue = true;
						red = false;
					}
					if (!spawnBlockStop) {
						spawnBlock();
						spawnBlockStop = true;
					}
					return;
					}
				}
				else if (seconds <= 15) {
					String formatBoardSec = T.formatSeconds(seconds);
					seconds--;
					eventSB.setLine(1, ChatColor.YELLOW + "Round: " + ChatColor.LIGHT_PURPLE + round);
					eventSB.setLine(2, ChatColor.YELLOW + "Time remaining: " + ChatColor.LIGHT_PURPLE + formatBoardSec);
					eventSB1.setLine(1, ChatColor.YELLOW + "Round: " + ChatColor.LIGHT_PURPLE + round);
					eventSB1.setLine(2, ChatColor.YELLOW + "Time remaining: " + ChatColor.LIGHT_PURPLE + formatBoardSec);
				}
			}
		}.runTaskTimer(Main.get(), 0, 20);
	}
	
	public void spawnBlock() {
		int x1 = arena.b1.getBlockX();
		int y1 = arena.b1.getBlockY();
		int z1 = arena.b1.getBlockZ();
		if (Bukkit.getWorld("redrover") != null) {
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 1).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 2).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 3).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 4).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 5).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 6).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 7).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 8).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 9).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 10).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 11).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 1).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 2).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 3).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 4).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 5).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 6).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 7).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 8).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 9).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 10).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 11).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 1).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 2).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 3).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 4).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 5).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 6).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 7).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 8).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 9).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 10).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 11).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 1).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 2).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 3).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 4).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 5).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 6).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 7).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 8).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 9).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 10).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 11).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 1).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 2).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 3).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 4).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 5).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 6).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 7).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 8).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 9).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 10).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 11).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 1).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 2).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 3).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 4).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 5).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 6).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 7).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 8).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 9).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 10).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 11).setType(Material.AIR); // End of middle slice
			
			
			if (block1) {
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 7).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 11).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 7).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 11).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 7).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 11).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 7).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 11).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 7).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 11).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 7).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 11).setType(Material.SMOOTH_BRICK); // set to 7 blocks out
				Player p = killer.get(0);
				for (PotionEffect effect : p.getActivePotionEffects()) {
					p.removePotionEffect(effect.getType());
				}
				p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000000, 2, false));
				broadcast("&6&lSPEED ROUND!");
				block1 = false;
				block2 = true;
				
				return;
			}
			if (block2) {
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 2).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 3).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 4).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 5).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 6).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 7).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 8).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 9).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 10).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 11).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 2).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 3).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 4).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 5).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 6).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 7).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 8).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 9).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 10).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 11).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 2).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 3).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 4).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 5).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 6).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 7).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 8).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 9).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 10).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 11).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 2).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 3).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 4).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 5).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 6).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 7).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 8).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 9).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 10).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 11).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 2).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 3).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 4).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 5).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 6).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 7).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 8).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 9).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 10).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 11).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 2).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 3).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 4).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 5).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 6).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 7).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 8).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 9).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 10).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 11).setType(Material.AIR); // Set all to air
				Player p = killer.get(0);
				for (PotionEffect effect : p.getActivePotionEffects()) {
					p.removePotionEffect(effect.getType());
				}
				block2 = false;
				block3 = true;
				return;
			}
			if (block3) {
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 2).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 3).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 4).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 5).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 6).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 7).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 11).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 2).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 3).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 4).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 5).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 6).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 7).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 11).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 2).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 3).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 4).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 5).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 6).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 7).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 11).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 2).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 3).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 4).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 5).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 6).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 7).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 11).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 2).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 3).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 4).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 5).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 6).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 7).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 11).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 2).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 3).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 4).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 5).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 6).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 7).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 11).setType(Material.SMOOTH_BRICK); // set to 2 blocks out
				Player p = killer.get(0);
				for (PotionEffect effect : p.getActivePotionEffects()) {
					p.removePotionEffect(effect.getType());
				}
				p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000000, 10, false));
				broadcast("&6&lULTRA SPEED ROUND!");
				block3 = false;
				block4 = true;
				return;
			}
			if (block4) {
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 5).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 6).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 7).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 11).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 5).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 6).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 7).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 11).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 5).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 6).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 7).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 11).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 5).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 6).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 7).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 11).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 5).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 6).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 7).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 11).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 5).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 6).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 7).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 11).setType(Material.SMOOTH_BRICK); // Set to 5 blocks out
				Player p = killer.get(0);
				for (PotionEffect effect : p.getActivePotionEffects()) {
					p.removePotionEffect(effect.getType());
				};
				block4 = false;
				block5 = true;
				return;
			}
			if (block5) {
				block5 = false;
				block6 = true;
				return;
			}
			if (block6) {
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 11).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 11).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 11).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 11).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 11).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 11).setType(Material.SMOOTH_BRICK); // Set to 8 blocks out
				Player p = killer.get(0);
				for (PotionEffect effect : p.getActivePotionEffects()) {
					p.removePotionEffect(effect.getType());
				}
				p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000000, 10, false));
				broadcast("&6&lULTRA SPEED ROUND!");
				block6 = false;
				block7 = true;
				return;
			}
			if (block7) {
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 2).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 3).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 4).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 5).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 6).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 7).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 8).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 9).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 10).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 11).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 2).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 3).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 4).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 5).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 6).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 7).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 8).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 9).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 10).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 11).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 2).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 3).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 4).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 5).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 6).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 7).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 8).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 9).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 10).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 11).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 2).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 3).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 4).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 5).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 6).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 7).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 8).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 9).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 10).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 11).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 2).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 3).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 4).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 5).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 6).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 7).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 8).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 9).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 10).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 11).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 1).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 2).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 3).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 4).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 5).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 6).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 7).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 8).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 9).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 10).setType(Material.AIR);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 11).setType(Material.AIR); // Set all to air
				Player p = killer.get(0);
				for (PotionEffect effect : p.getActivePotionEffects()) {
					p.removePotionEffect(effect.getType());
				}
				p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000000, 2, false));
				block7 = false;
				block8 = true;
				return;
			}
			if (block8) {
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 3).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 4).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 5).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 6).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 7).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 11).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 3).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 4).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 5).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 6).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 7).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 11).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 3).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 4).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 5).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 6).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 7).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 11).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 3).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 4).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 5).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 6).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 7).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 11).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 3).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 4).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 5).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 6).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 7).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 11).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 3).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 4).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 5).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 6).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 7).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 11).setType(Material.SMOOTH_BRICK); // Set all to SMOOTH_BRICK
				Player p = killer.get(0);
				for (PotionEffect effect : p.getActivePotionEffects()) {
					p.removePotionEffect(effect.getType());
				}
				block8 = false;
				block9 = true;
				return;
			}
			if (block9) {
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 7).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 11).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 7).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 11).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 7).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 11).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 7).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 11).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 7).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 11).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 7).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 8).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 9).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 10).setType(Material.SMOOTH_BRICK);
				Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 11).setType(Material.SMOOTH_BRICK); // set to 7 blocks in
				Player p = killer.get(0);
				for (PotionEffect effect : p.getActivePotionEffects()) {
					p.removePotionEffect(effect.getType());
				}
				p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000000, 20, false));
				broadcast("&6&lSUPER SPEED ROUND");
				block9 = false;
				block5 = true;
				return;
			}
		}
		else {
			broadcast("&eThe world redrover does not exist. Create the world redrover all in lowercase!");
		}
	}
	
	public void showEventBoard1(Player player) {
		player.setScoreboard(this.eventSB.getScoreboard());
	}
	
	public void showEventBoard2(Player player) {
		player.setScoreboard(this.eventSB1.getScoreboard());
	}
	
	public void hideEventBoard(Player player) {
		player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
	}

	public void die(Player player)
	{
		if (alivePlayers.remove(player))
		{
			player.teleport(arena.spectate);
			player.getInventory().setItem(8, leaveItem);
			T.sendMessage(player, "&eYou have been eliminated.");
			T.sendMessage(player, "&eType " + T.g("&d/e leave") + " &eto leave.");
			broadcast("&d" + player.getName() + " &ewas eliminated by &d" + killer.get(0).getName() + "&e. (&d" + (players.size() - deadPlayers.size()) + "&e)");
			deadPlayers.add(player);
			if (red && !blue) showEventBoard1(player);
			if (!red && blue) showEventBoard2(player);
			updateEventStatsP1();
			updateEventStatsP2();
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
		int x1 = arena.b1.getBlockX();
		int y1 = arena.b1.getBlockY();
		int z1 = arena.b1.getBlockZ();
		if (Bukkit.getWorld("redrover") != null) {
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 1).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 2).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 3).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 4).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 5).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 6).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 7).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 8).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 9).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 10).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 11).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 1).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 2).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 3).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 4).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 5).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 6).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 7).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 8).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 9).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 10).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 11).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 1).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 2).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 3).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 4).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 5).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 6).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 7).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 8).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 9).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 10).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 11).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 1).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 2).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 3).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 4).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 5).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 6).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 7).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 8).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 9).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 10).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 11).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 1).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 2).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 3).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 4).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 5).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 6).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 7).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 8).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 9).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 10).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 11).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 1).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 2).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 3).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 4).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 5).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 6).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 7).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 8).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 9).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 10).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 11).setType(Material.AIR); // End of middle slice
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
				if (red && !blue) {
					showEventBoard1(player);
				}
				if (blue && !red) {
					showEventBoard2(player);
				}
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean removeSpec(Player player)
	{
		player.getInventory().remove(leaveItem);
		player.getOpenInventory().getTopInventory().clear();
		player.getInventory().clear();
		specPlayers.remove(player);
		hideEventBoard(player);
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
			T.sendMessage(player, "&e&oThe &d&oRedRover &e&oEvent.");
			T.sendMessage(player, "&e&oRun across from each side and make sure to be on the &d&oCorrect &e&oside at all times.");
			T.sendMessage(player, "&e&oWho ever is alive discluding the &d&oKiller &e&ois the &3&oWinner&e&o. Although the &d&oKiller &e&ocan win if they kill ever &d&oPlayer&e&o.");
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
		if (players.contains(player) && killer.contains(player)) {
			killer.get(0).getInventory().clear();
			killer.get(0).getActivePotionEffects().clear();
			killer.get(0).getInventory().setBoots(new ItemStack(Material.AIR));
			killer.get(0).getInventory().setLeggings(new ItemStack(Material.AIR));
			killer.get(0).getInventory().setChestplate(new ItemStack(Material.AIR));
			killer.get(0).getInventory().setHelmet(new ItemStack(Material.AIR));
			killer.clear();
			Collections.shuffle(alivePlayers);
			Player p = alivePlayers.remove(0);
			killer.add(p);
			p.teleport(arena.p2);
			p.getInventory().removeItem(leaveItem);
			p.getInventory().setItem(0, sword);
			p.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
			p.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
			p.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
			p.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
			updateEventStatsP1();
			updateEventStatsP2();
		}
		die(player);
		player.getInventory().remove(leaveItem);
		alivePlayers.remove(player);
		deadPlayers.remove(player);
		specPlayers.remove(player);
		updateEventStatsP1();
		updateEventStatsP2();
		hideEventBoard(player);
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
					if (!alivePlayers.contains(p))
					{
						return;
					}
					else {
						event.setDamage(0);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		if (players.contains(event.getPlayer()) && killer.contains(event.getPlayer())) {
			event.getPlayer().getActivePotionEffects().clear();
			for (PotionEffect effect : event.getPlayer().getActivePotionEffects()) {
				event.getPlayer().removePotionEffect(effect.getType());
			}
			killer.get(0).getInventory().clear();
			killer.get(0).getActivePotionEffects().clear();
			killer.get(0).getInventory().setBoots(new ItemStack(Material.AIR));
			killer.get(0).getInventory().setLeggings(new ItemStack(Material.AIR));
			killer.get(0).getInventory().setChestplate(new ItemStack(Material.AIR));
			killer.get(0).getInventory().setHelmet(new ItemStack(Material.AIR));
			killer.clear();
			players.remove(event.getPlayer());
			Collections.shuffle(alivePlayers);
			Player p = alivePlayers.remove(0);
			killer.add(p);
			p.teleport(arena.p2);
			p.getInventory().removeItem(leaveItem);
			p.getInventory().setItem(0, sword);
			p.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
			p.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
			p.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
			p.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
			updateEventStatsP1();
			updateEventStatsP2();
		}
		if (alivePlayers.size() == 1 && killer.size() == 0) {
			finish();
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
		
		if (alivePlayers.isEmpty() && killer.size() == 1)
		{
			Player winner = killer.get(0);
			int joins = Main.get().getFilestats().getInt("Event-Stats." + winner.getUniqueId().toString() + "." + name) + 1;
			Main.get().getFilestats().set("Event-Stats." + winner.getUniqueId().toString() + "." + name, joins);
			Main.get().saveFileStats();
			Main.get().reloadFileStats();
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
		
		if (alivePlayers.isEmpty() && killer.isEmpty() && deadPlayers.isEmpty())
		{
			for (Player p : players)
			{
				p.getInventory().remove(leaveItem);
				p.getOpenInventory().getTopInventory().clear();
				p.getInventory().clear();
				p.teleport(Main.get().getEventManager().getMainSpawn());
				p.getInventory().setBoots(new ItemStack(Material.AIR));
				p.getInventory().setLeggings(new ItemStack(Material.AIR));
				p.getInventory().setChestplate(new ItemStack(Material.AIR));
				p.getInventory().setHelmet(new ItemStack(Material.AIR));
			}
			alivePlayers.clear();
			if (countdownTask != null) {
	            countdownTask.cancel();
	        }
			deadPlayers.clear();
			killer.clear();
			countingDown = false;
		}
		
		if (alivePlayers.size() == 1 && killer.size() == 1 && deadPlayers.isEmpty())
		{
			for (Player p : players)
			{
				p.getInventory().remove(leaveItem);
				p.getOpenInventory().getTopInventory().clear();
				p.getInventory().clear();
				p.teleport(Main.get().getEventManager().getMainSpawn());
				for (PotionEffect effect : p.getActivePotionEffects()) {
					p.removePotionEffect(effect.getType());
				}
				p.getInventory().setBoots(new ItemStack(Material.AIR));
				p.getInventory().setLeggings(new ItemStack(Material.AIR));
				p.getInventory().setChestplate(new ItemStack(Material.AIR));
				p.getInventory().setHelmet(new ItemStack(Material.AIR));
			}
			alivePlayers.clear();
			if (countdownTask != null) {
	            countdownTask.cancel();
	        }
			deadPlayers.clear();
			killer.clear();
			countingDown = false;
		}
		
		for (Player p : players)
		{
			hideEventBoard(p);
			p.getInventory().remove(leaveItem);
			p.getInventory().setBoots(new ItemStack(Material.AIR));
			p.getInventory().setLeggings(new ItemStack(Material.AIR));
			p.getInventory().setChestplate(new ItemStack(Material.AIR));
			p.getInventory().setHelmet(new ItemStack(Material.AIR));
			p.getOpenInventory().getTopInventory().clear();
			if (p.getOpenInventory() != null) {
				p.closeInventory();
			}
			p.getInventory().clear();
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
			for (PotionEffect effect : p.getActivePotionEffects()) {
				p.removePotionEffect(effect.getType());
			}
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
		int x1 = arena.b1.getBlockX();
		int y1 = arena.b1.getBlockY();
		int z1 = arena.b1.getBlockZ();
		if (Bukkit.getWorld("redrover") != null) {
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 1).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 2).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 3).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 4).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 5).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 6).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 7).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 8).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 9).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 10).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 - 11).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 1).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 2).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 3).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 4).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 5).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 6).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 7).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 8).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 9).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 10).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 + 1, z1 + 11).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 1).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 2).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 3).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 4).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 5).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 6).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 7).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 8).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 9).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 10).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 - 11).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 1).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 2).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 3).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 4).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 5).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 6).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 7).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 8).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 9).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 10).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1, z1 + 11).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 1).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 2).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 3).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 4).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 5).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 6).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 7).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 8).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 9).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 10).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 - 11).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 1).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 2).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 3).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 4).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 5).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 6).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 7).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 8).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 9).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 10).setType(Material.AIR);
			Bukkit.getWorld("redrover").getBlockAt(x1, y1 - 1, z1 + 11).setType(Material.AIR); // End of middle slice
		}
		else {
			broadcast(ChatColor.BOLD + T.gold + "The world redrover does not exist. Create the world redrover all in lowercase!");
		}
		alivePlayers.clear();
		if (countdownTask != null) {
            countdownTask.cancel();
        }
		deadPlayers.clear();
		specPlayers.clear();
		killer.clear();
		players.clear();
		countingDown = false;
	}

}
