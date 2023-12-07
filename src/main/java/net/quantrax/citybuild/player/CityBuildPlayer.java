package net.quantrax.citybuild.player;

import de.chojo.sadu.wrapper.util.UpdateResult;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static net.quantrax.citybuild.backend.database.StaticQueryAdapter.builder;

public class CityBuildPlayer {

	private final UUID uuid;
	private final int playerId;

	/**
	 * Constructs a new CityBuildPlayer with the given UUID. <br>
	 * Additionally the internal id in the corresponding dataset of the uuid is fetched synchronously for future queries.
	 *
	 * @param uuid the UUID of the player
	 */
	public CityBuildPlayer(@NotNull UUID uuid) {
		this.uuid = uuid;
		this.playerId = this.playerId();
	}

	/**
	 * Creates a new player asynchronously in the database meaning that a new dataset for the player itself and for the amount of coins is inserted into the database. Both tables are in a 1:1 relation to each other.
	 *
	 * @return a CompletableFuture that will be completed with the {@link UpdateResult} indicating the success of the create operation
	 */
	public CompletableFuture<UpdateResult> create() {
		return builder().query("INSERT INTO citybuild.citybuild_player (uuid) VALUE (?);")
				.parameter(stmt -> stmt.setUuidAsString(this.uuid))
				.append()
				.query("INSERT INTO citybuild.player_coins (player_id, value) VALUES (?, ?);")
				.parameter(stmt -> stmt.setInt(this.playerId).setInt(0))
				.insert()
				.send();
	}

	/**
	 * Sets the value of coins asynchronously to the specified value for the player into the database by updating the corresponding dataset of the uuid.
	 *
	 * @param value the value of coins to be set
	 *
	 * @return a CompletableFuture that will be completed with the {@link UpdateResult} indicating the success of the deposit operation
	 */
	public CompletableFuture<UpdateResult> depositCoins(int value) {
		return builder().query("UPDATE citybuild.player_coins SET value=? WHERE player_id=?;")
				.parameter(stmt -> stmt.setInt(value).setInt(this.playerId))
				.update()
				.send();
	}

	/**
	 * Adds the specified value of coins asynchronously to the database for the player.
	 *
	 * @param value the value of coins to be added
	 *
	 * @return a CompletableFuture that will be completed with an Optional containing the new total amount of coins after the addition. If any exception occurs during the operation,
	 * an empty Optional is returned.
	 */
	public CompletableFuture<Optional<Integer>> addCoins(int value) {
		return this.coins()
				.whenComplete((oldValue, $) -> {
					final int newValue = oldValue.orElse(0) + value;

					builder().query("UPDATE citybuild.player_coins SET value=? WHERE player_id=?;")
							.parameter(stmt -> stmt.setInt(newValue).setInt(this.playerId))
							.update()
							.send();
				})
				.exceptionally($ -> Optional.empty());
	}

	/**
	 * Subtracts the given value from the current coins value asynchronously. The new value can not be lower than zero.
	 *
	 * @param value the value to subtract from the coins
	 *
	 * @return a CompletableFuture that will be completed with an Optional containing the new total amount of coins after the subtraction. If any exception occurs during the operation,
	 * an empty Optional is returned.
	 */
	public CompletableFuture<Optional<Integer>> subtractCoins(int value) {
		return this.coins()
				.whenComplete((oldValue, $) -> {
					final int newValue = Math.max(oldValue.orElse(0) - value, 0); // The new value of saved coins cannot be lower than 0

					builder().query("UPDATE citybuild.player_coins SET value=? WHERE player_id=?;")
							.parameter(stmt -> stmt.setInt(newValue).setInt(this.playerId))
							.update()
							.send();
				})
				.exceptionally($ -> Optional.empty());
	}

	/**
	 * Retrieves the amount of coins for the player asynchronously from the database.
	 *
	 * @return a CompletableFuture that will be completed with an Optional containing the amount of coins. If no dataset with the given player_id is present, an empty Optional will be
	 * returned.
	 */
	public CompletableFuture<Optional<Integer>> coins() {
		return builder(Integer.class).query("SELECT value FROM citybuild.player_coins WHERE player_id=?;")
				.parameter(stmt -> stmt.setInt(this.playerId))
				.readRow(row -> row.getInt("value"))
				.first();
	}

	/**
	 * Retrieves the Bukkit Player object corresponding to this CityBuildPlayer.
	 *
	 * @return the Bukkit Player object, or null if the player is not online
	 */
	public @Nullable Player asBukkitPlayer() {
		return Bukkit.getPlayer(this.uuid);
	}


	private int playerId() {
		return builder(Integer.class).query("SELECT id FROM citybuild.citybuild_player WHERE uuid=?;")
				.parameter(stmt -> stmt.setUuidAsString(this.uuid))
				.readRow(row -> row.getInt("id"))
				.firstSync()
				.orElse(-1); // If no dataset with the given uuid is present an error value of -1 is returned instead
	}

}
