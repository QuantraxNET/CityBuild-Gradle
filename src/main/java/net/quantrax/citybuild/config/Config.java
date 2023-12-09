package net.quantrax.citybuild.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * <br>
 * <p>
 * You can implement this class and treat it like a config built in your class
 * useful for quick configs
 * <br>
 * <br>
 * Example Usage or  {@link CustomConfig}
 * <br>
 * <br>
 * <pre>
 *     public class ExampleConfig implements Config {
 *
 *         private final UUID configID;
 *
 *         public ExampleConfig() {
 *             this.configID = this.loadConfig(dir, fileName, plugin);
 *         }
 *
 *         public void sendMessage(Player p) {
 *             this.getMessage(...)...
 *         }
 *
 *         public void getUUID() {
 *             return this.configID;
 *         }
 *
 *     }
 * </pre>
 */
public interface Config {


    List<Config> configs = new ArrayList<>();

    /**
     * This is basically a static method and reloads <u>all</u> of the configs
     */
    default void reloadAllConfig() {
        configs.forEach(Config::reloadConfig);
    }



    default UUID loadConfig(@Nullable String dir, @NotNull String fileName, @NotNull JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder() + "/" + dir, fileName);

        if (!file.exists()) {
            new File(plugin.getDataFolder() + (dir == null ? "" : "/" + dir)).mkdirs();

            plugin.saveResource((dir == null ? "" : dir + "/") + fileName, false);
        }

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        configs.add(this);

        UUID uuid = UUID.randomUUID();
        new ConfigHolder(uuid, config, file);

        return uuid;
    }

    default Object get(String key) {
        return getConfig().get(key) == null ? key : getConfig().get(key);
    }

    default List<Integer> getIntList(String key) {
        return getConfig().getIntegerList(key);
    }

    /**
     * Gets a message as a <a href="https://docs.advntr.dev/minimessage/format.html">minimessage</a component
     *
     * <br>
     *
     * possible config:
     * <pre>
     * test:
     *   message: "<yellow>Hello <0>, this is a message"
     * </pre>
     *
     * possible code:
     *
     * <pre>
     * public void sendMessage(Player player) {
     *   player.sendMessage(config.getMessage("test.message", player.getName());
     * }
     * </pre>
     * @param key the config key
     * @param args the args
     * @return the new component
     */
    default Component getMessage(String key, Object... args) {
        if (get(key) == null) return Component.text("NOT FOUND");
        String message = get(key).toString();
        List<TagResolver> placeholders = new ArrayList<>();

        placeholders.add(Placeholder.parsed("prefix", "prefix"));

        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Component component) {
                args[i] = PlainTextComponentSerializer.plainText().serialize(component);
            }
            placeholders.add(Placeholder.parsed(String.valueOf(i), args[i] == null ? "null" : args[i].toString()));
        }

        return MiniMessage.miniMessage().deserialize(message, placeholders.toArray(new TagResolver[0]));
    }

    default Sound getSound(String key) {
        try {
            return Sound.valueOf(get(key).toString());
        } catch (Exception e) {
            return Sound.ENTITY_ENDER_DRAGON_GROWL;
        }
    }

    default List<String> getStringList(String key) {
        return getConfig().getStringList(key);
    }


    default List<Component> getList(String key, Object... args) {
        List<String> defaultList = getConfig().getStringList(key);

        List<TagResolver> placeholders = new ArrayList<>();

        placeholders.add(Placeholder.parsed("prefix", "prefix"));

        for (int i = 0; i < args.length; i++) {
            placeholders.add(Placeholder.parsed(String.valueOf(i), args[i] == null ? "null" : args[i].toString()));
        }

        return defaultList.stream().map(s -> MiniMessage.miniMessage().deserialize(s, placeholders.toArray(new TagResolver[0]))).toList();
    }

    default boolean getBool(String key) {
        return getConfig().getBoolean(key);
    }

    default int getInt(String key) {
        return getConfig().getInt(key);
    }

    default double getDouble(String key) {
        return getConfig().getDouble(key);
    }

    default long getLong(String key) {
        return getConfig().getLong(key);
    }

    default Material getMaterial(String key) {
        try {
            return Material.valueOf(get(key).toString());
        } catch (Exception e) {
            return Material.DIRT;
        }
    }

    default void set(String key, Object value) {
        getConfig().set(key, value);
        save();
    }

    default void save() {
        try {
            getConfig().save(getHolder().getFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    default YamlConfiguration getConfig() {
        return ConfigHolder.get(getUUID()).getConfig();
    }

    default void reloadConfig() {
        getHolder().setConfig(YamlConfiguration.loadConfiguration(getHolder().getFile()));
    }



    default ConfigHolder getHolder() {
        return ConfigHolder.get(getUUID());
    }

    UUID getUUID();
}
