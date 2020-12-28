package rs.dusk.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.core.network.model.session.getSession
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.task.TaskExecutor
import rs.dusk.engine.task.sync
import rs.dusk.network.rs.codec.game.decode.message.ConsoleCommandMessage
import rs.dusk.utility.inject
import rs.dusk.world.command.Command

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 30, 2020
 */
class ConsoleCommandMessageHandler : MessageHandler<ConsoleCommandMessage>() {

    val sessions: Sessions by inject()
    val bus: EventBus by inject()
    val executor: TaskExecutor by inject()

    override fun handle(ctx: ChannelHandlerContext, msg: ConsoleCommandMessage) {
        val session = ctx.channel().getSession()
        val player = sessions.get(session) ?: return
        val (command) = msg
        val parts = command.split(" ")
        val prefix = parts[0]
        executor.sync {
            bus.emit(Command(player, prefix, command.removePrefix("$prefix ")))
        }
    }

}