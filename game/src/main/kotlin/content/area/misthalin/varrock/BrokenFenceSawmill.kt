package content.area.misthalin.varrock

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.type.Tile

private val EAST_TILE = Tile(3296, 3498)
private val WEST_TILE = Tile(3295, 3498)

class BrokenFenceSawmill : Script {
    init {
        objectOperate("Squeeze-under", "fence_varrock_lumbermill") {
            when (tile) {
                EAST_TILE -> {
                    anim("human_squeeze_under", override = true)
                    message("You squeeze underneath the fence", ChatType.Filter)
                    walkOverDelay(WEST_TILE)
                    clearAnim()
                }
                WEST_TILE -> {
                    anim("human_squeeze_under_2", override = true)
                    message("You squeeze underneath the fence", ChatType.Filter)
                    walkOverDelay(EAST_TILE)
                    clearAnim()
                }
                else -> {
                    message("You need to get closer to use that!")
                }
            }
        }
    }
}