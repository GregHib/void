package rs.dusk.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.model.session.getSession
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.task.TaskExecutor
import rs.dusk.engine.task.start
import rs.dusk.network.rs.codec.game.GameMessageHandler
import rs.dusk.network.rs.codec.game.decode.message.StringEntryMessage
import rs.dusk.utility.inject
import rs.dusk.world.interact.dialogue.event.StringEntered

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since August 04, 2020
 */
class StringEntryMessageHandler : GameMessageHandler<StringEntryMessage>() {

    val sessions: Sessions by inject()
    val bus: EventBus by inject()
    val executor: TaskExecutor by inject()

    override fun handle(ctx: ChannelHandlerContext, msg: StringEntryMessage) {
        val session = ctx.channel().getSession()
        val player = sessions.get(session) ?: return
        executor.start {
            bus.emit(StringEntered(player, msg.text))
        }
    }

}