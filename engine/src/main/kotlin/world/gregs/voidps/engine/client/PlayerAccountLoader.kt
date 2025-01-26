package world.gregs.voidps.engine.client

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.data.AccountManager
import world.gregs.voidps.engine.data.AccountStorage
import world.gregs.voidps.engine.data.SaveQueue
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.network.Response
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.ConnectionQueue
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.login.AccountLoader
import world.gregs.voidps.network.login.protocol.encode.login

/**
 * Handles the loading of player accounts from storage, validation of login credentials,
 * and managing of player login processes.
 *
 * This class integrates with various system components such as the connection queue,
 * account storage, account manager, save queue, and account definitions to facilitate
 * account-related operations.
 *
 * @property queue The connection queue used to manage player login and logout operations.
 * @property storage The account storage system used to persist and retrieve player accounts.
 * @property accounts The account manager responsible for creating, setting up, and spawning player accounts.
 * @property saveQueue The save queue that handles saving of player accounts asynchronously.
 * @property accountDefinitions Definitions of player accounts, including metadata such as password hashes.
 * @property gameContext The coroutine dispatcher used for running game-related tasks.
 */
class PlayerAccountLoader(
    private val queue: ConnectionQueue,
    private val storage: AccountStorage,
    private val accounts: AccountManager,
    private val saveQueue: SaveQueue,
    private val accountDefinitions: AccountDefinitions,
    private val gameContext: CoroutineDispatcher
) : AccountLoader {
    /**
     * Logger instance utilized for logging messages and events within the PlayerAccountLoader class.
     * It provides a streamlined mechanism for tracking the execution flow and debugging processes.
     */
    private val logger = InlineLogger()

    /**
     * Determines whether an account with the given username exists in the storage.
     *
     * @param username The username of the account to check for existence.
     * @return True if the account exists, false otherwise.
     */
    override fun exists(username: String): Boolean {
        return storage.exists(username)
    }

    /**
     * Retrieves the password hash associated with the given username.
     *
     * @param username The username for which the password hash is being requested.
     * @return The password hash of the specified username, or null if no entry exists for the username.
     */
    override fun password(username: String): String? {
        return accountDefinitions.get(username)?.passwordHash
    }

    /**
     * Loads a player's account and connects them to the game.
     *
     * @param client The client instance representing the connection to the player.
     * @param username The username of the account to be loaded.
     * @param passwordHash The hashed password of the account for verification.
     * @param displayMode The display mode selected by the client (e.g., fullscreen or windowed).
     * @return A SendChannel of instructions for the loaded player, or null if the operation fails.
     */
    override suspend fun load(client: Client, username: String, passwordHash: String, displayMode: Int): SendChannel<Instruction>? {
        try {
            val saving = saveQueue.saving(username)
            if (saving) {
                client.disconnect(Response.ACCOUNT_ONLINE)
                return null
            }
            val player = storage.load(username)?.toPlayer() ?: accounts.create(username, passwordHash)
            logger.info { "Player $username loaded and queued for login." }
            connect(player, client, displayMode)
            return player.instructions
        } catch (e: IllegalStateException) {
            logger.trace(e) { "Error loading player account" }
            client.disconnect(Response.COULD_NOT_COMPLETE_LOGIN)
            return null
        }
    }

    /**
     * Connects a player to the game server.
     *
     * @param player The player to be connected. Represents a client-controlled or bot-controlled player.
     * @param client The client associated with the player. Can be null if the player is a bot.
     * @param displayMode The display mode to use when connecting the player.
     */
    suspend fun connect(player: Player, client: Client? = null, displayMode: Int = 0) {
        if (!accounts.setup(player)) {
            logger.warn { "Error setting up account" }
            client?.disconnect(Response.WORLD_FULL)
            return
        }
        withContext(gameContext) {
            queue.await()
            logger.info { "${if (client != null) "Player" else "Bot"} logged in ${player.accountName} index ${player.index}." }
            client?.login(player.name, player.index, player.rights.ordinal, membersWorld = World.members)
            accounts.spawn(player, client, displayMode)
        }
    }
}