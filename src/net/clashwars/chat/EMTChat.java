package net.clashwars.chat;


import java.util.HashMap;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import net.clashwars.chat.bukkit.ChatPlugin;
import net.clashwars.chat.config.Config;
import net.clashwars.chat.config.PluginConfig;
import net.clashwars.chat.runnables.ResetRunnable;
import net.milkbowl.vault.chat.Chat;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitScheduler;

public class EMTChat {
	private ChatPlugin				chat;
	private Chat					c;
	private final Logger			log				= Logger.getLogger("Minecraft");
	private Config					cfg;
	private String					format;
	private int 					cooldown;
	private int						maxShouts;
	private int 					resetTime;
	private boolean					limitsEnabled;
	private long					lastReset;
	HashMap<String, Long> 			cooldowns 		= new HashMap<String, Long>();
	HashMap<String, Integer> 		shoutAmt 		= new HashMap<String, Integer>();

	public EMTChat(ChatPlugin chat) {
		this.chat = chat;
	}

	public void log(Object msg) {
		log.info("[EMTChat " + getPlugin().getDescription().getVersion() + "]: " + msg.toString());
	}

	public void onDisable() {
		log("Disabled.");
	}

	public void onEnable() {
		
		cfg = new PluginConfig(this);
		cfg.init();
		cfg.load();
		
		setupChat();
		
		if (limitsEnabled) {
			registerTasks();
		}
		
		log("Successfully enabled.");
	}
	
	private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        c = rsp.getProvider();
        return c != null;
    }
	
	private void registerTasks() {
		BukkitScheduler sch = getServer().getScheduler();

		if (getResetTime() > 0) {
			sch.runTaskTimer(getPlugin(), new ResetRunnable(this), 0, getResetTime()*1200);
		} else {
			log("Didn't setup limits because timer can't be less then 1 minute.");
		}
		
	}
	
	
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		if(cmd.getName().equalsIgnoreCase("global")) {
			
			if (!(sender instanceof Player)) {
				sender.sendMessage("This command can only be used by a player.");
				return true;
			}
			Player player = (Player) sender;
			
			if (!(player.hasPermission("chat.shout"))) {
				sender.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Chat" + ChatColor.DARK_GRAY + "] " 
						+ ChatColor.RED + "Insufficient permissions " + ChatColor.GRAY + "-" + ChatColor.DARK_RED + "chat.shout");
				return true;
			}
			
			if (limitsEnabled) {
				if (shoutAmt.containsKey(player.getName())) {
					if (shoutAmt.get(player.getName()) >= maxShouts) {
						sender.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Chat" + ChatColor.DARK_GRAY + "] " 
								+ ChatColor.RED + "You have reached the limit of " + ChatColor.DARK_RED + maxShouts + ChatColor.RED + " shouts!");
						
						int timeLeft = (resetTime * 60) - ((int) (System.currentTimeMillis() - getLastReset())) / 1000;
						sender.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Chat" + ChatColor.DARK_GRAY + "] " 
								+ ChatColor.GRAY + "All shouts will be reset in " + ChatColor.DARK_GRAY + timeLeft + ChatColor.GRAY + " seconds!");
						return true;
					}
				}
			}
			
			
			long cd = cooldown * 1000;
			if (cooldowns.get(player.getName()) == null) {
				cooldowns.put(player.getName(), System.currentTimeMillis() - (cd + 1000));
			}
			
			if (System.currentTimeMillis() - cooldowns.get(player.getName()) > cd) {
				
				if (!(player.hasPermission("chat.bypass"))) {
					cooldowns.put(player.getName(), System.currentTimeMillis());
					if (shoutAmt.containsKey(player.getName())) {
						shoutAmt.put(player.getName(), (shoutAmt.get(player.getName()) + 1));
					} else {
						shoutAmt.put(player.getName(), 1);
					}
					
				}
				
				String msg = "";
				if (args.length >= 1) {
					for (int i = 0; i < args.length; i++) {
						if (i == 0) {
							msg += args[i];
						} else {
							msg += " " + args[i];
						}
					}
					if (!(player.hasPermission("chat.bypass"))) {
						msg = stripColorCodes(msg);
					}
					
					String output = format.replace("<message>", msg)
							.replace("<player>", player.getDisplayName()
							.replace("<prefix>", c.getPlayerPrefix(player)));
					output = output.replace("<prefix> ", "");
					chat.getServer().broadcastMessage(integrateColor(output));
					
				} else {
					sender.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Chat" + ChatColor.DARK_GRAY + "] " 
					+ ChatColor.RED + "Invalid command usage " + ChatColor.GRAY + "-" + ChatColor.DARK_RED + "/shout <message> or /global <message>");
				}
			} else {
				long time = (System.currentTimeMillis() - cooldowns.get(player.getName()));
				time = time / 1000;
				time = cooldown - time;
				sender.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Chat" + ChatColor.DARK_GRAY + "] " 
				+ ChatColor.RED + "You can't shout for another " + ChatColor.DARK_RED + time + ChatColor.RED + " seconds");
			}
			return true;
		}
		else if(cmd.getName().equalsIgnoreCase("chat")) {
			
			if (args.length >= 1) {
				if (args[0].equalsIgnoreCase("reload")) {
					if (sender.hasPermission("chat.reload")) {
						cfg.load();
						sender.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Chat" + ChatColor.DARK_GRAY + "] " 
						+ ChatColor.GREEN + "Configuration reloaded.");
					} else {
						sender.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Chat" + ChatColor.DARK_GRAY + "] " 
								+ ChatColor.RED + "Insufficient permissions " + ChatColor.GRAY + "-" + ChatColor.DARK_RED + "chat.reload");
					}
				} else if (args[0].equalsIgnoreCase("reset")) {
					sender.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Chat" + ChatColor.DARK_GRAY + "] " 
							+ ChatColor.GREEN + "Shout limits have been reset!.");
					resetShouts();
			    	broadcast("&8[&6Chat&8] &aAll shout limits have been force reseted!");
				}
			} else {
				sender.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Chat" + ChatColor.DARK_GRAY + "] " 
				+ ChatColor.RED + "Global chat plugin made by " + ChatColor.DARK_RED + "worstboy32" + ChatColor.RED + " for EMT.");
				sender.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Chat" + ChatColor.DARK_GRAY + "] " 
						+ ChatColor.RED + "Use " + ChatColor.DARK_RED + "/chat reload" + ChatColor.RED + " to reload the configuration.");
				sender.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Chat" + ChatColor.DARK_GRAY + "] " 
						+ ChatColor.RED + "Config: " + ChatColor.DARK_RED + "Cooldown: " + ChatColor.GRAY + this.getCooldown()
						+ ChatColor.DARK_RED + " Max-shouts: " + ChatColor.GRAY + this.getMaxShouts()
						+ ChatColor.DARK_RED + " Limits-reset: " + ChatColor.GRAY + this.getResetTime());
			}
			
			return true;
		}
        return false;
    }
	
	
	/* UTILS */
	public static String stripColorCodes(String str) {
		return Pattern.compile("&([0-9a-fk-orA-FK-OR])").matcher(str).replaceAll("");
	}
	public static String integrateColor(String str) {
		for (ChatColor c : ChatColor.values()) {
			str = str.replaceAll("&" + c.getChar() + "|&" + Character.toUpperCase(c.getChar()), c.toString());
		}
		return str;
	}

	/* GETTERS & SETTERS*/

	public ChatPlugin getPlugin() {
		return chat;
	}

	public Server getServer() {
		return getPlugin().getServer();
	}
	
	public void broadcast (String msg) {
		this.getServer().broadcastMessage(integrateColor(msg));
	}

	public void setFormat(String set) {
		this.format = set;
	}
	
	public String getFormat() {
    	return format;
    }

	public void setCooldown(int set) {
		this.cooldown = set;
	}
	
	public int getCooldown() {
		return cooldown;
	}

	public void setResetTime(int set) {
		this.resetTime = set;
	}
	
	public int getResetTime() {
		return resetTime;
	}
	
	public void setMaxShouts(int set) {
		this.maxShouts = set;
	}
	
	public int getMaxShouts() {
		return maxShouts;
	}
	
	public HashMap<String, Integer> getShoutAmt() {
		return shoutAmt;
	}
	
	public void resetShouts() {
		this.shoutAmt.clear();
	}

	public void setLimitsEnabled(boolean set) {
		this.limitsEnabled = set;
	}
	
	public boolean getLimitsEnabled() {
		return limitsEnabled;
	}
	
	public void setLastReset(long set) {
		this.lastReset = set;
	}
	
	public long getLastReset() {
		return lastReset;
	}
	
}
