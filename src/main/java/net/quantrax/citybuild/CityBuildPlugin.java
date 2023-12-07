package net.quantrax.citybuild;

import net.quantrax.citybuild.backend.database.Properties;
import net.quantrax.citybuild.backend.database.StaticSaduLoader;
import org.bukkit.plugin.java.JavaPlugin;

public final class CityBuildPlugin extends JavaPlugin {

	@Override
	public void onLoad() {
		final Properties properties = new Properties("localhost", "3306", "citybuild", "root", ""); // Local test-database with credentials. Has to be reworked when publishing
		StaticSaduLoader.start(properties);
	}

}
