package net.quantrax.citybuild.config;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * This is an example of a config, u can also use
 * <pre>
 *     new CustomConfig(directory, file, plugin)
 * </pre>
 */
public class CustomConfig implements Config {

    private final UUID configID;

    public CustomConfig(@Nullable String dir, String file, JavaPlugin plugin) {
        this.configID = this.loadConfig(dir, file, plugin);
    }

    @Override
    public UUID getUUID() {
        return this.configID;
    }
}
