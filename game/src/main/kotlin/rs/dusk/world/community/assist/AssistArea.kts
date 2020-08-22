import rs.dusk.engine.action.ActionType
import rs.dusk.engine.entity.character.*
import rs.dusk.engine.entity.character.move.PlayerMoved
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerMoveType
import rs.dusk.engine.entity.character.update.visual.player.movementType
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.map.Tile

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