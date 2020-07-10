package rs.dusk.engine.client.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.model.session.getSession
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.event.EventBuffer
import rs.dusk.engine.model.entity.index.player.command.Command
import rs.dusk.network.rs.codec.game.GameMessageHandler
import rs.dusk.network.rs.codec.game.decode.message.ConsoleCommandMessage
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 30, 2020
 */
class ConsoleCommandMessageHandler : GameMessageHandler<ConsoleCommandMessage>() {

    val sessions: Sessions by inject()
    val buffer: EventBuffer by inject()

    override fun handle(ctx: ChannelHandlerContext, msg: ConsoleCommandMessage) {
        val session = ctx.channel().getSession()
        val player = sessions.get(session) ?: return
        val (command) = msg
        val parts = command.split(" ")
        val prefix = parts[0]
        buffer.emitLater(Command(player, prefix, command.removePrefix("$prefix ")))
    }

}