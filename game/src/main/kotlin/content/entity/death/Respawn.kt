package content.entity.death

import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile

private fun defaultRespawnTile(): Tile = Tile(
    Settings["world.home.x", 0],
    Settings["world.home.y", 0],
    Settings["world.home.level", 0],
)

fun Player.respawnTile(): Tile = when (val value = get<Any>("respawn_tile")) {
    is Tile -> value
    is Int -> Tile(value)
    else -> defaultRespawnTile()
}

fun Player.setRespawnTile(tile: Tile) {
    if (tile == defaultRespawnTile()) {
        clear("respawn_tile")
    } else {
        set("respawn_tile", tile.id)
    }
}
