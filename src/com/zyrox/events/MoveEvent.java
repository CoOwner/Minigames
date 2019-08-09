package com.zyrox.events;

import org.bukkit.event.Listener;

public class MoveEvent implements Listener {
	
//	public static boolean isWithinRegion(Player player, String region)
//    { 
//		return isWithinRegion(player.getLocation(), region);
//    }
//
//	public static boolean isWithinRegion(Block block, String region)
//    { 
//		return isWithinRegion(block.getLocation(), region); 
//		}
//
//	public static boolean isWithinRegion(Location loc, String region)
//	{
//		WorldGuardPlugin guard = getWorldGuard();
//		com.sk89q.worldedit.Vector v = toVector(loc);
//		RegionManager manager = guard.getRegionManager(loc.getWorld());
//		ApplicableRegionSet set = manager.getApplicableRegions(v);
//		for (ProtectedRegion each : set) {
//			if (each.getId().equalsIgnoreCase(region)) {
//				return true;
//			}
//		}
//		return false;
//	}
//	
//	private static WorldGuardPlugin getWorldGuard() {
//	    Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
//	 
//	    // WorldGuard may not be loaded
//	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
//	        System.out.println("Error: WorldGuard not installed!");
//	        return null; // Maybe you want throw an exception instead
//	    }
//	 
//	    return (WorldGuardPlugin) plugin;
//	}
//
//	@EventHandler
//	public void onMove(PlayerMoveEvent event) {
//		Player p = (Player) event.getPlayer();
//		Location l = p.getLocation();
//        Block standingOn = l.getBlock().getRelative(BlockFace.DOWN);
//        Block b = l.getBlock();
//        double x = l.getX() - (double)l.getBlockX();
//        double z = l.getZ() - (double)l.getBlockZ();
//        Block b1 = standingOn.getLocation().add(1.0, 0.0, 0.0).getBlock();
//        Block b2 = standingOn.getLocation().add(1.0, 0.0, 1.0).getBlock();
//        Block b3 = standingOn.getLocation().add(0.0, 0.0, 1.0).getBlock();
//        Block b4 = standingOn.getLocation().add(-1.0, 0.0, 1.0).getBlock();
//        Block b5 = standingOn.getLocation().add(-1.0, 0.0, 0.0).getBlock();
//        Block b6 = standingOn.getLocation().add(-1.0, 0.0, -1.0).getBlock();
//        Block b7 = standingOn.getLocation().add(0.0, 0.0, -1.0).getBlock();
//        Block b8 = standingOn.getLocation().add(1.0, 0.0, -1.0).getBlock();
//        Bukkit.getScheduler().runTaskLater(Main.get(), new Runnable(){
//
//            @Override
//            public void run() {
//            	if (isWithinRegion(p.getLocation(), "inwater")) {
//	                if (standingOn.getType() == Material.STATIONARY_WATER || standingOn.getType() == Material.WATER && standingOn.getType() != Material.REDSTONE_BLOCK) {
//	                	T.sendMessage(p, p.getName() + " is in water standingOn");
//	                }
//	                else {
//	                	T.sendMessage(p, p.getName() + " is in standingOn " + standingOn.getType() + " might have also stood on the redstone block");
//	                }
//	                if (x > 0.7 && b1.getType() == Material.WATER || x > 0.7 && b1.getType() == Material.STATIONARY_WATER && standingOn.getType() != Material.REDSTONE_BLOCK) {
//	                	T.sendMessage(p, p.getName() + " is in water b1");
//	                }
//	                else {
//	                	T.sendMessage(p, p.getName() + " is in b1 " + b1.getType() + " might have also stood on the redstone block");
//	                }
//	                if (x < 0.3 && b5.getType() == Material.WATER || x < 0.3 && b5.getType() == Material.STATIONARY_WATER && standingOn.getType() != Material.REDSTONE_BLOCK) {
//	                	T.sendMessage(p, p.getName() + " is in water b5");
//	                }
//	                else {
//	                	T.sendMessage(p, p.getName() + " is in b5 " + b5.getType() + " might have also stood on the redstone block");
//	                }
//	                if (z > 0.7 && b3.getType() == Material.WATER || z > 0.7 && b3.getType() == Material.STATIONARY_WATER && standingOn.getType() != Material.REDSTONE_BLOCK) {
//	                	T.sendMessage(p, p.getName() + " is in water b3");
//	                }
//	                else {
//	                	T.sendMessage(p, p.getName() + " is in b3 " + b3.getType() + " might have also stood on the redstone block");
//	                }
//	                if (z < 0.3 && b7.getType() == Material.WATER || z < 0.3 && b7.getType() == Material.STATIONARY_WATER && standingOn.getType() != Material.REDSTONE_BLOCK) {
//	                	T.sendMessage(p, p.getName() + " is in water b7");
//	                }
//	                else {
//	                	T.sendMessage(p, p.getName() + " is in b7 " + b7.getType() + " might have also stood on the redstone block");
//	                }
////	                if (z < 0.3 && b7.getType() != Material.WATER || z < 0.3 && b7.getType() != Material.STATIONARY_WATER) {
////	                	T.sendMessage(p, p.getName() + " is in b7 " + b7.getType());
////	                }
//	                //corners
//	                if (x > 0.7 && z > 0.7 && b2.getType() == Material.WATER || x > 0.7 && z > 0.7 && b2.getType() == Material.STATIONARY_WATER && standingOn.getType() != Material.REDSTONE_BLOCK) {
//	                	T.sendMessage(p, p.getName() + " is in water b2");
//	                }
//	                else {
//	                	T.sendMessage(p, p.getName() + " is in b2 " + b2.getType() + " might have also stood on the redstone block");
//	                }
//	                if (x < 0.3 && z > 0.7 && b4.getType() == Material.WATER || x < 0.3 && z > 0.7 && b4.getType() == Material.STATIONARY_WATER && standingOn.getType() != Material.REDSTONE_BLOCK) {
//	                	T.sendMessage(p, p.getName() + " is in water b4");
//	                }
//	                else {
//	                	T.sendMessage(p, p.getName() + " is in b4 " + b4.getType() + " might have also stood on the redstone block");
//	                }
//	                if (x > 0.7 && z < 0.3 && b8.getType() == Material.WATER || x > 0.7 && z < 0.3 && b8.getType() == Material.STATIONARY_WATER && standingOn.getType() != Material.REDSTONE_BLOCK) {
//	                	T.sendMessage(p, p.getName() + " is in water b8");
//	                }
//	                else {
//	                	T.sendMessage(p, p.getName() + " is in b8 " + b8.getType() + " might have also stood on the redstone block");
//	                }
//	                if (x < 0.3 && z < 0.3 && b6.getType() == Material.WATER || x < 0.3 && z < 0.3 && b6.getType() == Material.STATIONARY_WATER && standingOn.getType() != Material.REDSTONE_BLOCK) {
//	                	T.sendMessage(p, p.getName() + " is in water b6");
//	                }
//	                else {
//	                	T.sendMessage(p, p.getName() + " is in b6 " + b6.getType() + " might have also stood on the redstone block");
//	                }
//            	}
//            }
//        }, 6);
//	}
//	
}
