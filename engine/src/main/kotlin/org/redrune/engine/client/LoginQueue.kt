package org.redrune.engine.client

import org.redrune.core.network.model.session.Session

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
interface LoginQueue {
    suspend fun add(username: String, session: Session? = null): LoginResponse
}