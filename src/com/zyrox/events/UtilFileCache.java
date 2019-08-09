package com.zyrox.events;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class UtilFileCache
{
	private static File getFile(String category, String key)
	{
		File file = new File(new File(Main.get().getDataFolder(), category), key + ".yml");
		file.getParentFile().mkdirs();
		return file;
	}

	public static FileConfiguration read(String category, String key)
	{
		File f = getFile(category, key);
		return YamlConfiguration.loadConfiguration(f);
	}

	public static void save(String category, String key, FileConfiguration config)
	{
		try
		{
			config.save(getFile(category, key));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
