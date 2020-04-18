package rs.dusk.engine.client

import rs.dusk.core.network.model.session.Session

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
interface LoginQueue {
    suspend fun add(username: String, session: Session? = null): LoginResponse
}