package rs.dusk.core.network.connection.event.type

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rs.dusk.core.network.NetworkClient
import rs.dusk.core.network.connection.event.ChannelEvent
import rs.dusk.core.network.connection.event.type.ReestablishmentResponse.FAILURE
import rs.dusk.core.network.connection.event.type.ReestablishmentResponse.SUCCESS

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since May 02, 2020
 */
class ReestablishmentEvent(
	
	/**
     * The client that we wish to reconnect to a server
     */
    private val client: NetworkClient,
	
	/**
     * The maximum amount of re-connection attempts
     */
    private val limit: Int,
	
	/**
     * After a connection has closed, this value is the amount of seconds to wait until attempting to reconnect
     */
    private val delay: Long
) : ChannelEvent {

    private val logger = InlineLogger()

    /**
     * If we are currently attempting to re-establish a connection
     */
    private var running = false

    override fun run(ctx: ChannelHandlerContext, cause: Throwable?) {
        if (!run()) {
            return
        }
        GlobalScope.launch {
            val response = reestablish()
            if (response == FAILURE) {
                client.shutdown()
            }
            running = false
        }
    }

    private fun run(): Boolean {
        if (running) {
            logger.debug { "An attempt to run multiple re-establishment events has been stopped." }
            return false
        }
        running = true
        return true
    }

    private suspend fun reestablish(): ReestablishmentResponse {
        var attempt = 1

        logger.info { "Re-establishment is starting, it will be attempted $limit times at an interval of $delay ms." }
        repeat(limit) {

            logger.debug { "Re-establishment attempt ${attempt}/$limit is running." }

            val reconnected = reconnect()

            if (reconnected) {
                logger.info { "Successfully reconnected to the server after attempt #$attempt." }
                return SUCCESS
            } else {
                if (attempt == limit) {
                    logger.info { "Re-establishment of the connection failed after $limit  attempts, shutting down." }
                } else {
                    logger.debug { "Re-establishment attempt ${attempt}/$limit failed." }
                }
            }

            delay(delay)
            attempt++
        }
        return FAILURE
    }

    /**
     * Handles the reconnection attempt of the [client][NetworkClient] to the [host][rs.dusk.core.network.connection.ConnectionSettings.host]
     */
    private fun reconnect(): Boolean {
        return try {
            client.connect()
            true
        } catch (e: Exception) {
            false
        }
    }

}

/**
 * The results that are possible from a re-establishment attempt
 */
private enum class ReestablishmentResponse {
    SUCCESS, FAILURE
}