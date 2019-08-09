package com.zyrox.events;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.cloutteam.samjakob.gui.types.PaginatedGUI;

public class Main extends JavaPlugin
{

	public static Main main;
	private EventManager eventManager;
	
	public static FileConfiguration statscfg;
	public static File statsfile;
	
	public static FileConfiguration msgcfg;
	public static File msgfile;

	private CommandMain cmdMain = new CommandMain();
	private CommandConfig cmdConfig = new CommandConfig();

	@Override
	public void onEnable()
	{
		super.onEnable();

		main = this;
		Bukkit.getServer().getPluginManager().registerEvents(new MoveEvent(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new HostGUI(), this);
		
		// Load later in case we need to wait for some world loading plugins
		Bukkit.getScheduler().runTaskLater(this, () ->
		{
			eventManager = new EventManager();

			PluginCommand events = getCommand("events");
			events.setExecutor(cmdMain);
			events.setTabCompleter(cmdMain);
			PluginCommand config = getCommand("eventsconfig");
			config.setExecutor(cmdConfig);
			config.setTabCompleter(cmdConfig);
			setupstats();
			setupmsgs();
			getCommand("eventstats").setExecutor(new StatConfig(this));
			PaginatedGUI.prepare(this);

		}, 3);

	}

	public static Main get()
	{
		return main;
	}

	public EventManager getEventManager()
	{
		return eventManager;
	}
	
	public void setup() {
		setupstats();
		setupmsgs();
	}
	
	private void setupstats() {
    	statsfile = new File(getDataFolder(), "stats.yml");
    	if (!statsfile.exists()) {
    		statsfile.getParentFile().mkdirs();
    		saveResource("stats.yml", false);
    	}
    	
    	statscfg = new YamlConfiguration();
    	try {
    		statscfg.load(statsfile);
    	} catch (IOException | InvalidConfigurationException e) {
    		e.printStackTrace();
    	}
    }
	
	private void setupmsgs() {
    	msgfile = new File(getDataFolder(), "messages.yml");
    	if (!msgfile.exists()) {
    		msgfile.getParentFile().mkdirs();
    		saveResource("messages.yml", false);
    	}
    	
    	msgcfg = new YamlConfiguration();
    	try {
    		msgcfg.load(msgfile);
    	} catch (IOException | InvalidConfigurationException e) {
    		e.printStackTrace();
    	}
    }
	
	public FileConfiguration getFilestats() {
		return statscfg;
	}
	
	public FileConfiguration getFilemsgs() {
		return msgcfg;
	}
	
	public void saveFileStats() {
        try {
            this.statscfg.save(this.statsfile);
        } catch (IOException e) {
        	getLogger().warning("Unable to save stats.yml");
        }
    }
 
    public void reloadFileStats() {
        this.statscfg = YamlConfiguration.loadConfiguration(this.statsfile);
    }
    
    public void saveFilemsgs() {
        try {
            this.msgcfg.save(this.msgfile);
        } catch (IOException e) {
        	getLogger().warning("Unable to save messages.yml");
        }
    }
 
    public void reloadFilemsgs() {
        this.msgcfg = YamlConfiguration.loadConfiguration(this.msgfile);
    }
}