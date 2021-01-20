package world.gregs.void.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import world.gregs.void.engine.client.Sessions
import world.gregs.void.network.codec.Handler
import world.gregs.void.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 05, 2020
 */
class RegionLoadedHandler : Handler() {

    val logger = InlineLogger()
    val sessions: Sessions by inject()

    override fun regionLoaded(context: ChannelHandlerContext) {
        val session = context.channel()
        val player = sessions.get(session) ?: return
        player.viewport.loaded = true
    }

}