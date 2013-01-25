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
import java.net.URLConnection;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class Updater implements Listener{
	private String opUpdateMsg = "";
	private PluginDescriptionFile desc;
	private boolean enabled;
	
	public Updater(JavaPlugin plugin, boolean enabled){
		this.enabled = enabled;
		desc = plugin.getDescription();
		URL connect = null;
		try{
			if(enabled){
				connect = new URL("http://inceptolabs.hopto.org:23516/update.php?p="+desc.getName()+"&v="+desc.getVersion());
				URLConnection con = connect.openConnection();
				con.setConnectTimeout(3000);
				con.setReadTimeout(4000);
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String version = in.readLine();
				in.close();
				if(!version.equalsIgnoreCase("false")){
					plugin.getLogger().warning("New version of '"+desc.getName()+"' available! Your version: '"+desc.getVersion()+"' New Version: '"+version+"' Please visit dev.bukkit.org to update.");
					opUpdateMsg = version;
				}else
					plugin.getLogger().info("You are running the lastest release: "+desc.getVersion());
			}
		}catch(MalformedURLException e){
			e.printStackTrace();
		}catch(IOException e){
			plugin.getLogger().warning("Failed on connecting to "+connect.getHost());
			plugin.getLogger().info("Either you do not have an internet connection or the update server is down.");
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerLogin(PlayerJoinEvent event){
		if(event.getPlayer().isOp()&&!opUpdateMsg.equals("")&&enabled){
			event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE+"New version of '"+desc.getName()+"' is available! Update via http://dev.bukkit.org");
			event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE+"Your version: '"+desc.getVersion()+"' New version: '"+opUpdateMsg+"'");
		}
	}
}