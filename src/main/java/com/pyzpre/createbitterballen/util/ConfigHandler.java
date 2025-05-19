package com.pyzpre.createbitterballen.util;

import com.electronwill.nightconfig.core.file.FileConfig;

import java.nio.file.Path;
public class ConfigHandler {

    private static final String CONFIG_FILE_NAME = "create_bitterballen.toml";
    private static final String WARNING_KEY = "general.warning_shown";

    private static FileConfig config;

    public static void loadConfig(Path configPath) {
        Path configFilePath = configPath.resolve(CONFIG_FILE_NAME);

        config = FileConfig.builder(configFilePath).sync().autosave().build();
        config.load();

        if (!config.contains(WARNING_KEY)) {
            config.add(WARNING_KEY, false); // Default value is false
            config.save();
        }
    }

    public static boolean isWarningShown() {
        return config.getOrElse(WARNING_KEY, false);
    }

    public static void setWarningShown(boolean value) {
        config.set(WARNING_KEY, value);
        config.save();
    }

    public static void closeConfig() {
        if (config != null) {
            config.close();
        }
    }
}
