package com.casino.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RegionChecker {
    
    public static boolean isInRegion(Player player, String regionName) {
        if (regionName == null || regionName.isEmpty()) {
            return true;
        }
        
        try {
            Location loc = player.getLocation();
            RegionContainer container = WorldGuard.getInstance()
                    .getPlatform()
                    .getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet set = query.getApplicableRegions(
                    BukkitAdapter.adapt(loc)
            );
            
            for (ProtectedRegion region : set) {
                if (region.getId().equalsIgnoreCase(regionName)) {
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            return true;
        }
    }
    
    public static boolean isWorldGuardAvailable() {
        try {
            Class.forName("com.sk89q.worldguard.WorldGuard");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
