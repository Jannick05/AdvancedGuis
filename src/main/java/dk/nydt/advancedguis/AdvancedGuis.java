package dk.nydt.advancedguis;

import dk.nydt.advancedguis.commands.AG;
import dk.nydt.sscore.api.utils.Config;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class AdvancedGuis extends JavaPlugin {
    public static Config config;
    public static FileConfiguration configYML;
    public static AdvancedGuis instance;
    @Override
    public void onEnable() {
        instance = this;

        //CONFIG.YML
        if (!(new File(getDataFolder(), "config.yml")).exists()) saveResource("config.yml", false);

        config = new Config(this, null, "config.yml");
        configYML = config.getConfig();

        //REGISTER COMMANDS
        getCommand("AG").setExecutor(new AG());

    }

    @Override
    public void onDisable() {
        config.saveConfig();
    }

}
