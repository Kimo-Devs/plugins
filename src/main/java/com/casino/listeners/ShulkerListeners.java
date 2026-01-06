package com.casino.listeners;

import com.casino.CasinoPlugin;
import com.casino.utils.ConfigManager;
import com.casino.utils.RegionChecker;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;

import java.text.DecimalFormat;
import java.util.List;

public class ShulkerListener implements Listener {
    
    private final CasinoPlugin plugin;
    private final DecimalFormat df = new DecimalFormat("#,###");
    
    public ShulkerListener(CasinoPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onShulkerBoxOpen(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        
        if (block == null) return;
        
        if (!isShulkerBox(block.getType())) return;
        
        ConfigManager config = plugin.getConfigManager();
        String regionName = config.getCasinoRegion();
        
        if (!regionName.isEmpty() && !RegionChecker.isInRegion(player, regionName)) {
            return;
        }
        
        ConfigManager.TitleData titleData = config.getTitleData("shulker-open");
        if (titleData != null) {
            player.sendTitle(titleData.title, titleData.subtitle,
                    titleData.fadeIn, titleData.stay, titleData.fadeOut);
        }
        
        List<String> messages = config.getMessageList("shulker-info");
        for (String message : messages) {
            message = message.replace("${min-amount}", df.format(config.getMinAmount()));
            player.sendMessage(message);
        }
    }
    
    @EventHandler
    public void onShulkerBoxClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        
        Player player = (Player) event.getPlayer();
        InventoryHolder holder = event.getInventory().getHolder();
        
        if (!(holder instanceof ShulkerBox)) return;
        
        ConfigManager config = plugin.getConfigManager();
        String regionName = config.getCasinoRegion();
        
        if (!regionName.isEmpty() && !RegionChecker.isInRegion(player, regionName)) {
            return;
        }
        
        ConfigManager.TitleData titleData = config.getTitleData("shulker-close");
        if (titleData != null) {
            player.sendTitle(titleData.title, titleData.subtitle,
                    titleData.fadeIn, titleData.stay, titleData.fadeOut);
        }
    }
    
    private boolean isShulkerBox(Material material) {
        return material.name().contains("SHULKER_BOX");
    }
}
