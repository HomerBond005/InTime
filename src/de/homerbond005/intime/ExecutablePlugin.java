package de.homerbond005.intime;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

public class ExecutablePlugin extends Executable {
	private final String plugin;
	private final PluginType pltype;
	private final PluginManager pm;

	public ExecutablePlugin(String plugin, Set<String> times, Weekday weekday,
			PluginType pltype) {
		super(times, weekday);
		this.plugin = plugin;
		pm = Bukkit.getPluginManager();
		this.pltype = pltype;
	}

	public String getPlugin() {
		return plugin;
	}

	@Override
	public void execute(String args) {
		if (args.toLowerCase().startsWith("enable")) {
			if (pm.getPlugin(plugin) != null)
				pm.enablePlugin(pm.getPlugin(plugin));
			else
				InTime.log.warning("Error while enabling plugin! Unable to find the following plugin: " + plugin);
		} else if (args.toLowerCase().startsWith("disable")) {
			if (pm.isPluginEnabled(plugin))
				pm.disablePlugin(pm.getPlugin(plugin));
			else
				InTime.log.warning("Error while disabling plugin! Unable to find the following plugin: " + plugin);
		} else {
			Bukkit.getLogger().warning("Unknown action + \"" + args + "\" for the plugin \"" + plugin + "\"!");
		}
	}

	public enum PluginType {
		ENABLE, DISABLE
	}

	@Override
	public boolean matchesTime(int hours, int minutes, Weekday weekday) {
		if (this.weekday != Weekday.ALL && this.weekday != weekday)
			return false;
		for (String time : times) {
			String start = time.split("-")[0];
			String end = time.split("-")[1];
			if (pltype == PluginType.ENABLE) {
				if (start.equals(t(hours) + ":" + t(minutes))) {
					return true;
				}
				if (end.equals(t(hours) + ":" + t(minutes))) {
					return true;
				}
			} else {
				if (start.equals(t(hours) + ":" + t(minutes))) {
					return true;
				}
				if (end.equals(t(hours) + ":" + t(minutes))) {
					return true;
				}
			}
		}
		return false;
	}

	public PluginType getPluginType() {
		return pltype;
	}

	@Override
	public boolean executeIfMatches(int hours, int minutes, Weekday weekday,
			String args) {
		if (this.weekday != Weekday.ALL && this.weekday != weekday)
			return false;
		for (String time : times) {
			String start = time.split("-")[0];
			String end = time.split("-")[1];
			if (pltype == PluginType.ENABLE) {
				if (start.equals(t(hours) + ":" + t(minutes))) {
					execute("enable");
					return true;
				}
				if (end.equals(t(hours) + ":" + t(minutes))) {
					execute("disable");
					return true;
				}
			} else {
				if (start.equals(t(hours) + ":" + t(minutes))) {
					execute("disable");
					return true;
				}
				if (end.equals(t(hours) + ":" + t(minutes))) {
					execute("enable");
					return true;
				}
			}
		}
		return false;
	}

}
