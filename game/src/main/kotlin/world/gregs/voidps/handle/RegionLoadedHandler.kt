package world.gregs.voidps.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.engine.client.Sessions
import world.gregs.voidps.network.codec.Handler
import world.gregs.voidps.utility.inject

/**
 * @author GregHib <greg@gregs.world>
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