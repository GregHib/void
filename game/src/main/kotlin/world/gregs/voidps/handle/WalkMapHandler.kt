package world.gregs.voidps.handle

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.engine.client.Sessions
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.network.codec.Handler
import world.gregs.voidps.network.codec.game.encode.message
import world.gregs.voidps.utility.inject

/**
 * @author GregHib <greg@gregs.world>
 * @since April 30, 2020
 */
class WalkMapHandler : Handler() {

    val sessions: Sessions by inject()

    override fun walk(context: ChannelHandlerContext, x: Int, y: Int, running: Boolean) {
        val session = context.channel()
        val player = sessions.get(session) ?: return
        player.walkTo(player.tile.copy(x = x, y = y)) { result ->
            if (result is PathResult.Failure) {
                player.message("You can't reach that.")
                return@walkTo
            }
        }
    }

}