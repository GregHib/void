import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.entity.character.*
import world.gregs.voidps.engine.entity.character.move.PlayerMoved
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerMoveType
import world.gregs.voidps.engine.entity.character.update.visual.player.movementType
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.engine.map.Tile

/**
 * Player leaving assistance range
 */

val maximumTileDistance = 20

PlayerMoved where { player.has("assistant") } then {
    when (player.movementType) {
        PlayerMoveType.Teleport -> player["assist_point"] = player.tile
        else -> {
            val point: Tile? = player.getOrNull("assist_point")
            if (point == null || !player.tile.within(point, maximumTileDistance)) {
                val assistant: Player? = player.getOrNull("assistant")
                assistant?.action?.cancel(ActionType.Assisting)
            }
        }
    }
}