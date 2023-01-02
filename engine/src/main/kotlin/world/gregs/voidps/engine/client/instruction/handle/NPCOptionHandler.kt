package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.instruction.handle.ObjectOptionHandler.Companion.getDefinition
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.move.interact
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.npc.NPCClick
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.noInterest
import world.gregs.voidps.engine.entity.character.watch
import world.gregs.voidps.engine.entity.definition.NPCDefinitions
import world.gregs.voidps.engine.tick.delay
import world.gregs.voidps.network.instruct.InteractNPC

class NPCOptionHandler(
    private val npcs: NPCs,
    private val definitions: NPCDefinitions
) : InstructionHandler<InteractNPC>() {

    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractNPC) {
        val npc = npcs.indexed(instruction.npcIndex) ?: return
        val definition = getDefinition(player, definitions, npc.def, npc.def)
        val options = definition.options
        val index = instruction.option - 1
        if (index !in options.indices) {
            player.noInterest()
            logger.warn { "Invalid npc option $npc $index" }
            return
        }

        val selectedOption = options[index]
        val click = NPCClick(npc, definition, selectedOption)
        player.events.emit(click)
        if (click.cancelled) {
            return
        }
        player.walkTo(npc, watch = npc, distance = npc.def["interact_distance", 1], cancelAction = true) { path ->
            player.delay(1) {
                player.watch(null)
                player.face(npc)
            }
            val partial = path.alternative
            player.interact(NPCOption(npc, definition, selectedOption, partial))
        }
    }

}