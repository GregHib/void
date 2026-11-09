package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.character.mode.interact.NPCOnObjectInteract
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnObjectInteract
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.network.client.instruction.InteractObject
import world.gregs.voidps.type.Tile

class ObjectOptionHandler : InstructionHandler<InteractObject>() {

    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractObject): Boolean {
        if (player.delayed) {
            return false
        }
        val (objectId, x, y, option) = instruction
        val tile = player.tile.copy(x = x, y = y)
        val target = getObject(tile, objectId)
        if (target == null) {
            logger.warn { "Invalid object $objectId $tile" }
            return false
        }
        val definition = ObjectDefinitions.resolve(target.def, player)
        val options = definition.options
        if (options == null) {
            logger.warn { "Invalid object interaction $target $option ${definition.options.contentToString()}" }
            return false
        }
        val index = option - 1
        val selectedOption = options.getOrNull(index)
        if (selectedOption == null) {
            logger.warn { "Invalid object option $target $index ${options.contentToString()}" }
            return false
        }
        player.closeInterfaces()
        player.interactObject(target, selectedOption)
        return true
    }

    private fun getObject(tile: Tile, objectId: Int): GameObject? {
        val obj = GameObjects.findOrNull(tile, objectId)
        if (obj == null) {
            val definition = ObjectDefinitions.getOrNull(objectId)
            return if (definition == null) {
                GameObjects.findOrNull(tile, objectId.toString())
            } else {
                GameObjects.findOrNull(tile, definition.id)
            }
        }
        return obj
    }
}

fun Player.interactObject(target: GameObject, option: String, approachRange: Int? = null) {
    mode = PlayerOnObjectInteract(target, option, this, approachRange)
}

fun NPC.interactObject(target: GameObject, option: String, approachRange: Int? = null) {
    mode = NPCOnObjectInteract(target, option, this, approachRange)
}
