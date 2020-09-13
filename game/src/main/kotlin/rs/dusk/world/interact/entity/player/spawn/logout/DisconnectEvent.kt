package rs.dusk.world.interact.entity.player.spawn.logout

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.connection.event.ChannelEvent
import rs.dusk.core.network.model.session.getSession
import rs.dusk.engine.client.Sessions
import rs.dusk.utility.inject

class DisconnectEvent : ChannelEvent {

    private val sessions: Sessions by inject()
    private val logoutQueue: LogoutQueue by inject()

    override fun run(ctx: ChannelHandlerContext, cause: Throwable?) {
        val session = ctx.channel().getSession()
        val player = sessions.get(session) ?: return
        logoutQueue.add(player)
    }
}