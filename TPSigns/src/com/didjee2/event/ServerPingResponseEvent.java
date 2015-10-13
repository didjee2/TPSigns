package com.didjee2.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.didjee2.ping.ServerInfo;
import com.didjee2.ping.ServerPing;
import com.didjee2.ping.ServerPing.StatusResponse;

public class ServerPingResponseEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	private ServerInfo server;
	private ServerPing ping;
	private StatusResponse response;
	
	public ServerPingResponseEvent(ServerInfo server, ServerPing ping, StatusResponse response)
	{
		this.server = server;
		this.ping = ping;
		this.response = response;
	}
	
	public HandlerList getHandlers() 
	{
        return handlers;
    }
	
	public static HandlerList getHandlerList() 
	{
        return handlers;
    }
	
	public ServerInfo getServer()
	{
		return server;
	}
	
	public ServerPing getPing()
	{
		return ping;
	}
	
	public StatusResponse getResponse()
	{
		return response;
	}
}
