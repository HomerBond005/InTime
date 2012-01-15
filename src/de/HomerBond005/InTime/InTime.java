package de.HomerBond005.InTime;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class InTime extends JavaPlugin{
	private Map<String, List<String>> plugins;
	private Map<String, String[]> commands;
	private int[] tasks = new int[2];
	PluginManager pm;
	private File mainDir = new File("plugins/InTime");
	private File configfile = new File(mainDir + File.separator + "config.yml");
	public void onEnable(){
		pm = getServer().getPluginManager();
		mainDir.mkdir();
		if(!configfile.exists()){
			this.getConfig().options().copyDefaults(true);
			this.saveConfig();
		}else{
			this.getConfig().options().copyDefaults(false);
		}
		System.out.println("[InTime] is enabled!");
		System.out.println("[InTime]: Current time: " + t(hours()) + ":" + t(minutes()));
		reload();
		System.out.println();
		getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable(){
			public void run(){
				initPlugins(hours(), minutes());
			}
		});
		tasks[0] = getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
		    public void run() {
		        managePlugins(hours(), minutes());
		    }
		}, 60L, 600L);
		tasks[1] = getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
		    public void run() {
		        manageCommands(hours(), minutes());
		    }
		}, 60L, 1200L);
	}
	public void onDisable(){
		for(int task : tasks){
			getServer().getScheduler().cancelTask(task);
		}
		getServer().getScheduler().cancelTasks(this);
		System.out.println("[InTime] is disabled!");
	}
	@SuppressWarnings("unchecked")
	private void reload(){
		//PLUGINS
		Set<String> pluginnames = this.getConfig().getConfigurationSection("plugins").getKeys(false);
		plugins = new HashMap<String, List<String>>();
		for(String plugin : pluginnames){
			if(this.getConfig().getString("plugins." + plugin + ".type").equals("enable")){
				plugins.put(plugin + ".enable", this.getConfig().getList("plugins." + plugin + ".times"));
			}else if(this.getConfig().getString("plugins." + plugin + ".type").equals("disable")){
				plugins.put(plugin + ".disable", this.getConfig().getList("plugins." + plugin + ".times"));
			}
		}
		//COMMANDS
		commands = new HashMap<String, String[]>();
		Set<String> commandnames = this.getConfig().getConfigurationSection("commands").getKeys(false);
		for(String command : commandnames){
			String[] value = {this.getConfig().getString("commands." + command + ".command"), this.getConfig().getString("commands." + command + ".arguments", ""), this.getConfig().getString("commands." + command + ".time")};
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
			System.out.println("[InTime]: Executing the following: '" + name + " " + arguments + "'.");
			getServer().dispatchCommand(getServer().getConsoleSender(), name + " " + arguments);
		}
	}
	//PLUGINMANAGEMENT
	private String getTypeOfPlugin(String plugin){
		if(plugins.containsKey(plugin + ".enable")){
			return "enable";
		}else if(plugins.containsKey(plugin + ".disable")){
			return "disable";
		}else{
			return null;
		}
	}
	private void managePlugins(int hours, int minutes){
		for(Entry<String, List<String>> plugin : plugins.entrySet()){
			handlePlugin(formatPluginName(plugin.getKey()), hours, minutes);
		}
	}
	private boolean enablePlugin(String name){
		Plugin plugin = pm.getPlugin(name);
		if(plugin == null){
			return false;
		}else{
			if(!pm.isPluginEnabled(plugin)){
				System.out.println("[InTime]: Enabling '" + name + "'.");
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
				System.out.println("[InTime]: Disabling '" + name + "'.");
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
		if(firt > sect){ //If first time is later than seconde time
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
