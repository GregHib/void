package rs.dusk.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.core.network.model.session.getSession
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.task.TaskExecutor
import rs.dusk.engine.task.sync
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 26, 2020
 */
class InterfaceClosedMessageHandler : MessageHandler() {

    val sessions: Sessions by inject()
    val executor: TaskExecutor by inject()

    override fun interfaceClosed(context: ChannelHandlerContext) {
        val session = context.channel().getSession()
        val player = sessions.get(session) ?: return
        executor.sync {
            val id = player.interfaces.get("main_screen")
            if(id != null) {
                player.interfaces.close(id)
            }
        }
    }

}