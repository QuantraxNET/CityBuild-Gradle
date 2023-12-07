package net.quantrax.citybuild;

import com.google.common.collect.UnmodifiableIterator;
import com.google.common.reflect.ClassPath;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
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
            for (UnmodifiableIterator<ClassPath.ClassInfo> unmodifiableIterator =
                 ClassPath.from(getClassLoader())
                         .getTopLevelClasses("net.quantrax.citybuild.listener")
                         .iterator();
                 unmodifiableIterator.hasNext(); ) {
                    ClassPath.ClassInfo classInfo = unmodifiableIterator.next();
                    Class<?> clazz = Class.forName(classInfo.getName());
                    if (Listener.class.isAssignableFrom(clazz))
                        Bukkit.getPluginManager().registerEvents((Listener) clazz.newInstance(), (Plugin) this);
                }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
