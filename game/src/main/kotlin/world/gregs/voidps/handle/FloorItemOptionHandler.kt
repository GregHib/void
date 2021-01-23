package world.gregs.voidps.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.engine.client.Sessions
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.item.FloorItemOption
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.network.codec.Handler
import world.gregs.voidps.network.codec.game.encode.message
import world.gregs.voidps.utility.inject

/**
 * @author GregHib <greg@gregs.world>
 * @since May 30, 2020
 */
class FloorItemOptionHandler : Handler() {

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
            val partial = result is PathResult.Partial
            bus.emit(FloorItemOption(player, item, selectedOption, partial))
        }
    }

}