package com.zyrox.events;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class T
{

	public static String body = ChatColor.GRAY + "";
	public static String gold = ChatColor.GOLD + "";
	public static String prefix = body + "[" + gold + "Events" + body + "] ";
	public static String dr = ChatColor.DARK_RED + "";
	public static String noPerm = "&cI'm sorry, but you do not have permission to perform this command.";

	public static String g(String msg)
	{
		return msg;
	}

	public static String formatSeconds(long seconds)
	{
		long days = seconds / (24 * 60 * 60);
		seconds %= 24 * 60 * 60;
		long hh = seconds / (60 * 60);
		seconds %= 60 * 60;
		long mm = seconds / 60;
		seconds %= 60;
		long ss = seconds;

		if (days > 0)
		{
			return days + "d " + hh + "h " + mm + "m " + ss + "s";
		}
		if (hh > 0)
		{
			return hh + "h " + mm + "m " + ss + "s";
		}
		if (mm > 0)
		{
			return mm + "m " + ss + "s";
		}
		return ss + "s";
	}

	public static String main(String string)
	{
		StringBuilder sb = new StringBuilder();
		for (String line : string.split("\n"))
		{
			if (sb.length() > 0)
			{
				sb.append("\n");
			}
			sb.append(line);
		}
		return sb.toString();
	}

	public static List<String> startsWith(String prefix, String... input)
	{
		prefix = prefix.toLowerCase();
		List<String> matches = new ArrayList<>();
		for (String s : input)
		{
			if (s.toLowerCase().startsWith(prefix))
			{
				matches.add(s);
			}
		}
		return matches;
	}
	
	public static String replace(String text) {
		return ChatColor.translateAlternateColorCodes('&', text);
	}
	
	public static void sendMessage(CommandSender sender, String message) {
		sender.sendMessage(replace(message));
	}
	
	public static void bMsg(String message) {
		for (Player p : Bukkit.getOnlinePlayers()) { sendMessage(p, message); }
	}
	
	public static void sendConsoleMessage(String message) {
		Main.main.getServer().getConsoleSender().sendMessage(replace(message));
	}

}
