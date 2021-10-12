package world.gregs.voidps.engine.client

import com.github.michaelbull.logging.InlineLogger
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt
import world.gregs.voidps.engine.data.PlayerFactory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.login.LoginQueue
import world.gregs.voidps.network.*
import world.gregs.voidps.network.Network.Companion.finish

@ExperimentalUnsignedTypes
class PlayerAccountLoader(
    private val loginQueue: LoginQueue,
    private val factory: PlayerFactory,
    private val gameContext: CoroutineDispatcher,
    private val loginLimit: Int
) : Network.AccountLoader {

    private val logger = InlineLogger()

    override suspend fun load(write: ByteWriteChannel, client: Client, username: String, password: String, displayMode: Int): MutableSharedFlow<Instruction>? {
        if (loginQueue.isOnline(username)) {
            write.finish(Network.ACCOUNT_ONLINE)
            return null
        }

        if (loginQueue.logins(client.address) >= loginLimit) {
            write.finish(Network.LOGIN_LIMIT_EXCEEDED)
            return null
        }

        val index = loginQueue.login(username, client.address)
        if (index == null) {
            loginQueue.logout(username, client.address, index)
            write.finish(Network.WORLD_FULL)
            return null
        }
        val player = loadPlayer(write, client, username, password, index) ?: return null
        write.sendLoginDetails(username, index, 2)
        withContext(gameContext) {
            player.gameFrame.displayMode = displayMode
            loginQueue.await()
            logger.info { "Player logged in $username index $index." }
            player.login(client)
        }
        return player.instructions
    }

    private suspend fun ByteWriteChannel.sendLoginDetails(username: String, index: Int, rights: Int) {
        writeByte(Network.SUCCESS)
        writeByte(13 + Client.string(username))
        writeByte(rights)
        writeByte(0)// Unknown - something to do with skipping chat messages
        writeByte(0)
        writeByte(0)
        writeByte(0)
        writeByte(0)// Moves chat box position
        writeShort(index)
        writeByte(true)
        writeMedium(0)
        writeByte(true)
        writeString(username)
        flush()
    }

    private suspend fun loadPlayer(write: ByteWriteChannel, client: Client, username: String, password: String, index: Int): Player? {
        try {
            var account = factory.load(username)
            if (account == null) {
                account = factory.create(username, password)
            } else if (account.passwordHash.isBlank() || !BCrypt.checkpw(password, account.passwordHash)) {
                loginQueue.logout(username, client.address, index)
                write.finish(Network.INVALID_CREDENTIALS)
                return null
            }
            factory.initPlayer(account, index)
            logger.info { "Player $username loaded and queued for login." }
            loginQueue.await()
            return account
        } catch (e: IllegalStateException) {
            logger.trace { "Error loading player account ${e.stackTrace.toList()}" }
            e.printStackTrace()
            loginQueue.logout(username, client.address, index)
            write.finish(Network.COULD_NOT_COMPLETE_LOGIN)
            return null
        }
    }
}