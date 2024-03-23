package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.definition.Transforms
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.client.instruction.InteractObject
import world.gregs.voidps.type.Tile

class ObjectOptionHandler(
    private val objects: GameObjects,
    private val definitions: ObjectDefinitions
) : InstructionHandler<InteractObject>() {

    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractObject) {
        if (player.hasClock("delay") || player.hasClock("input_delay")) {
            return
        }
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
        val selectedOption = options.getOrNull(index)
        if (selectedOption == null) {
            logger.warn { "Invalid object option $target $index" }
            return
        }
        player.mode = Interact(player, target, ObjectOption(player, target, definition, selectedOption), approachRange = -1)
    }

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

    companion object {
        private fun getVarbitIndex(player: Player, id: Int): Int {
            val definitions: VariableDefinitions = get()
            val key = definitions.getVarbit(id) ?: return 0
            return getInt(definitions, key, player)
        }

        private fun getVarpIndex(player: Player, id: Int): Int {
            val definitions: VariableDefinitions = get()
            val key = definitions.getVarp(id) ?: return 0
            return getInt(definitions, key, player)
        }

        private fun getInt(definitions: VariableDefinitions, key: String, player: Player): Int {
            val variable = definitions.get(key) ?: return 0
            val value = player.variables.get<Any>(key) ?: return 0
            return variable.values.toInt(value)
        }

        fun <T, D : DefinitionsDecoder<T>> getDefinition(player: Player, definitions: D, definition: T, def: Transforms): T {
            val transforms = def.transforms ?: return definition
            val varbit = def.varbit
            if (varbit != -1) {
                val index = getVarbitIndex(player, varbit)
                return definitions.get(transforms[index])
            }

            val varp = def.varp
            if (varp != -1) {
                val index = this.getVarpIndex(player, varp)
                return definitions.get(transforms[index])
            }
            return definition
        }
    }
}