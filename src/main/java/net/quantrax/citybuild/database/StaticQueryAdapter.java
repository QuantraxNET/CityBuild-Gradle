package net.quantrax.citybuild.database;

import de.chojo.sadu.base.QueryFactory;
import de.chojo.sadu.wrapper.stage.QueryStage;
import net.quantrax.citybuild.database.exception.AlreadyInitializedException;
import net.quantrax.citybuild.database.exception.NotInitializedException;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.util.logging.Logger;

/**
 * The StaticQueryAdapter class provides a static interface for querying a data source using a fluent builder pattern.
 * It acts as an adapter between the application and the QueryFactory, allowing the application to conveniently construct queries and execute them.
 * <p>
 * It also provides methods for initializing the adapter with a data source and checking if the adapter has been initialized.
 */
public class StaticQueryAdapter {

	private static final Logger LOGGER = Logger.getLogger(StaticQueryAdapter.class.getName());
	private static QueryFactory factory = null;

	/**
	 * Returns a new QueryStage builder.
	 *
	 * @return A QueryStage builder
	 * @throws NotInitializedException if the adapter has not been initialized
	 */
	public static QueryStage<Void> builder() {
		assertInit();
		return factory.builder();
	}


	/**
	 * Returns a new QueryStage builder for the given class.
	 *
	 * @param clazz the class for which the QueryStage builder is created
	 * @param <T> the type of elements in the QueryStage
	 * @return a QueryStage builder
	 * @throws NotInitializedException if the adapter has not been initialized
	 */
	public static <T> QueryStage<T> builder(@NotNull Class<T> clazz) {
		assertInit();
		return factory.builder(clazz);
	}

	public static void start(DataSource dataSource) {
		if (factory != null) throw new AlreadyInitializedException();
		factory = new QueryFactory(dataSource);

		LOGGER.info("Static sadu query adapter started.");
	}

	private static void assertInit() {
		if (factory == null) throw new NotInitializedException();
	}

}
