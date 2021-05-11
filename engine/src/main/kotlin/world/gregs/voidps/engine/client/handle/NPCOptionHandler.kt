package world.gregs.voidps.engine.client.handle

import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.npc.NPCClick
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.character.update.visual.watch
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.network.instruct.InteractNPC
import world.gregs.voidps.utility.inject

class NPCOptionHandler : Handler<InteractNPC>() {

    private val npcs: NPCs by inject()

    override fun validate(player: Player, instruction: InteractNPC) {
        val npc = npcs.getAtIndex(instruction.npcIndex) ?: return
        val options = npc.def.options
        val index = instruction.option - 1
        if (index !in options.indices) {
            //Invalid option
            return
        }

        val selectedOption = options[index]
        val click = NPCClick(npc, selectedOption)
        player.events.emit(click)
        if (click.cancel) {
            return
        }
        player.watch(npc)
        player.face(npc)
        player.walkTo(npc) {
            player.watch(null)
            player.face(npc)
            if (player.movement.result is PathResult.Failure) {
                player.message("You can't reach that.")
                return@walkTo
            }
            val partial = player.movement.result is PathResult.Partial
            player.events.emit(NPCOption(npc, selectedOption, partial))
        }
    }

}