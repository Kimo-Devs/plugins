package com.casino.utils;

import com.casino.CasinoPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    
    private final CasinoPlugin plugin;
    
    public ConfigManager(CasinoPlugin plugin) {
        this.plugin = plugin;
    }
    
    public int getMinAmount() {
        return plugin.getConfig().getInt("min-amount", 2500);
    }
    
    public String getCasinoRegion() {
        return plugin.getConfig().getString("casino-region", "casino");
    }
    
    public int getAnimationDuration() {
        return plugin.getConfig().getInt("animation.duration-ticks", 60);
    }
    
    public int getTickInterval() {
        return plugin.getConfig().getInt("animation.tick-interval", 4);
    }
    
    public int getCooldown() {
        return plugin.getConfig().getInt("cooldown", 1);
    }
    
    public boolean isSoundEnabled() {
        return plugin.getConfig().getBoolean("sounds.enabled", true);
    }
    
    public String getSound(String type) {
        return plugin.getConfig().getString("sounds." + type, "");
    }
    
    public String getMessage(String path) {
        String message = plugin.getConfig().getString("messages.chat." + path, "");
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public String getMessage(String path, String placeholder, String value) {
        String message = getMessage(path);
        return message.replace(placeholder, value);
    }
    
    public List<String> getMessageList(String path) {
        List<String> messages = plugin.getConfig().getStringList("messages.chat." + path);
        List<String> colored = new ArrayList<>();
        for (String msg : messages) {
            colored.add(ChatColor.translateAlternateColorCodes('&', msg));
        }
        return colored;
    }
    
    public TitleData getTitleData(String type) {
        ConfigurationSection section = plugin.getConfig()
                .getConfigurationSection("messages.title." + type);
        if (section == null) return null;
        
        String title = ChatColor.translateAlternateColorCodes('&', 
                section.getString("title", ""));
        String subtitle = ChatColor.translateAlternateColorCodes('&', 
                section.getString("subtitle", ""));
        int fadeIn = section.getInt("fade-in", 10);
        int stay = section.getInt("stay", 60);
        int fadeOut = section.getInt("fade-out", 20);
        
        return new TitleData(title, subtitle, fadeIn, stay, fadeOut);
    }
    
    public List<MultiplierData> getMultipliers() {
        List<MultiplierData> multipliers = new ArrayList<>();
        List<?> list = plugin.getConfig().getList("multipliers");
        
        if (list != null) {
            for (Object obj : list) {
                if (obj instanceof ConfigurationSection) {
                    ConfigurationSection section = (ConfigurationSection) obj;
                    double value = section.getDouble("value");
                    int weight = section.getInt("weight");
                    String display = ChatColor.translateAlternateColorCodes('&', 
                            section.getString("display", ""));
                    multipliers.add(new MultiplierData(value, weight, display));
                }
            }
        }
        
        return multipliers;
    }
    
    public static class TitleData {
        public final String title;
        public final String subtitle;
        public final int fadeIn;
        public final int stay;
        public final int fadeOut;
        
        public TitleData(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
            this.title = title;
            this.subtitle = subtitle;
            this.fadeIn = fadeIn;
            this.stay = stay;
            this.fadeOut = fadeOut;
        }
    }
    
    public static class MultiplierData {
        public final double value;
        public final int weight;
        public final String display;
        
        public MultiplierData(double value, int weight, String display) {
            this.value = value;
            this.weight = weight;
            this.display = display;
        }
    }
}
