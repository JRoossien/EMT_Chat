package net.clashwars.chat.bukkit;


import net.clashwars.chat.EMTChat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import org.bukkit.plugin.java.JavaPlugin;


public class ChatPlugin extends JavaPlugin {
	private EMTChat	chat;

	@Override
	public void onDisable() {
		chat.onDisable();
	}

	@Override
	public void onEnable() {
		chat = new EMTChat(this);
		chat.onEnable();
	}
	
	@Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
        return chat.onCommand(sender, cmd, lbl, args);
    }
}
