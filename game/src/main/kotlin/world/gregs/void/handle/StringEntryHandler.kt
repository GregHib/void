package world.gregs.void.handle

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.engine.client.Sessions
import world.gregs.void.engine.event.EventBus
import world.gregs.void.engine.task.TaskExecutor
import world.gregs.void.engine.task.sync
import world.gregs.void.network.codec.Handler
import world.gregs.void.utility.inject
import world.gregs.void.world.interact.dialogue.event.StringEntered

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since August 04, 2020
 */
class StringEntryHandler : Handler() {

    val sessions: Sessions by inject()
    val bus: EventBus by inject()
    val executor: TaskExecutor by inject()

    override fun stringEntered(context: ChannelHandlerContext, text: String) {
        val session = context.channel()
        val player = sessions.get(session) ?: return
        executor.sync {
            bus.emit(StringEntered(player, text))
        }
    }

}