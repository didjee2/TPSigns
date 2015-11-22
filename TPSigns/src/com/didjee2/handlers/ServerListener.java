package com.didjee2.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.didjee2.TPSigns;
import com.didjee2.event.BungeeSignsInteractEvent;
import com.didjee2.event.BungeeSignsUpdateEvent;
import com.didjee2.ping.ServerInfo;
import com.didjee2.sign.BungeeSign;
import com.didjee2.sign.SignLayout;

public class ServerListener implements Listener {
	
	TPSigns plugin;
	private HashMap<Player, Long> cooldown = new HashMap<>();
	
	public ServerListener(TPSigns plugin){
		this.plugin = plugin;
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onCreateBungeeSign(SignChangeEvent event){
		if(event.getLine(0).equalsIgnoreCase("[tpsigns]") || event.getLine(0).equalsIgnoreCase("[tpsign]") || event.getLine(0).equalsIgnoreCase("tpsign")){
			if(event.getPlayer().hasPermission("tpsigns.create") || event.getPlayer().isOp()){
				String sname = event.getLine(1);
				String lname = event.getLine(2);
				if (lname.equalsIgnoreCase("")) {
					lname = "default";
				}
					
				Location location = event.getBlock().getLocation();
				ServerInfo server = TPSigns.getInstance().getConfigData().getServer(sname);
				SignLayout layout = TPSigns.getInstance().getConfigData().getLayout(lname);
				if (server != null) {
					if (layout != null){
						TPSigns.getInstance().getConfigData().addSign(location, server, layout);
						event.getPlayer().sendMessage(TPSigns.pre + "§aSign sucessfully created.");
					} else {
						event.getPlayer().sendMessage(TPSigns.pre + "§cLayout '" + event.getLine(2) + "' not exists!");
						event.getBlock().breakNaturally();
					}
				} else {
					event.getPlayer().sendMessage(TPSigns.pre + "§cServer '" + event.getLine(1) + "' not exists!");
					event.getBlock().breakNaturally();
				}
			} else {
				event.getPlayer().sendMessage(TPSigns.pre + "§cYou don't have permission to do this!");
				event.getBlock().breakNaturally();
			}
		}
	}
	

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onBungeeSignUpdate(BungeeSignsUpdateEvent event){
		List<BlockFace> bflist = new ArrayList<BlockFace>();
		bflist.add(BlockFace.DOWN);
		bflist.add(BlockFace.EAST);
		bflist.add(BlockFace.NORTH);
		bflist.add(BlockFace.UP);
		bflist.add(BlockFace.WEST);
		bflist.add(BlockFace.SOUTH);
		for(BungeeSign sign : event.getSigns()){
			HashMap<String, Byte> map = sign.getLayout().getMap();
			for(String s : map.keySet()){
				
				if(s.toLowerCase().contains("online")){
					if(sign.getServer().isOnline()){
						Byte b = map.get(s);
						for(BlockFace bf : bflist){
							Block block = sign.getLocation().getBlock().getRelative(bf);
							if(block.getType().equals(Material.STAINED_GLASS) || block.getType().equals(Material.STAINED_CLAY) || block.getType().equals(Material.WOOL) || block.getType().equals(Material.STAINED_GLASS_PANE)){
								block.setData(b);
							}
						}
					}
				}
				
				if(s.toLowerCase().contains("offline")){
					if(!sign.getServer().isOnline()){
						Byte b = map.get(s);
						for(BlockFace bf : bflist){
							Block block = sign.getLocation().getBlock().getRelative(bf);
							if(block.getType().equals(Material.STAINED_GLASS) || block.getType().equals(Material.STAINED_CLAY) || block.getType().equals(Material.WOOL) || block.getType().equals(Material.STAINED_GLASS_PANE)){
								block.setData(b);
							}
						}
					}
				}
				
				if(sign.getServer().isOnline() && sign.getServer().getMotd() != null && sign.getServer().getMotd().contains(s)){
					Byte b = map.get(s);
					
					
					for(BlockFace bf : bflist){
						Block block = sign.getLocation().getBlock().getRelative(bf);
						if(block.getType().equals(Material.STAINED_GLASS) || block.getType().equals(Material.STAINED_CLAY) || block.getType().equals(Material.WOOL) || block.getType().equals(Material.STAINED_GLASS_PANE)){
							block.setData(b);
						}
					}
				}
			}
		}
	}
	
	
	@EventHandler(ignoreCancelled = true)
	public void onRemoveBungeeSign(BlockBreakEvent event)
	{
		if(!event.isCancelled())
		{
			if(event.getBlock().getState() instanceof Sign)
			{
				if(TPSigns.getInstance().getConfigData().containsSign(event.getBlock()))
				{
					if(event.getPlayer().hasPermission("tpsigns.destroy") || event.getPlayer().isOp())
					{
						TPSigns.getInstance().getConfigData().removeSign(event.getBlock().getLocation());
						event.getPlayer().sendMessage(TPSigns.pre + "§aSign sucessfully destroyed.");
					}
					else
					{
						event.getPlayer().sendMessage(TPSigns.pre + "§cYou don't have permission to do this!");
						event.setCancelled(true);
					}
				}
			}
		}
		else
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if(!event.isCancelled())
		{
			if(event.getAction() == Action.RIGHT_CLICK_BLOCK)
			{
				if(event.getClickedBlock().getState() instanceof Sign)
				{
					if(TPSigns.getInstance().getConfigData().getBlocks().contains(event.getClickedBlock()))
					{
						if(event.getPlayer().hasPermission("tpsigns.use") || event.getPlayer().isOp())
						{
							for(BungeeSign sign : TPSigns.getInstance().getConfigData().getSigns())
							{
								if(sign != null && !sign.isBroken() && sign.getLocation().equals(event.getClickedBlock().getLocation()))
								{
									ServerInfo server = sign.getServer();
									if(server != null)
									{
										BungeeSignsInteractEvent e = new BungeeSignsInteractEvent(event.getPlayer(), sign, server);
										Bukkit.getPluginManager().callEvent(e);
										event.setCancelled(true);
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBungeeSignInteract(BungeeSignsInteractEvent event)
	{
		if(!event.isCancelled())
		{
			if(event.getSign().getLayout().isTeleport())
			{
				if(event.getServer().isOnline())
				{
					if(!hasCooldown(event.getPlayer()))
					{
						addCooldown(event.getPlayer());
						event.getServer().teleportPlayer(event.getPlayer());
					}
					else
					{
						event.getPlayer().sendMessage(event.getSign().getLayout().parseCooldownMessage(getCooldown(event.getPlayer())));
					}
				}
				else
				{
					event.getPlayer().sendMessage(event.getSign().getLayout().parseOfflineMessage(event.getServer()));
				}
			}
		}
	}
	
	private boolean hasCooldown(Player player)
	{
		if(TPSigns.getInstance().getConfigData().getCooldown() != 0)
		{
			if(!player.hasPermission("tpsigns.use.nocooldown"))
			{
				if(cooldown.containsKey(player))
				{
					long time = System.currentTimeMillis();
					long cooldown = this.cooldown.get(player);
					long result = (time - cooldown);
					
					if(result >= TPSigns.getInstance().getConfigData().getCooldown())
					{
						this.cooldown.remove(player);
						return false;
					}
					
					return true;
				}
			}
			else
			{
				return false;
			}
		}
		
		return false;
	}
	
	private void addCooldown(Player player)
	{
		if(TPSigns.getInstance().getConfigData().getCooldown() != 0)
		{
			if(!player.hasPermission("tpsigns.use.nocooldown"))
			{
				if(!cooldown.containsKey(player))
				{
					cooldown.put(player, System.currentTimeMillis());
				}
			}
		}
	}
	
	private int getCooldown(Player player)
	{
		if(TPSigns.getInstance().getConfigData().getCooldown() != 0)
		{
			if(!player.hasPermission("tpsigns.use.nocooldown"))
			{
				if(cooldown.containsKey(player))
				{
					long time = System.currentTimeMillis();
					long cooldown = this.cooldown.get(player);
					long result = (cooldown - time);
					int wait = Integer.parseInt(result / 1000 + "");
					int towait = (int) ((TPSigns.getInstance().getConfigData().getCooldown() / 1000) + wait);
					
					return towait;
				}
			}
		}
		return 0;
	}
}
