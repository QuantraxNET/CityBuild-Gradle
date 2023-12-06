package net.quantrax.citybuild;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class CityBuildPlugin extends JavaPlugin {

    @Getter
    private static CityBuildPlugin instance;

    @Override
    public void onLoad() {
        CityBuildPlugin.instance = this;
    }

}
