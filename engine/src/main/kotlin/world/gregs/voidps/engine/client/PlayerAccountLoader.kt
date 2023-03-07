package world.gregs.voidps.engine.client

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt
import world.gregs.voidps.engine.data.PlayerFactory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.network.*

/**
 * Checks password is valid for a player account before logging in
 */
class PlayerAccountLoader(
    private val queue: NetworkQueue,
    private val factory: PlayerFactory,
    private val gameContext: CoroutineDispatcher,
    private val collisions: Collisions,
    private val players: Players
) : AccountLoader {
    private val logger = InlineLogger()

    /**
     * @return flow of instructions for the player to be controlled with
     */
    override suspend fun load(client: Client, username: String, password: String, index: Int, displayMode: Int): MutableSharedFlow<Instruction>? {
        try {
            val saving = factory.saving(username)
            if (saving) {
                client.disconnect(Response.ACCOUNT_ONLINE)
                return null
            }
            val player = factory.getOrElse(username, index) { factory.create(username, password) }
            if (validPassword(player, password)) {
                client.disconnect(Response.INVALID_CREDENTIALS)
                return null
            }

            logger.info { "Player $username loaded and queued for login." }
            withContext(gameContext) {
                queue.await()
                logger.info { "Player logged in $username index $index." }
                player.login(client, displayMode)
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