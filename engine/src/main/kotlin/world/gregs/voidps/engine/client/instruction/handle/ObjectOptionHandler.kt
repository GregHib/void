package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.move.interact
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.ObjectClick
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.instruct.InteractObject

class ObjectOptionHandler : InstructionHandler<InteractObject>() {

    private val objects: Objects by inject()
    private val definitions: ObjectDefinitions by inject()
    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractObject) {
        val (objectId, x, y, option) = instruction
        val tile = player.tile.copy(x = x, y = y)
        val target = objects[tile, objectId] ?: objects[tile, definitions.get(objectId).id]
        if (target == null) {
            logger.warn { "Invalid object $objectId $tile" }
            return
        }
        val definition = target.def
        val options = definition.options
        val index = option - 1
        if (index !in options.indices) {
            logger.warn { "Invalid object option $target $index" }
            return
        }

        val selectedOption = options[index]
        val click = ObjectClick(target, selectedOption)
        player.events.emit(click)
        if (click.cancel) {
            return
        }
        player.walkTo(target) { path ->
            player.face(target)
            val partial = path.result is PathResult.Partial
            player.interact(ObjectOption(target, selectedOption, partial))
        }
    }

}