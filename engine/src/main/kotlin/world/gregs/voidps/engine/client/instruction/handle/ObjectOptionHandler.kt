package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.definition.Transforms
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.variable.VariableType
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.move.interact
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.DefinitionsDecoder
import world.gregs.voidps.engine.entity.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectClick
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.network.instruct.InteractObject

class ObjectOptionHandler(
    private val objects: Objects,
    private val definitions: ObjectDefinitions
) : InstructionHandler<InteractObject>() {

    private val logger = InlineLogger()

    private fun getObject(tile: Tile, objectId: Int): GameObject? {
        val obj = objects[tile, objectId]
        if (obj == null) {
            val definition = definitions.getOrNull(objectId)
            return if (definition == null) {
                objects[tile, objectId.toString()]
            } else {
                objects[tile, definition.id]
            }
        }
        return obj
    }

    override fun validate(player: Player, instruction: InteractObject) {
        val (objectId, x, y, option) = instruction
        val tile = player.tile.copy(x = x, y = y)
        val target = getObject(tile, objectId)
        if (target == null) {
            logger.warn { "Invalid object $objectId $tile" }
            return
        }
        val definition = getDefinition(player, definitions, target.def, target.def)
        val options = definition.options
        if (options == null) {
            logger.warn { "Invalid object interaction $target $option" }
            return
        }
        val index = option - 1
        if (index !in options.indices) {
            logger.warn { "Invalid object option $target $index" }
            return
        }

        val selectedOption = options[index]
        val click = ObjectClick(target, definition, selectedOption)
        player.events.emit(click)
        if (click.cancelled) {
            return
        }
        player.walkTo(target, distance = target.def["interact_distance", 0], cancelAction = true) { path ->
            player.face(target)
            val partial = path.result is PathResult.Partial
            player.interact(ObjectOption(target, definition, selectedOption, partial))
        }
    }

    companion object {
        fun <T, D : DefinitionsDecoder<T>> getDefinition(player: Player, definitions: D, definition: T, def: Transforms): T {
            val transforms = def.transforms ?: return definition
            val varbit = def.varbit
            if (varbit != -1) {
                val index = player.variables.getIntValue(VariableType.Varbit, varbit) ?: return definition
                return definitions.get(transforms[index])
            }

            val varp = def.varp
            if (varp != -1) {
                val index = player.variables.getIntValue(VariableType.Varp, varp) ?: return definition
                return definitions.get(transforms[index])
            }
            return definition
        }
    }
}