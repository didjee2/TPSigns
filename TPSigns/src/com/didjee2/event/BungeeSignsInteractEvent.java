package com.didjee2.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.didjee2.ping.ServerInfo;
import com.didjee2.sign.BungeeSign;

public class BungeeSignsInteractEvent extends Event implements Cancellable
{
	private static final HandlerList handlers = new HandlerList();
	private Player player;
	private BungeeSign sign;
	private ServerInfo server;
	private boolean cancelled;
	
	public BungeeSignsInteractEvent(Player player, BungeeSign sign, ServerInfo server)
	{
		this.player = player;
		this.sign = sign;
		this.server = server;
	}
	
	public HandlerList getHandlers() 
	{
        return handlers;
    }
	
	public static HandlerList getHandlerList() 
	{
        return handlers;
    }
	
	public Player getPlayer()
	{
		return player;
	}
	
	public BungeeSign getSign()
	{
		return sign;
	}
	
	public ServerInfo getServer()
	{
		return server;
	}
	
	public void setServer(ServerInfo server)
	{
		this.server = server;
	}
	
	public void setCancelled(boolean cancel)
	{
		this.cancelled = cancel;
	}

	public boolean isCancelled() 
	{
		return cancelled;
	}
}
