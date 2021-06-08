package world.gregs.voidps.engine.client.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.character.move.cantReach
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.obj.ObjectClick
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.sync
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.network.instruct.InteractObject
import world.gregs.voidps.utility.inject

class ObjectOptionHandler : Handler<InteractObject>() {

    private val objects: Objects by inject()
    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractObject) {
        val (objectId, x, y, option) = instruction
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

        sync {

            val selectedOption = options[index]
            val click = ObjectClick(target, selectedOption)
            player.events.emit(click)
            if (click.cancel) {
                return@sync
            }
            player.walkTo(target) {
                player.face(target)
                if (player.cantReach(target)) {
                    player.message("You can't reach that.")
                    return@walkTo
                }
                val partial = player.movement.result is PathResult.Partial
                player.events.emit(ObjectOption(target, selectedOption, partial))
            }
        }
    }

}