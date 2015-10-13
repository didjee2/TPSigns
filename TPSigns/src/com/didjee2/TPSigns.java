package com.didjee2;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.didjee2.handlers.ServerListener;
import com.didjee2.ping.ServerInfo;
import com.didjee2.scheduler.AnimationTask;
import com.didjee2.scheduler.PingScheduler;
import com.didjee2.scheduler.SignScheduler;
import com.didjee2.sign.BungeeSign;
import com.didjee2.sign.SignLayout;

public class TPSigns extends JavaPlugin implements PluginMessageListener
{
	private PingScheduler ping;
	private SignScheduler sign;
	private AnimationTask anim;
	private static TPSigns instance;
	private static ConfigData data;
	public static String pre = "§7[§3TPSigns§7] §r";
	
	@SuppressWarnings("deprecation")
	@Override
	public void onEnable() {
		instance = this;
		
		File config_file = new File("plugins/TPSigns/config.yml");
		if(!config_file.exists()){
			Bukkit.getServer().getLogger().info("===========[ TPSigns ]===========");
			Bukkit.getServer().getLogger().info("Thanks for Buying & Using TPSigns");
			Bukkit.getServer().getLogger().info("Problems? PM me (didjee2) on Spigot!");
			Bukkit.getServer().getLogger().info("===========[ TPSigns ]===========");
		}
		
		data = new ConfigData(this);
		pre = "§7[§3TPSigns§7] §r";
		Bukkit.getPluginManager().registerEvents(new ServerListener(this), this);
		this.ping = new PingScheduler(this);
		this.sign = new SignScheduler(this);
		
		data.loadConfig();
		
		this.anim = new AnimationTask(this);
		anim.resetAnimation();
		anim.startAnimation();

		List<BlockFace> bflist = new ArrayList<BlockFace>();
		bflist.add(BlockFace.DOWN);
		bflist.add(BlockFace.EAST);
		bflist.add(BlockFace.NORTH);
		bflist.add(BlockFace.UP);
		bflist.add(BlockFace.WEST);
		bflist.add(BlockFace.SOUTH);
		
		for(BungeeSign sign : getConfigData().getSigns()){
			for(BlockFace f : bflist){
				Block b = sign.getLocation().getBlock().getRelative(f);
				if(b.getType().equals(Material.STAINED_GLASS) || b.getType().equals(Material.STAINED_CLAY) || b.getType().equals(Material.WOOL) || b.getType().equals(Material.STAINED_GLASS_PANE)){
					b.setData(DyeColor.WHITE.getData());
				}
			}
		}
		
		long time = (long) (10.3 * 20L);
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				Bukkit.getScheduler().runTaskLater(instance, sign, 40L);
				Bukkit.getScheduler().runTaskLaterAsynchronously(instance, ping, 5L);
				
				getServer().getMessenger().registerOutgoingPluginChannel(instance, "BungeeCord");
				getServer().getMessenger().registerIncomingPluginChannel(instance, "BungeeCord", instance);
			}
		}, time);
		
		getLogger().info("Enabled TPSigns by didjee2");
	}
	
	@Override
	public void onDisable() {
		anim.stopAnimation();
		anim.resetAnimation();
		
		data.unloadConfig();
		
		getLogger().info("Disabled TPSigns by didjee2");
		getLogger().info("Thank you for using TPSigns.");
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) 
	{
		if(command.getName().equalsIgnoreCase("tpsign") || command.getName().equalsIgnoreCase("tpsigns"))
		{
			if(sender.hasPermission("tpsigns.admin"))
			{
				if(args.length == 1){
				if(args[0].equalsIgnoreCase("reload")){
					alertOperators(sender, "§e§oReloading TPSigns...§7§o");
					sender.sendMessage(pre + "§eReloading TPSigns...");
					this.reloadConfig();
					Bukkit.getPluginManager().disablePlugin(this);
					Bukkit.getPluginManager().enablePlugin(this);
					alertOperators(sender, "§a§oTPSigns sucessfully reloaded.§7§o");
					sender.sendMessage(pre + "§aTPSigns sucessfully reloaded.");
					return true;
					} if(args[0].equalsIgnoreCase("help")){
						sender.sendMessage("§c------------------------------------------");
						sender.sendMessage("§7TPSigns Created by didjee2");
						sender.sendMessage("§7Commands List:");
						sender.sendMessage("§7- §a/tpsigns reload");
						sender.sendMessage("§7- §a/tpsigns help");
						sender.sendMessage("§7- §a/tpsigns create (servername) (layoutname)!");
						sender.sendMessage("§c------------------------------------------");
					} if(args[0].equalsIgnoreCase("remove")){
						if(sender instanceof Player){
							Player p = (Player)sender;
							Block b = p.getTargetBlock((HashSet<Byte>)null, 10);
							if(b.getType() == Material.SIGN || b.getType() == Material.WALL_SIGN){
								Sign sign = (Sign)b.getState();
								if(TPSigns.getInstance().getConfigData().containsSign(sign.getBlock()))
								{
									TPSigns.getInstance().getConfigData().removeSign(sign.getBlock().getLocation());
								}
							} else {
								sender.sendMessage(TPSigns.pre + "§aYou are not looking at a sign!");
							}
						} 
					} else if((!args[0].equalsIgnoreCase("help")) && (!args[0].equalsIgnoreCase("reload")) && (!args[0].equalsIgnoreCase("remove"))){
						sender.sendMessage("§c------------------------------------------");
						sender.sendMessage("§e             TPSigns Created by didjee2");
						sender.sendMessage("§a       Use /tpsigns help for more help!");
						sender.sendMessage("§c------------------------------------------");
					}
				} if(args.length == 3){
					 if(args[0].equalsIgnoreCase("create")){
						if(sender instanceof Player){
							Player p = (Player)sender;
							Block b = p.getTargetBlock((HashSet<Byte>)null, 10);
							if(b.getType() == Material.SIGN || b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST){
								Sign sign = (Sign)b.getState();
								
								if(TPSigns.getInstance().getConfigData().containsSign(sign.getBlock()))
								{
									TPSigns.getInstance().getConfigData().removeSign(sign.getBlock().getLocation());
								}
								
								sign.setLine(0, "[tpsign]");
								sign.setLine(1, args[1]);
								sign.setLine(2, args[2]);
								
								String sname = sign.getLine(1);
								String lname = sign.getLine(2);
								if (lname.equalsIgnoreCase("")) 
								{
									lname = "default";
							    }
								
								Location location = sign.getBlock().getLocation();
								ServerInfo server = TPSigns.getInstance().getConfigData().getServer(sname);
								SignLayout layout = TPSigns.getInstance().getConfigData().getLayout(lname);
								if (server != null) 
						        {
									if (layout != null)
								    {
										TPSigns.getInstance().getConfigData().addSign(location, server, layout);
							        	p.sendMessage(TPSigns.pre + "§aSign sucessfully created.");
								    }
									else
									{
										p.getPlayer().sendMessage(TPSigns.pre + "§cLayout '" + sign.getLine(2) + "' not exists!");
										sign.getBlock().breakNaturally();
									}
						        }
						        else
						        {
						        	p.sendMessage(TPSigns.pre + "§cServer '" + sign.getLine(1) + "' not exists!");
						        	sign.getBlock().breakNaturally();
						        }
							} else {
								sender.sendMessage(TPSigns.pre + "§aYou are not looking at a sign!");
							}
						} else {
							sender.sendMessage(TPSigns.pre + "§aYou need to be ingame to use this command!");
						}
					 } else {
							sender.sendMessage("§c------------------------------------------");
							sender.sendMessage("§e             TPSigns Created by didjee2");
							sender.sendMessage("§a       Use /tpsigns help for more help!");
							sender.sendMessage("§c------------------------------------------"); 
					 }
				} else if((args.length != 3) && (args.length != 1)){
					sender.sendMessage("§c------------------------------------------");
					sender.sendMessage("§e             TPSigns Created by didjee2");
					sender.sendMessage("§a       Use /tpsigns help for more help!");
					sender.sendMessage("§c------------------------------------------");
				}
			}
			else
			{
				sender.sendMessage(pre + "§cYou don't have permission to use this command!");
				return true;
			}
		}
		return true;
	}
	
	public static TPSigns getInstance()
	{
		return instance;
	}
	
	public ConfigData getConfigData()
	{
		return data;
	}
	
	@SuppressWarnings("deprecation")
	private void alertOperators(CommandSender sender, String alert)
	{
		for(Player player : Bukkit.getOnlinePlayers())
		{
			if(player.isOp())
			{
				if(!sender.getName().equals(player.getName()))
				{
					player.sendMessage("§7§o["+sender.getName()+": "+alert+"]");
				}
			}
		}
	}
	
	public void callSyncEvent(final Event event)
	{
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable()
		{
			@Override
			public void run() 
			{
				getServer().getPluginManager().callEvent(event);
			}
		});
	}
	
	public void logConsole(Level level, String error)
	{
		if(data.getConsoleLog() == true)
		{
			Bukkit.getLogger().log(level, error);
		}
	}
	
	//Bungeecord
	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] msg)
	{
		if (!channel.equals("BungeeCord")) 
		{
			return;
	    }
	}
}
