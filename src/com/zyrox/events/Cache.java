package com.zyrox.events;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Cache
{
	private String name;
	private Map<UUID, FileConfiguration> data = new HashMap<>();

	public Cache(String name)
	{
		this.name = name;
	}

	private FileConfiguration getOrLoad(UUID uuid)
	{
		return data.computeIfAbsent(uuid, (u) -> UtilFileCache.read(name, u.toString()));
	}

	public Object get(Player player, String key)
	{
		return getOrLoad(player.getUniqueId()).get(key);
	}

	public Object get(Player player, String key, Object def)
	{
		return getOrLoad(player.getUniqueId()).get(key, def);
	}

	public long get(Player player, String key, long def)
	{
		return getOrLoad(player.getUniqueId()).getLong(key, def);
	}

	public String get(Player player, String key, String def)
	{
		return getOrLoad(player.getUniqueId()).getString(key, def);
	}

	public void set(Player player, String key, Object value)
	{
		FileConfiguration config = getOrLoad(player.getUniqueId());
		config.set(key, value);
		UtilFileCache.save(name, player.getUniqueId().toString(), config);
	}

}
