/*
 * Copyright HomerBond005
 * 
 *  Published under CC BY-NC-ND 3.0
 *  http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package de.HomerBond005.InTime;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import de.HomerBond005.InTime.Executable.Weekday;
import de.HomerBond005.InTime.ExecutablePlugin.PluginType;

public class InTime extends JavaPlugin{
	private Set<ExecutablePlugin> plugins;
	private Set<ExecutableCommand> commands;
	private int task;
	private Metrics metrics;
	private Updater updater;
	protected static Logger log;
	protected static PluginManager pm;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onEnable(){
		log = getLogger();
		pm = getServer().getPluginManager();
		getConfig().options().copyDefaults(true);
		getConfig().options().copyHeader(true);
		saveConfig();
		reload();
		getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable(){
			public void run(){
				Calendar cal = Calendar.getInstance();
		    	int h = cal.get(Calendar.HOUR_OF_DAY);
		    	int m = cal.get(Calendar.MINUTE);
				initPlugins(h, m);
			}
		});
		task = getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
		    public void run() {
		    	Calendar cal = Calendar.getInstance();
		    	int h = cal.get(Calendar.HOUR_OF_DAY);
		    	int m = cal.get(Calendar.MINUTE);
		    	Weekday wd = Weekday.getByInt(cal.get(Calendar.DAY_OF_WEEK));
		        managePlugins(h, m, wd);
		        manageCommands(h, m, wd);
		    }
		}, 0L, 1200L);
		try{
			metrics = new Metrics(this);
			metrics.start();
		}catch(IOException e){
			log.log(Level.WARNING, "Error while enabling Metrics.");
		}
		updater = new Updater(this, getConfig().getBoolean("updateReminderEnabled", true));
		getServer().getPluginManager().registerEvents(updater, this);
		Calendar cal = Calendar.getInstance();
    	int h = cal.get(Calendar.HOUR_OF_DAY);
    	int m = cal.get(Calendar.MINUTE);
		log.log(Level.INFO, "Current time: " + t(h) + ":" + t(m));
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
		plugins = new HashSet<ExecutablePlugin>();
		for(String plugin : pluginnames){
			Weekday wd = Weekday.getByName(getConfig().getString("plugins."+plugin+".weekday", "all"));
			if(getConfig().getString("plugins." + plugin + ".type").equalsIgnoreCase("enable")){
				plugins.add(new ExecutablePlugin(plugin, new HashSet<String>(getConfig().getStringList("plugins." + plugin + ".times")), wd, PluginType.ENABLE));
			}else if(getConfig().getString("plugins." + plugin + ".type").equals("disable")){
				plugins.add(new ExecutablePlugin(plugin, new HashSet<String>(getConfig().getStringList("plugins." + plugin + ".times")), wd, PluginType.DISABLE));
			}
		}
		//COMMANDS
		commands = new HashSet<ExecutableCommand>();
		Set<String> commandnames = getConfig().getConfigurationSection("commands").getKeys(false);
		for(String command : commandnames){
			Weekday wd = Weekday.getByName(getConfig().getString("commands."+command+".weekday", "all"));
			commands.add(new ExecutableCommand(getConfig().getString("commands." + command + ".command"), getConfig().getString("commands." + command + ".arguments", ""), new HashSet<String>(getConfig().getStringList("commands." + command + ".times")), wd));
		}
	}
	
	//COMMANDMANAGEMENT
	private void manageCommands(int hours, int minutes, Weekday wd){
		for(ExecutableCommand command : commands){
			command.executeIfMatches(hours, minutes, wd, "");
		}
	}
	
	//PLUGINMANAGEMENT
	private void managePlugins(int hours, int minutes, Weekday wd){
		for(ExecutablePlugin plugin : plugins){
			plugin.executeIfMatches(hours, minutes, wd, "");
		}
	}
	
	private void initPlugins(int hours, int minutes){
		for(ExecutablePlugin plugin : plugins){
			if(plugin.getPluginType() == PluginType.ENABLE){
				boolean disable = true;
				for(String time : plugin.times){
					String start = time.split("-")[0];
					String end = time.split("-")[1];
					if(inTime(hours, minutes, Integer.parseInt(start.split(":")[0]), Integer.parseInt(start.split(":")[1]), Integer.parseInt(end.split(":")[0]), Integer.parseInt(end.split(":")[1]))){
						disable = false;
					}
				}
				if(disable){
					plugin.execute("disable");
				}
			}else{
				boolean enable = true;
				for(String time : plugin.times){
					String start = time.split("-")[0];
					String end = time.split("-")[1];
					if(inTime(hours, minutes, Integer.parseInt(start.split(":")[0]), Integer.parseInt(start.split(":")[1]), Integer.parseInt(end.split(":")[0]), Integer.parseInt(end.split(":")[1]))){
						enable = false;
					}
				}
				if(!enable){
					plugin.execute("disable");
				}
			}
		}
	}
	
	//TIME
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
