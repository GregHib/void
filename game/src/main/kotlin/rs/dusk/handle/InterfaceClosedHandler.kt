package rs.dusk.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.task.TaskExecutor
import rs.dusk.engine.task.sync
import rs.dusk.network.codec.Handler
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 26, 2020
 */
class InterfaceClosedHandler : Handler() {

    val sessions: Sessions by inject()
    val executor: TaskExecutor by inject()

    override fun interfaceClosed(context: ChannelHandlerContext) {
        val session = context.channel()
        val player = sessions.get(session) ?: return
        executor.sync {
            val id = player.interfaces.get("main_screen")
            if(id != null) {
                player.interfaces.close(id)
            }
        }
    }

}