package content.area.misthalin.varrock.lumber_yard

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType

class SawmillBrokenFence : Script {
    init {
        objectOperate("Squeeze-under", "fence_varrock_lumbermill") { (target) ->
            message("You squeeze underneath the fence", ChatType.Filter)
            if (tile.x <= target.tile.x) {
                anim("player_sneaks_through_fence")
                exactMoveDelay(target.tile.addX(1))
            } else {
                anim("player_sneaks_through_fence_reversed")
                exactMoveDelay(target.tile)
            }
        }
    }
}
