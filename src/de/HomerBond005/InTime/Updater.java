/*
 * Copyright HomerBond005
 * 
 *  Published under CC BY-NC-ND 3.0
 *  http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package de.HomerBond005.InTime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class Updater implements Listener{
	String opUpdateMsg = "";
	PluginDescriptionFile desc;
	public Updater(JavaPlugin plugin){
		desc = plugin.getDescription();
		try {
			URL connect = new URL("http://homerbond005.bplaced.net/update.php?p="+desc.getName());
			BufferedReader in = new BufferedReader(new InputStreamReader(connect.openStream()));
			String version = in.readLine();
			in.close();
			if(!version.equalsIgnoreCase(desc.getVersion())){
				plugin.getLogger().log(Level.WARNING, "New version of '"+desc.getName()+"' available! Your version: '"+desc.getVersion()+"' New Version: '"+version+"' Please visit bukkit.org to update.");
				opUpdateMsg = version;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerLogin(PlayerJoinEvent event){
		if(event.getPlayer().isOp()&&!opUpdateMsg.equals("")){
			event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE+"New version of '"+desc.getName()+"' is available! Update via http://bukkit.org");
			event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE+"Your version: '"+desc.getVersion()+"' New version: '"+opUpdateMsg+"'");
		}
	}
}