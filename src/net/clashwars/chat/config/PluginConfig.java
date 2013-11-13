package net.clashwars.chat.config;

import java.io.File;

import net.clashwars.chat.EMTChat;

import org.bukkit.configuration.file.YamlConfiguration;

public class PluginConfig extends Config {
	private EMTChat chat;
	private YamlConfiguration cfg;
	private final File			dir		= new File("plugins/Chat/");
	private final File			file	= new File(dir + "/Chat.yml");
	
	
	public PluginConfig (EMTChat chat) {
		this.chat = chat;
	}

	@Override
	public void init() {
		try {
			dir.mkdirs();
			file.createNewFile();
			cfg = new YamlConfiguration();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void load() {
		try {
			cfg.load(file);
			
			chat.setFormat(ConfigUtil.getString(cfg, file, "chat.format", "&6[&4global&6] &r<prefix> &9<player>&7: &c<message>"));
			chat.setCooldown(ConfigUtil.getInt(cfg, file, "chat.cooldown", 15));
			chat.setLimitsEnabled(ConfigUtil.getBoolean(cfg, file, "limit.enabled", true));
			chat.setResetTime(ConfigUtil.getInt(cfg, file, "limit.reset-timer", 5));
			chat.setMaxShouts(ConfigUtil.getInt(cfg, file, "limit.max-shouts", 5));
			
            cfg.save(file);
            
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
    public void save() {
            try {
                    
                    cfg.save(file);
            } catch (Exception e) {
                    e.printStackTrace();
            }
    }
}
