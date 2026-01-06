package com.casino.commands;

import com.casino.CasinoPlugin;
import com.casino.utils.ConfigManager;
import com.casino.utils.RegionChecker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class CasinoCommand implements CommandExecutor {
    
    private final CasinoPlugin plugin;
    private final HashMap<Player, Long> cooldowns = new HashMap<>();
    private final HashMap<Player, Boolean> spinning = new HashMap<>();
    private final Random random = new Random();
    private final DecimalFormat df = new DecimalFormat("#,###");
    
    public CasinoCommand(CasinoPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ConfigManager config = plugin.getConfigManager();
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(config.getMessage("not-player"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (spinning.getOrDefault(player, false)) {
            player.sendMessage(config.getMessage("spinning"));
            return true;
        }
        
        if (cooldowns.containsKey(player)) {
            long timeLeft = (cooldowns.get(player) - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage(config.getMessage("spinning"));
                return true;
            }
        }
        
        String regionName = config.getCasinoRegion();
        if (!regionName.isEmpty() && !RegionChecker.isInRegion(player, regionName)) {
            if (!player.hasPermission("casino.bypass")) {
                player.sendMessage(config.getMessage("not-in-region"));
                return true;
            }
        }
        
        if (args.length != 1) {
            player.sendMessage(config.getMessage("usage"));
            return true;
        }
        
        int amount;
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(config.getMessage("invalid-number"));
            return true;
        }
        
        int minAmount = config.getMinAmount();
        if (amount < minAmount) {
            player.sendMessage(config.getMessage("min-amount", 
                    "${amount}", df.format(minAmount)));
            return true;
        }
        
        if (plugin.getEconomy().getBalance(player) < amount) {
            player.sendMessage(config.getMessage("insufficient-funds", 
                    "${amount}", df.format(amount)));
            return true;
        }
        
        startCasino(player, amount);
        return true;
    }
    
    private void startCasino(Player player, int amount) {
        ConfigManager config = plugin.getConfigManager();
        spinning.put(player, true);
        
        plugin.getEconomy().withdrawPlayer(player, amount);
        
        ConfigManager.TitleData startTitle = config.getTitleData("start");
        if (startTitle != null) {
            String title = startTitle.title.replace("{amount}", df.format(amount));
            player.sendTitle(title, startTitle.subtitle, 
                    startTitle.fadeIn, startTitle.stay, startTitle.fadeOut);
        }
        
        playSound(player, config.getSound("start"), 1.0f, 1.0f);
        
        List<ConfigManager.MultiplierData> multipliers = config.getMultipliers();
        if (multipliers.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Error: No multipliers configured!");
            spinning.put(player, false);
            plugin.getEconomy().depositPlayer(player, amount);
            return;
        }
        
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = config.getAnimationDuration();
            final int interval = config.getTickInterval();
            int currentIndex = 0;
            
            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    ConfigManager.MultiplierData result = getRandomMultiplier(multipliers);
                    finishCasino(player, amount, result);
                    cancel();
                    return;
                }
                
                ConfigManager.MultiplierData current = multipliers.get(currentIndex % multipliers.size());
                player.sendTitle("", current.display, 0, 15, 0);
                
                if (ticks % interval == 0) {
                    float pitch = 1.0f + (ticks * 0.01f);
                    playSound(player, config.getSound("spin"), 0.5f, pitch);
                }
                
                currentIndex++;
                ticks += interval;
            }
        }.runTaskTimer(plugin, 20L, config.getTickInterval());
    }
    
    private ConfigManager.MultiplierData getRandomMultiplier(
            List<ConfigManager.MultiplierData> multipliers) {
        
        int totalWeight = 0;
        for (ConfigManager.MultiplierData mult : multipliers) {
            totalWeight += mult.weight;
        }
        
        int randomNum = random.nextInt(totalWeight);
        int currentWeight = 0;
        
        for (ConfigManager.MultiplierData mult : multipliers) {
            currentWeight += mult.weight;
            if (randomNum < currentWeight) {
                return mult;
            }
        }
        
        return multipliers.get(0);
    }
    
    private void finishCasino(Player player, int amount, 
                             ConfigManager.MultiplierData result) {
        ConfigManager config = plugin.getConfigManager();
        
        if (result.value == 0) {
            ConfigManager.TitleData zonkTitle = config.getTitleData("zonk");
            if (zonkTitle != null) {
                player.sendTitle(zonkTitle.title, zonkTitle.subtitle,
                        zonkTitle.fadeIn, zonkTitle.stay, zonkTitle.fadeOut);
            }
            
            playSound(player, config.getSound("zonk"), 1.0f, 0.8f);
            player.sendMessage(config.getMessage("zonk-result", 
                    "${amount}", df.format(amount)));
            
        } else {
            int winAmount = (int) (amount * result.value);
            plugin.getEconomy().depositPlayer(player, winAmount);
            
            ConfigManager.TitleData winTitle = config.getTitleData("win");
            if (winTitle != null) {
                String title = winTitle.title.replace("{multiplier}", result.display);
                String subtitle = winTitle.subtitle.replace("{amount}", df.format(winAmount));
                player.sendTitle(title, subtitle,
                        winTitle.fadeIn, winTitle.stay, winTitle.fadeOut);
            }
            
            playSound(player, config.getSound("win"), 1.0f, 1.2f);
            player.sendMessage(config.getMessage("win-result", 
                    "${amount}", df.format(winAmount))
                    .replace("{multiplier}", String.valueOf(result.value)));
        }
        
        int cooldownSeconds = config.getCooldown();
        cooldowns.put(player, System.currentTimeMillis() + (cooldownSeconds * 1000L));
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            spinning.put(player, false);
            cooldowns.remove(player);
        }, 20L * cooldownSeconds);
    }
    
    private void playSound(Player player, String soundName, float volume, float pitch) {
        if (!plugin.getConfigManager().isSoundEnabled()) return;
        if (soundName == null || soundName.isEmpty()) return;
        
        try {
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound: " + soundName);
        }
    }
          }
