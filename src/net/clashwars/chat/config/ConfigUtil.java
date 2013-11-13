package net.clashwars.chat.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigUtil {
    private YamlConfiguration cfg;

    public ConfigUtil(YamlConfiguration cfg) {
        this.cfg = cfg;
    }

    public YamlConfiguration getConfig() {
        return cfg;
    }
    
    public static String getString(YamlConfiguration cfg, File file, String path, String def) {
		try {
			if (!cfg.isSet(path)) {
				cfg.set(path, def);
				cfg.save(file);
				return def;
			}
			return cfg.getString(path, def);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static int getInt(YamlConfiguration cfg, File file, String path, int def) {
		try {
			if (!cfg.isSet(path)) {
				cfg.set(path, def);
				cfg.save(file);
				return def;
			}
			return cfg.getInt(path, def);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static float getFloat(YamlConfiguration cfg, File file, String path, float def) {
		try {
			if (!cfg.isSet(path)) {
				cfg.set(path, def);
				cfg.save(file);
				return def;
			}
			return cfg.getLong(path, (long) def);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static boolean getBoolean(YamlConfiguration cfg, File file, String path, boolean def) {
		try {
			if (!cfg.isSet(path)) {
				cfg.set(path, def);
				cfg.save(file);
				return def;
			}
			return cfg.getBoolean(path, def);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
    
    public static List<String> getStringList(YamlConfiguration cfg, File file, String path, String... def) {
		try {
			if (!cfg.isSet(path)) {
				cfg.set(path, (List<String>) new ArrayList<String>(Arrays.asList(def)));
				cfg.save(file);
				return new ArrayList<String>(Arrays.asList(def));
			}
			return cfg.getStringList(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
