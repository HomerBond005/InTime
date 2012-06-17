/*
 * Copyright HomerBond005
 * 
 *  Published under CC BY-NC-ND 3.0
 *  http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package de.HomerBond005.InTime;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class InTime extends JavaPlugin{
	private Map<String, List<String>> plugins;
	private Map<String, String[]> commands;
	private int task;
	private PluginManager pm;
	private Metrics metrics;
	private Updater updater;
	private Logger log;
	
	@Override
	public void onEnable(){
		log = getLogger();
		pm = getServer().getPluginManager();
		if(!new File(getDataFolder()+File.separator+"config.yml").exists()){
			getConfig().options().copyDefaults(true);
			saveConfig();
		}else{
			getConfig().options().copyDefaults(false);
		}
		reload();
		getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable(){
			public void run(){
				initPlugins(hours(), minutes());
			}
		});
		task = getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
		    public void run() {
		        managePlugins(hours(), minutes());
		        manageCommands(hours(), minutes());
		    }
		}, 0L, 600L);
		try{
			metrics = new Metrics(this);
			metrics.start();
		}catch(IOException e){
			log.log(Level.WARNING, "Error while enabling Metrics.");
		}
		updater = new Updater(this);
		getServer().getPluginManager().registerEvents(updater, this);
		log.log(Level.INFO, "Current time: " + t(hours()) + ":" + t(minutes()));
		log.log(Level.INFO, "is enabled!");
	}
	
	@Override
	public void onDisable(){
		getServer().getScheduler().cancelTask(task);
		getServer().getScheduler().cancelTasks(this);
		log.log(Level.INFO, "is disabled!");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
		if(command.getName().toLowerCase().equals("intime")){
			if(sender.isOp()||!(sender instanceof org.bukkit.entity.Player)){
				sender.sendMessage(ChatColor.GREEN+"InTime v"+getDescription().getVersion());
				reload();
				sender.sendMessage(ChatColor.DARK_GREEN+"Successfully reloaded.");
			}else
				return false;
		}
		return true;
	}
	
	private void reload(){
		//PLUGINS
		reloadConfig();
		Set<String> pluginnames = getConfig().getConfigurationSection("plugins").getKeys(false);
		plugins = new HashMap<String, List<String>>();
		for(String plugin : pluginnames){
			if(getConfig().getString("plugins." + plugin + ".type").equals("enable")){
				plugins.put(plugin + ".enable", getConfig().getStringList("plugins." + plugin + ".times"));
			}else if(getConfig().getString("plugins." + plugin + ".type").equals("disable")){
				plugins.put(plugin + ".disable", getConfig().getStringList("plugins." + plugin + ".times"));
			}
		}
		//COMMANDS
		commands = new HashMap<String, String[]>();
		Set<String> commandnames = getConfig().getConfigurationSection("commands").getKeys(false);
		for(String command : commandnames){
			String[] value = {getConfig().getString("commands." + command + ".command"), getConfig().getString("commands." + command + ".arguments", ""), getConfig().getString("commands." + command + ".time")};
			commands.put(command, value);
		}
	}
	
	//COMMANDMANAGEMENT
	private void manageCommands(int hours, int minutes){
		for(Entry<String, String[]> command : commands.entrySet()){
			handleCommand(command.getValue()[0], command.getValue()[1], command.getValue()[2], hours, minutes);
		}
	}
	
	private void handleCommand(String name, String arguments, String time, int acthours, int actminutes){
		if(time.equals(t(acthours) + ":" + t(actminutes))){
			log.log(Level.INFO, "Executing the following: '" + name + " " + arguments + "'.");
			getServer().dispatchCommand(getServer().getConsoleSender(), name + " " + arguments);
		}
	}
	
	//PLUGINMANAGEMENT
	private void managePlugins(int hours, int minutes){
		for(Entry<String, List<String>> plugin : plugins.entrySet()){
			handlePlugin(formatPluginName(plugin.getKey()), hours, minutes);
		}
	}
	
	private void handlePlugin(String plugin, int hours, int minutes){
		List<String> times = plugins.get(plugin + "." + getTypeOfPlugin(plugin));
		for(String time : times){
			String start = time.split("-")[0];
			String end = time.split("-")[1];
			if(getTypeOfPlugin(plugin) == "enable"){
				if(start.equals(t(hours) + ":" + t(minutes))){
					enablePlugin(plugin);
				}
				if(end.equals(t(hours) + ":" + t(minutes))){
					disablePlugin(plugin);
				}
			}else if(getTypeOfPlugin(plugin) == "disable"){
				if(start.equals(t(hours) + ":" + t(minutes))){
					disablePlugin(plugin);
				}
				if(end.equals(t(hours) + ":" + t(minutes))){
					enablePlugin(plugin);
				}
			}
		}
	}
	
	private String getTypeOfPlugin(String plugin){
		if(plugins.containsKey(plugin + ".enable")){
			return "enable";
		}else if(plugins.containsKey(plugin + ".disable")){
			return "disable";
		}else{
			return null;
		}
	}
	
	private boolean enablePlugin(String name){
		Plugin plugin = pm.getPlugin(name);
		if(plugin == null){
			return false;
		}else{
			if(!pm.isPluginEnabled(plugin)){
				log.log(Level.INFO, "Enabling '" + name + "'.");
				pm.enablePlugin(plugin);
			}
			return true;
		}
	}
	
	private boolean disablePlugin(String name){
		Plugin plugin = pm.getPlugin(name);
		if(plugin == null){
			return false;
		}else{
			if(pm.isPluginEnabled(plugin)){
				log.log(Level.INFO, "Disabling '" + name + "'.");
				pm.disablePlugin(plugin);
			}
			return true;
		}
	}
	
	private void initPlugins(int hours, int minutes){
		for(Entry<String, List<String>> plugin : plugins.entrySet()){
			if(getTypeOfPlugin(formatPluginName(plugin.getKey())) == "enable"){
				boolean disable = true;
				for(String time : plugin.getValue()){
					String start = time.split("-")[0];
					String end = time.split("-")[1];
					if(inTime(hours, minutes, Integer.parseInt(start.split(":")[0]), Integer.parseInt(start.split(":")[1]), Integer.parseInt(end.split(":")[0]), Integer.parseInt(end.split(":")[1]))){
						disable = false;
					}
				}
				if(disable){
					disablePlugin(formatPluginName(plugin.getKey()));
				}
			}else if(getTypeOfPlugin(formatPluginName(plugin.getKey())) == "disable"){
				boolean enable = true;
				for(String time : plugin.getValue()){
					String start = time.split("-")[0];
					String end = time.split("-")[1];
					if(inTime(hours, minutes, Integer.parseInt(start.split(":")[0]), Integer.parseInt(start.split(":")[1]), Integer.parseInt(end.split(":")[0]), Integer.parseInt(end.split(":")[1]))){
						enable = false;
					}
				}
				if(!enable){
					disablePlugin(formatPluginName(plugin.getKey()));
				}
			}
		}
	}
	
	private String formatPluginName(String withType){
		if(withType.endsWith(".enable")){
			return withType.substring(0, withType.length() - 7);
		}else if(withType.endsWith(".disable")){
			return withType.substring(0, withType.length() - 8);
		}else{
			return withType;
		}
	}
	
	//TIME
	private int minutes(){
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.MINUTE);
	}
	
	private int hours(){
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.HOUR_OF_DAY);
	}
	
	private String t(int t){
		if(t < 10){
			return "0" + t;
		}else{
			return "" + t;
		}
	}
	
	private boolean inTime(int curh, int curm, int firh, int firm, int sech, int secm){
		int curt = Integer.valueOf(curh + "" + t(curm));
		int firt = Integer.valueOf(firh + "" + t(firm));
		int sect = Integer.valueOf(sech + "" + t(secm));
		if(firt > sect){ //If first time is later than second time
			if(curt > firt || curt < sect){
				return true;
			}else{
				return false;
			}
		}else if(firt < sect){ //If second time is later than first time
			if(curt > firt && curt < sect){
				return true;
			}else{
				return false;
			}
		}else if(firt == sect){
			if(curt == firt){
				return true;
			}else{
				return false;
			}
		}
		return false;
	}
}
