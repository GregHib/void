package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.move.interact
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.npc.NPCClick
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.noInterest
import world.gregs.voidps.engine.entity.character.watch
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.tick.delay
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.instruct.InteractNPC

class NPCOptionHandler : InstructionHandler<InteractNPC>() {

    private val npcs: NPCs by inject()
    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractNPC) {
        val npc = npcs.indexed(instruction.npcIndex) ?: return
        val options = npc.def.options
        val index = instruction.option - 1
        if (index !in options.indices) {
            player.noInterest()
            logger.warn { "Invalid npc option $npc $index" }
            return
        }

        val selectedOption = options[index]
        val click = NPCClick(npc, selectedOption)
        player.events.emit(click)
        if (click.cancelled) {
            return
        }
        player.walkTo(npc, watch = npc, distance = npc.def["interact_distance", 1], cancelAction = true) { path ->
            delay(1) {
                player.watch(null)
                player.face(npc)
            }
            val partial = path.result is PathResult.Partial
            player.interact(NPCOption(npc, selectedOption, partial))
        }
    }

}