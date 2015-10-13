package com.didjee2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.didjee2.ping.ServerInfo;
import com.didjee2.sign.BungeeSign;
import com.didjee2.sign.SignLayout;
import com.didjee2.utils.LocationSerialiser;

public class ConfigData 
{
	private TPSigns plugin;
	private boolean log;
	private FileConfiguration config;
	private FileConfiguration layout;
	private FileConfiguration sign;
	private List<ServerInfo> servers = new ArrayList<>();
	private List<BungeeSign> signs = new ArrayList<>();
	private List<Block> blocks = new ArrayList<>();
	private Map<String, SignLayout> layouts = new HashMap<>();
	private long cooldown;
	private long signUpdates;
	private int pingTimeout;
	private int pingInterval;
	
	private File config_file = new File("plugins/TPSigns/config.yml");
	private File layout_file = new File("plugins/TPSigns/layout.yml");
	private File sign_file = new File("plugins/TPSigns/signs.yml");
	
	public enum ConfigType {
		SETTINGS,
		LAYOUTS,
		SIGNS;
	}
	
	public ConfigData(TPSigns plugin) {
		this.plugin = plugin;
	}
	
	public void loadConfig() {
		config = null;
		layout = null;
		sign = null;
		
		servers.clear();
		signs.clear();
		layouts.clear();
		
		if(config_file.exists()){
			config = YamlConfiguration.loadConfiguration(config_file);
		} else {
			plugin.saveResource("config.yml", false);
			config = YamlConfiguration.loadConfiguration(config_file);
		}
		
		if(layout_file.exists()){
			layout = YamlConfiguration.loadConfiguration(layout_file);
		} else {
			plugin.saveResource("layout.yml", false);
			layout = YamlConfiguration.loadConfiguration(layout_file);
		}
		
		if(sign_file.exists()) {
			sign = YamlConfiguration.loadConfiguration(sign_file);
		} else {
			plugin.saveResource("signs.yml", false);
			sign = YamlConfiguration.loadConfiguration(sign_file);
		}
		
		loadSettings();
		loadServers();
		loadLayouts();
		loadSigns();
	}
	
	public void reloadConfig(){
		config = null;
		layout = null;
		sign = null;
		
		servers.clear();
		signs.clear();
		layouts.clear();
		
		loadConfig();
	}
	
	public void unloadConfig(){
		config = null;
		layout = null;
		sign = null;
		
		servers.clear();
		signs.clear();
		layouts.clear();
	}
	
	private void loadSettings(){
		this.log = this.config.getBoolean("options.logConsole");
		this.cooldown = (this.config.getInt("options.use-cooldown") * 1000);
		this.signUpdates = this.config.getInt("options.sign-updates");
		this.pingInterval = this.config.getInt("options.ping-interval");
		this.pingTimeout = this.config.getInt("options.ping-timeout");
	}
	
	private void loadServers(){
		ConfigurationSection srv = this.config.getConfigurationSection("servers");
		
		for(String server : srv.getKeys(false)){
			ConfigurationSection cs = srv.getConfigurationSection(server);
			String displayname = cs.getString("displayname");
			String[] address = cs.getString("address").split(":");
			String ip = address[0];
			String port = address[1];
			
			ServerInfo serverping = new ServerInfo(server, displayname, ip, Integer.valueOf(port), this.pingTimeout);
			serverping.resetPingDelay();
			this.servers.add(serverping);
		}
	}
	
	private void loadLayouts(){
		ConfigurationSection layouts = this.layout.getConfigurationSection("layouts");
		
		for(String layout : layouts.getKeys(false)) {
		    ConfigurationSection cs = layouts.getConfigurationSection(layout);
		    String online = cs.getString("online");
		    String offline = cs.getString("offline");
		    List<String> lines = cs.getStringList("layout");
		    boolean teleport = cs.getBoolean("teleport");
		    String offlineInt = cs.getString("offline-int");
		    String offlineMotd = cs.getString("offline-motd");
		    String offlineMessage = cs.getString("offline-message");
		    String cooldownMessage = cs.getString("cooldown-message");
		    
		    ConfigurationSection css = cs.getConfigurationSection("colors");
		    HashMap<String, Byte> glasscolors = new HashMap<String, Byte>();
		    for(String s : css.getKeys(false)){
		    	Byte b = Byte.valueOf(css.getString(s));
		    	if(b < 0 || b > 15){
		    		b = (byte)0;
		    	}
		    	glasscolors.put(s, b);
		    }
		    
		    SignLayout signLayout = new SignLayout(layout, online, offline, lines, teleport, offlineInt, offlineMotd, offlineMessage, cooldownMessage, glasscolors);
		    this.layouts.put(layout, signLayout);
		}
	}
	
	private void loadSigns(){
		for(String sign : this.sign.getStringList("signs")){
			Location location = LocationSerialiser.stringToLocationSign(sign);
			ServerInfo server = getServer(LocationSerialiser.getServerFromSign(sign));
			SignLayout layout = getLayout(LocationSerialiser.getLayoutFromSign(sign));
			
			Block b = location.getBlock();			
			BungeeSign bsign = new BungeeSign(server, location, layout);			
			this.signs.add(bsign);
			this.blocks.add(b);
		}
	}
	
	public SignLayout getLayout(String layout){
		return this.layouts.get(layout);
	}
	
	public ServerInfo getServer(String server){
		for(ServerInfo info : this.servers){
			if(info.getName().equals(server)){
				return info;
			}
		}
		
		return null;
	}
	
	public long getCooldown(){
		return this.cooldown;
	}
	
	public void setCooldown(int seconds){
		this.cooldown = seconds * 1000;
	}
	
	public boolean getConsoleLog(){
		return this.log;
	}
	
	public TPSigns getPlugin(){
	    return this.plugin;
	}

	public FileConfiguration getConfig(ConfigType type){
	    if(type.equals(ConfigType.SETTINGS)){
	    	return this.config;
	    } else if(type.equals(ConfigType.LAYOUTS)){
	    	return this.layout;
	    } else if(type.equals(ConfigType.SIGNS)){
	    	return this.sign;
	    }
	    
		return null;
	}
	
	public void saveConfig(ConfigType type) throws IOException{
		if(type.equals(ConfigType.SETTINGS)) {
	    	config.save(config_file);
	    } else if(type.equals(ConfigType.LAYOUTS)) {
	    	layout.save(layout_file);
	    } else if(type.equals(ConfigType.SIGNS)){
	    	sign.save(sign_file);
	    }
	}
	
	public List<ServerInfo> getServers(){
		return this.servers;
	}
	
	public void setServers(List<ServerInfo> servers){
	    this.servers = servers;
	}
	
	public List<BungeeSign> getSigns(){
		return this.signs;
	}
	
	public void setSigns(List<BungeeSign> signs){
	    this.signs = signs;
	}
	
	public Map<String, SignLayout> getLayouts(){
	    return this.layouts;
	}
	
	public void setLayouts(Map<String, SignLayout> signLayouts){
	    this.layouts = signLayouts;
	}

	public List<Block> getBlocks(){
		return this.blocks;
	}
	
	public void setBlocks(List<Block> blocks){
		this.blocks = blocks;
	}
	
	public long getUpdateInterval() {
		return signUpdates;
	}

	public void setUpdateInterval(long signUpdates) {
		this.signUpdates = signUpdates;
	}

	public int getPingInterval(){
		return this.pingInterval;
	}
	
	public int getPingTimeout(){
		return this.pingTimeout;
	}
	
	public void setPingInterval(int interval){
		this.pingInterval = interval;
	}
	
	public void setPingTimeout(int timeout){
		this.pingTimeout = timeout;
	}
	
	public BungeeSign getSignFromLocation(Location l){
		for(BungeeSign sign : signs){
			if(l.equals(sign.getLocation())){
				return sign;
			}
		}
		
		return null;
	}
	
	public boolean containsSign(Block b){
		if(blocks.contains(b)){
			return true;
		} else {
			return false;
		}
	}
	
	public void addSign(Location location, ServerInfo server, SignLayout layout){
		String index = LocationSerialiser.locationSignToString(location, server.getName(), layout.getName());
		List<String> list = this.sign.getStringList("signs");
		list.add(index);
		sign.set("signs", list);
		
		try {
			this.saveConfig(ConfigType.SIGNS);
		} catch (IOException e){
			e.printStackTrace();
		}
		
		blocks.add(location.getBlock());
		BungeeSign bsign = new BungeeSign(server, location, layout);
		signs.add(bsign);
	}
	
	public void removeSign(Location location){
		for(BungeeSign sign : signs){
			if(location.equals(sign.getLocation())){
				String index = LocationSerialiser.locationSignToString(location, sign.getServer().getName(), sign.getLayout().getName());
				List<String> list = this.sign.getStringList("signs");
				list.remove(index);
				this.sign.set("signs", list);
				
				try {
					this.saveConfig(ConfigType.SIGNS);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				blocks.remove(location);
				signs.remove(sign);
				break;
			}
		}
	}
}
