package com.casino;

import com.casino.commands.CasinoCommand;
import com.casino.listeners.ShulkerListener;
import com.casino.utils.ConfigManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class CasinoPlugin extends JavaPlugin {
    
    private static CasinoPlugin instance;
    private Economy economy;
    private ConfigManager configManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config
        saveDefaultConfig();
        
        // Initialize config manager
        configManager = new ConfigManager(this);
        
        // Setup Vault Economy
        if (!setupEconomy()) {
            getLogger().severe("====================================");
            getLogger().severe("Vault not found! Disabling plugin.");
            getLogger().severe("Please install Vault to use this plugin.");
            getLogger().severe("====================================");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Register commands
        getCommand("csn").setExecutor(new CasinoCommand(this));
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new ShulkerListener(this), this);
        
        getLogger().info("====================================");
        getLogger().info("Casino Plugin has been enabled!");
        getLogger().info("Version: " + getDescription().getVersion());
        getLogger().info("====================================");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Casino Plugin has been disabled!");
    }
    
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager()
                .getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }
    
    public static CasinoPlugin getInstance() {
        return instance;
    }
    
    public Economy getEconomy() {
        return economy;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
}
