package org.redrune.engine.client

import com.github.michaelbull.logging.InlineLogger
import org.koin.dsl.module
import org.redrune.core.network.model.session.Session
import org.redrune.engine.entity.factory.PlayerFactory
import org.redrune.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
val clientLoginQueueModule = module {
    single { PlayerLoginQueue() as LoginQueue }
}

class PlayerLoginQueue : LoginQueue {

    private val factory: PlayerFactory by inject()
    private val logger = InlineLogger()

    override suspend fun add(username: String, session: Session?): LoginResponse {
        try {
            val player = factory.spawn(username, session).await() ?: return LoginResponse.Full
            return LoginResponse.Success(player)
        } catch (e: IllegalStateException) {
            logger.error(e) { "Error loading player $username" }
            return LoginResponse.Failure
        }
    }

}
