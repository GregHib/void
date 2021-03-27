package world.gregs.voidps.engine.client.handle

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.instruct.FinishRegionLoad

class FinishRegionLoadHandler : Handler<FinishRegionLoad>() {

    override fun validate(player: Player, instruction: FinishRegionLoad) {
        player.viewport.loaded = true
    }

}