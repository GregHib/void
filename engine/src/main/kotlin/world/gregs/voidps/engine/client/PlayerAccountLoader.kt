package world.gregs.voidps.engine.client

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.data.AccountManager
import world.gregs.voidps.engine.data.SaveQueue
import world.gregs.voidps.engine.data.Storage
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.network.Response
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.ConnectionQueue
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.login.AccountLoader
import world.gregs.voidps.network.login.protocol.encode.login

/**
 * Checks password is valid for a player account before logging in
 * Keeps track of the players online, prevents duplicate login attempts
 */
class PlayerAccountLoader(
    private val queue: ConnectionQueue,
    private val storage: Storage,
    private val accounts: AccountManager,
    private val saveQueue: SaveQueue,
    private val accountDefinitions: AccountDefinitions,
    private val gameContext: CoroutineDispatcher,
) : AccountLoader {
    private val logger = InlineLogger()

    var update: Boolean = false

    override fun exists(username: String): Boolean = storage.exists(username)

    override fun password(username: String): String? = accountDefinitions.get(username)?.passwordHash

    /**
     * @return flow of instructions for the player to be controlled with
     */
    override suspend fun load(client: Client, username: String, passwordHash: String, displayMode: Int): SendChannel<Instruction>? {
        try {
            val saving = saveQueue.saving(username)
            if (saving) {
                client.disconnect(Response.ACCOUNT_ONLINE)
                return null
            }
            if (update) {
                client.disconnect(Response.GAME_UPDATE)
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

    suspend fun connect(player: Player, client: Client? = null, displayMode: Int = 0) {
        if (!accounts.setup(player, client, displayMode)) {
            logger.warn { "Error setting up account" }
            client?.disconnect(Response.WORLD_FULL)
            return
        }
        withContext(gameContext) {
            queue.await()
            logger.info { "${if (client != null) "Player" else "Bot"} logged in ${player.accountName} index ${player.index}." }
            client?.login(player.name, player.index, player.rights.ordinal, member = World.members, membersWorld = World.members)
            accounts.spawn(player, client)
            AuditLog.event(player, "connected", player.tile)
        }
    }
}
