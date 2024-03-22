package world.gregs.voidps.engine.client

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt
import world.gregs.voidps.engine.data.PlayerAccounts
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.AccountLoader
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.NetworkQueue
import world.gregs.voidps.network.Response
import world.gregs.voidps.network.client.Client

/**
 * Checks password is valid for a player account before logging in
 */
class PlayerAccountLoader(
    private val queue: NetworkQueue,
    private val accounts: PlayerAccounts,
    private val gameContext: CoroutineDispatcher
) : AccountLoader {
    private val logger = InlineLogger()

    /**
     * @return flow of instructions for the player to be controlled with
     */
    override suspend fun load(client: Client, username: String, password: String, index: Int, displayMode: Int): MutableSharedFlow<Instruction>? {
        try {
            val saving = accounts.saving(username)
            if (saving) {
                client.disconnect(Response.ACCOUNT_ONLINE)
                return null
            }
            val player = accounts.getOrElse(username, index) { accounts.create(username, password) }
            if (validPassword(player, password)) {
                client.disconnect(Response.INVALID_CREDENTIALS)
                return null
            }

            logger.info { "Player $username loaded and queued for login." }
            withContext(gameContext) {
                queue.await()
                logger.info { "Player logged in $username index $index." }
                accounts.login(player, client, displayMode)
            }
            return player.instructions
        } catch (e: IllegalStateException) {
            logger.trace(e) { "Error loading player account" }
            client.disconnect(Response.COULD_NOT_COMPLETE_LOGIN)
            return null
        }
    }

    private fun validPassword(player: Player, password: String) = player.passwordHash.isBlank() || !BCrypt.checkpw(password, player.passwordHash)
}