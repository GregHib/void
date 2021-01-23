package world.gregs.voidps.handle

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.engine.client.Sessions
import world.gregs.voidps.engine.task.TaskExecutor
import world.gregs.voidps.engine.task.sync
import world.gregs.voidps.network.codec.Handler
import world.gregs.voidps.utility.inject

/**
 * @author GregHib <greg@gregs.world>
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