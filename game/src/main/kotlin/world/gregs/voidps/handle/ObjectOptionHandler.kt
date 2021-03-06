package world.gregs.voidps.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.engine.client.Sessions
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.network.codec.Handler
import world.gregs.voidps.network.codec.game.encode.message
import world.gregs.voidps.utility.inject

/**
 * @author GregHib <greg@gregs.world>
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
            val partial = result is PathResult.Partial
            bus.emit(ObjectOption(player, target, selectedOption, partial))
        }
    }

}