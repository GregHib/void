package rs.dusk.engine.client

import kotlinx.coroutines.Deferred
import rs.dusk.core.network.model.session.Session
import rs.dusk.engine.EngineTask
import rs.dusk.engine.EngineTasks

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
abstract class LoginQueue(tasks: EngineTasks, priority: Int) : EngineTask(tasks, priority) {
    abstract fun add(username: String, session: Session? = null): Deferred<LoginResponse>
}