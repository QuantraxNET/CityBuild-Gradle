package net.quantrax.citybuild;

import de.derioo.inventoryframework.objects.InventoryFramework;
import de.derioo.manager.CommandFramework;
import lombok.Getter;
import net.quantrax.citybuild.support.SupportManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CityBuildPlugin extends JavaPlugin {

    @Getter
    private static CityBuildPlugin instance;

    @Override
    public void onLoad() {
        CityBuildPlugin.instance = this;
    }

    @Override
    public void onEnable() {
        new CommandFramework(this);
        new InventoryFramework(this);

        new SupportManager(this);
    }
}
