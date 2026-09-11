package content.area.misthalin.varrock

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.type.Tile

private val EAST_TILE = Tile(3296, 3498)
private val WEST_TILE = Tile(3295, 3498)

class BrokenFenceSawmill : Script {
    init {
        objectOperate("Squeeze-under", "fence_varrock_lumbermill") {
            when (tile) {
                EAST_TILE -> {
                    message("You squeeze underneath the fence", ChatType.Filter)
                    walkOverDelay(WEST_TILE)
                }
                WEST_TILE -> {
                    message("You squeeze underneath the fence", ChatType.Filter)
                    walkOverDelay(EAST_TILE)
                }
                else -> {
                    message("You need to get closer to use that!")
                }
            }
        }
    }
}