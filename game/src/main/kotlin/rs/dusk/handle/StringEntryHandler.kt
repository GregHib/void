package rs.dusk.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.task.TaskExecutor
import rs.dusk.engine.task.sync
import rs.dusk.network.codec.Handler
import rs.dusk.utility.inject
import rs.dusk.world.interact.dialogue.event.StringEntered

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