package com.didjee2.scheduler;

import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.didjee2.TPSigns;
import com.didjee2.event.BungeeSignsUpdateEvent;
import com.didjee2.sign.BungeeSign;

public class SignScheduler implements Runnable, Listener
{
	private final TPSigns plugin;
	
	public SignScheduler(TPSigns plugin)
	{
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@Override
	public void run()
	{
		final List<BungeeSign> signs = plugin.getConfigData().getSigns();
		BungeeSignsUpdateEvent event = new BungeeSignsUpdateEvent(signs);
		Bukkit.getPluginManager().callEvent(event);
		Bukkit.getScheduler().runTaskLater(plugin, this, plugin.getConfigData().getUpdateInterval());
	}
	
	@EventHandler
	public void onEvent(BungeeSignsUpdateEvent event)
	{
		if(!event.isCancelled())
		{
			Iterator<BungeeSign> list = event.getSigns().iterator();
			while(list.hasNext())
			{
				BungeeSign sign = list.next();
				if(sign != null) sign.updateSign();
			}
		}
	}
	
}
