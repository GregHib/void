import world.gregs.void.engine.action.ActionType
import world.gregs.void.engine.entity.character.*
import world.gregs.void.engine.entity.character.move.PlayerMoved
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.entity.character.player.PlayerMoveType
import world.gregs.void.engine.entity.character.update.visual.player.movementType
import world.gregs.void.engine.event.then
import world.gregs.void.engine.event.where
import world.gregs.void.engine.map.Tile

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