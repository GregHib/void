package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.player.Player

class FinishRegionLoadHandler : InstructionHandler<world.gregs.voidps.network.client.instruct.FinishRegionLoad>() {

    override fun validate(player: Player, instruction: world.gregs.voidps.network.client.instruct.FinishRegionLoad) {
        if (player["debug", false]) {
            println("Finished region load. $player ${player.viewport}")
        }
        player.viewport?.loaded = true
    }

}