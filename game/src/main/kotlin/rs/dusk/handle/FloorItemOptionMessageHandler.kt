package rs.dusk.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.entity.character.move.walkTo
import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.entity.character.update.visual.player.face
import rs.dusk.engine.entity.item.FloorItemOption
import rs.dusk.engine.entity.item.FloorItems
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.map.Tile
import rs.dusk.engine.path.PathResult
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 30, 2020
 */
class FloorItemOptionMessageHandler : MessageHandler() {

    val logger = InlineLogger()
    val sessions: Sessions by inject()
    val items: FloorItems by inject()
    val bus: EventBus by inject()

    override fun floorItemOption(context: ChannelHandlerContext, id: Int, run: Boolean, y: Int, x: Int, option: Int) {
        val session = context.channel()
        val player = sessions.get(session) ?: return
        val tile = Tile(x, y, player.tile.plane)
        val items = items[tile]
        val item = items.firstOrNull { it.id == id && it.tile == tile } ?: return
        val options = item.def.floorOptions
        if (option !in options.indices) {
            //Invalid option
            return
        }
        val selectedOption = options[option]
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