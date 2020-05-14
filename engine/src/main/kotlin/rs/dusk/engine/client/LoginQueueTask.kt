package rs.dusk.engine.client

import rs.dusk.engine.EngineTask
import kotlin.coroutines.resume

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
class LoginQueueTask(private val queue: LoginQueue, private val loginPerTickCap: Int) : EngineTask {

    override fun run() {
        var count = 0
        var next = queue.poll()
        while (next != null) {
            next.resume(Unit)
            if (count++ >= loginPerTickCap) {
                break
            }
            next = queue.poll()
        }
    }

}
