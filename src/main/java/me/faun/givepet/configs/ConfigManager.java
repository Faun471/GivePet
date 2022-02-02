package me.faun.givepet.configs;

import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.SettingsManagerBuilder;
import ch.jalu.configme.properties.Property;
import me.faun.givepet.GivePet;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;

public class ConfigManager {
    private final HashMap<String, SettingsManager> configs;

    public ConfigManager() {
        this.configs = new HashMap<>();
    }

    public void reloadConfigs() {
        configs.clear();
        loadConfig("messages");
        loadConfig("config");
    }

    public SettingsManager getConfig(String name) {
        if (!configs.containsKey(name)) {
            loadConfig(name);
        }

        return configs.get(name);
    }

    public void loadConfig(String name) {
        String fileName = name + ".yml";
        File file = new File(GivePet.getInstance().getDataFolder(), fileName);

        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }

        SettingsManager settingsManager = initSettings(name, file);
        configs.put(name, settingsManager);
    }

    public SettingsManager initSettings(String name, File config) {
        Class<? extends SettingsHolder> clazz = switch (name) {
                case "messages" -> Messages.class;
                case "config" -> Config.class;
                default -> null;
        };

        Path configFile = Path.of(config.getPath());
        return SettingsManagerBuilder
                .withYamlFile(configFile)
                .configurationData(clazz)
                .useDefaultMigrationService()
                .create();
    }

    public Object getConfigValue(String config, Property<?> value) {
        SettingsManager settingsManager = getConfig(config);
        return settingsManager.getProperty(value);
    }
}
