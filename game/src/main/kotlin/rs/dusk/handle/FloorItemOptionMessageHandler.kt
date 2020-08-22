package rs.dusk.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.model.session.getSession
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.entity.character.move.walkTo
import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.entity.character.update.visual.player.face
import rs.dusk.engine.entity.item.FloorItemOption
import rs.dusk.engine.entity.item.FloorItems
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.map.Tile
import rs.dusk.engine.path.PathResult
import rs.dusk.network.rs.codec.game.GameMessageHandler
import rs.dusk.network.rs.codec.game.decode.message.FloorItemOptionMessage
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 30, 2020
 */
class FloorItemOptionMessageHandler : GameMessageHandler<FloorItemOptionMessage>() {

    val logger = InlineLogger()
    val sessions: Sessions by inject()
    val items: FloorItems by inject()
    val bus: EventBus by inject()

    override fun handle(ctx: ChannelHandlerContext, msg: FloorItemOptionMessage) {
        val session = ctx.channel().getSession()
        val player = sessions.get(session) ?: return
        val (id, run, y, x, option) = msg
        val tile = Tile(x, y, player.tile.plane)
        val items = items[tile]
        val item = items.firstOrNull { it.id == id && it.tile == tile } ?: return
        val options = item.def.floorOptions
        val index = option - 1
        if (index !in options.indices) {
            //Invalid option
            return
        }
        val selectedOption = options[index]
        player.walkTo(item) { result ->
            player.face(item)
            if (result is PathResult.Failure) {
                player.message("You can't reach that.")
                return@walkTo
            }
            val partial = result is PathResult.Success.Partial
            bus.emit(FloorItemOption(player, item, selectedOption, partial))
        }
    }

}