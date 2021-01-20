package world.gregs.void.handle

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.engine.client.Sessions
import world.gregs.void.engine.task.TaskExecutor
import world.gregs.void.engine.task.sync
import world.gregs.void.network.codec.Handler
import world.gregs.void.utility.inject

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