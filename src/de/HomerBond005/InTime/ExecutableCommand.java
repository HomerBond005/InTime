package de.HomerBond005.InTime;

import java.util.Set;
import org.bukkit.Bukkit;

public class ExecutableCommand extends Executable {
	private String command;
	private String arguments;
	
	public ExecutableCommand(String command, String arguments, Set<String> times, Weekday weekday) {
		super(times, weekday);
		this.weekday = weekday;
		this.command = command;
		this.arguments = arguments;
	}
	
	public String getCommand() {
		return command;
	}
	
	public String getArguments() {
		return command;
	}
	
	@Override
	public void execute(String args){
		Bukkit.getLogger().info("is executing the command: \""+command+"\"");
		Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command+" "+arguments);
	}

	@Override
	public boolean matchesTime(int hours, int minutes, Weekday weekday){
		if(this.weekday != Weekday.ALL&&this.weekday != weekday)
			return false;
		for(String time : times){
			if(time.equals(t(hours) + ":" + t(minutes))){
				return true;
			}
		}
		return false;
	}

}
