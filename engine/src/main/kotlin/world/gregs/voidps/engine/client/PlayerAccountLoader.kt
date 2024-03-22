package world.gregs.voidps.engine.client

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt
import world.gregs.voidps.engine.data.PlayerAccounts
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.network.AccountLoader
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.NetworkQueue
import world.gregs.voidps.network.Response
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.encode.login

/**
 * Checks password is valid for a player account before logging in
 */
class PlayerAccountLoader(
    private val queue: NetworkQueue,
    private val accounts: PlayerAccounts,
    private val accountDefinitions: AccountDefinitions,
    private val gameContext: CoroutineDispatcher
) : AccountLoader {
    private val logger = InlineLogger()

    override fun validate(username: String, password: String): Int {
        if (username.length > 12) {
            return Response.LOGIN_SERVER_REJECTED_SESSION
        }
        val names = accountDefinitions.get(username)
        if (names != null && !BCrypt.checkpw(password, names.passwordHash)) {
            return Response.INVALID_CREDENTIALS
        }
        return Response.SUCCESS
    }

    override fun encrypt(username: String, password: String): String {
        val names = accountDefinitions.get(username)
        if (names != null) {
            return names.passwordHash
        }
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    /**
     * @return flow of instructions for the player to be controlled with
     */
    override suspend fun load(client: Client, username: String, passwordHash: String, index: Int, displayMode: Int): MutableSharedFlow<Instruction>? {
        try {
            val saving = accounts.saving(username)
            if (saving) {
                client.disconnect(Response.ACCOUNT_ONLINE)
                return null
            }
            val player = accounts.get(username) ?: accounts.create(username, passwordHash)
            player.index = index
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
        accounts.initPlayer(player)
        withContext(gameContext) {
            queue.await()
            logger.info { "${if (client != null) "Player" else "Bot"} logged in ${player.accountName} index ${player.index}." }
            client?.login(player.name, player.index, player.rights.ordinal, membersWorld = World.members)
            accounts.spawn(player, client, displayMode)
        }
    }
}