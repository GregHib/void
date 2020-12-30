package rs.dusk.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.entity.character.move.walkTo
import rs.dusk.engine.entity.character.update.visual.player.face
import rs.dusk.engine.entity.obj.ObjectOption
import rs.dusk.engine.entity.obj.Objects
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.path.PathResult
import rs.dusk.network.codec.Handler
import rs.dusk.network.codec.game.encode.message
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 30, 2020
 */
class ObjectOptionHandler : Handler() {

    val logger = InlineLogger()
    val sessions: Sessions by inject()
    val objects: Objects by inject()
    val bus: EventBus by inject()

    override fun objectOption(context: ChannelHandlerContext, objectId: Int, x: Int, y: Int, run: Boolean, option: Int) {
        val session = context.channel()
        val player = sessions.get(session) ?: return
        val tile = player.tile.copy(x = x, y = y)
        val target = objects[tile, objectId]
        if(target == null) {
            logger.warn { "Invalid object $objectId $x $y" }
            return
        }
        val definition = target.def
        val options = definition.options
        val index = option - 1
        if (index !in options.indices) {
            logger.warn { "Invalid object option $target $index" }
            //Invalid option
            return
        }

        player.face(target)
        val selectedOption = options[index]
        player.walkTo(target) { result ->
            player.face(target)
            if (result is PathResult.Failure) {
                player.message("You can't reach that.")
                return@walkTo
            }
            val partial = result is PathResult.Success.Partial
            bus.emit(ObjectOption(player, target, selectedOption, partial))
        }
    }

}