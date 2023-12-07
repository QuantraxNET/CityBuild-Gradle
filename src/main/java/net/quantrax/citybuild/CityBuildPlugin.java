package net.quantrax.citybuild;

import com.google.common.reflect.ClassPath;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class CityBuildPlugin extends JavaPlugin {

    @Override
    public void onEnable() {

        registerListener();
    }

    @Override
    public void onDisable() {

    }

    public void registerListener() {
        try {
            for (ClassPath.ClassInfo classInfo : ClassPath.from(getClassLoader())
                    .getTopLevelClasses("net.quantrax.citybuild.listener")) {
                Class<?> clazz = Class.forName(classInfo.getName());
                if (Listener.class.isAssignableFrom(clazz))
                    Bukkit.getPluginManager().registerEvents((Listener) clazz.getDeclaredConstructor().newInstance(), this);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
