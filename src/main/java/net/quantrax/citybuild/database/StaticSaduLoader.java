package net.quantrax.citybuild.database;

import com.zaxxer.hikari.HikariDataSource;
import de.chojo.sadu.databases.MariaDb;
import de.chojo.sadu.datasource.DataSourceCreator;

public final class StaticSaduLoader {

	public static void start(Properties properties) {
		final HikariDataSource dataSource = createSource(properties);

	}

	private static HikariDataSource createSource(Properties properties) {
		return DataSourceCreator.create(MariaDb.get())
				.configure(config -> config.host(properties.host()).port(properties.port()).database(properties.database()).user(properties.username()).password(properties.password()))
				.create()
				.withMinimumIdle(2) // At least two pending connections have to be active in case one fails and shutdowns
				.withMaximumPoolSize(4) // 4 existing connections is the maximum allowed in case both idling connections are failing
				.build();
	}

}
