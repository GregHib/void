package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.cantReach
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.npc.NPCClick
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.character.update.visual.watch
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.sync
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.instruct.InteractNPC

class NPCOptionHandler : InstructionHandler<InteractNPC>() {

    private val npcs: NPCs by inject()

    override fun validate(player: Player, instruction: InteractNPC) {
        val npc = npcs.getAtIndex(instruction.npcIndex) ?: return
        val options = npc.def.options
        val index = instruction.option - 1
        if (index !in options.indices) {
            //Invalid option
            return
        }

        sync {
            val selectedOption = options[index]
            val click = NPCClick(npc, selectedOption)
            player.events.emit(click)
            if (click.cancel) {
                return@sync
            }
            player.walkTo(npc, npc) { path ->
                player.watch(null)
                player.face(npc)
                if (player.cantReach(path)) {
                    player.message("You can't reach that.")
                    return@walkTo
                }
                val partial = path.result is PathResult.Partial
                player.events.emit(NPCOption(npc, selectedOption, partial))
            }
        }
    }

}