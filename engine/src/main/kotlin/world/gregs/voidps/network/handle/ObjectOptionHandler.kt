package world.gregs.voidps.network.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.entity.obj.Stairs
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.utility.inject

/**
 * @author GregHib <greg@gregs.world>
 * @since May 30, 2020
 */
class ObjectOptionHandler : Handler() {

    val logger = InlineLogger()
    val objects: Objects by inject()
    val bus: EventBus by inject()
    val stairs: Stairs by inject()

    override fun objectOption(player: Player, objectId: Int, x: Int, y: Int, run: Boolean, option: Int) {
        val tile = player.tile.copy(x = x, y = y)
        val target = objects[tile, objectId]
        if (target == null) {
            logger.warn { "Invalid object $objectId $tile" }
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
            stairs.option(player, target, selectedOption)
            bus.emit(ObjectOption(player, target, selectedOption, partial))
        }
    }

}