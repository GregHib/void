package content.social.assist

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.temporaryMoveType
import world.gregs.voidps.network.login.protocol.visual.update.player.MoveType
import world.gregs.voidps.type.Tile

class AssistArea : Script {

    val maximumTileDistance = 20

    init {
        moved(::leavingArea)
    }

    /**
     * Player leaving assistance range
     */
    fun leavingArea(player: Player, from: Tile) {
        if (!player.contains("assistant")) {
            return
        }
        when (player.temporaryMoveType) {
            MoveType.Teleport -> player["assist_point"] = player.tile
            else -> {
                val point: Tile? = player["assist_point"]
                if (point == null || !player.tile.within(point, maximumTileDistance)) {
                    val assistant: Player? = player["assistant"]
                    assistant?.closeMenu()
                }
            }
        }
    }
}
