package net.quantrax.citybuild.config;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ConfigHolder {

    private static final List<ConfigHolder> configs = new ArrayList<>();

    private final UUID uuid;

    private YamlConfiguration config;
    private final File file;

    public ConfigHolder(UUID uuid, YamlConfiguration config, File file) {
        this.uuid = uuid;
        this.config = config;
        this.file = file;

        ConfigHolder.configs.add(this);
    }


    public static @NotNull ConfigHolder get(@NotNull UUID uuid) {
        return configs.stream().filter(configHolder -> configHolder.getUuid().equals(uuid)).findFirst().orElseThrow();
    }
}
