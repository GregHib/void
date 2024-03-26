package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.entity.Despawn
import world.gregs.voidps.engine.entity.MAX_PLAYERS
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

class Players : CharacterList<Player>() {

    override val indexArray: Array<Player?> = arrayOfNulls(MAX_PLAYERS)

    fun get(name: String): Player? = firstOrNull { it.name == name }

    override operator fun get(tile: Tile): List<Player> {
        return filter { it.tile == tile }
    }

    override operator fun get(zone: Zone): List<Player> {
        return filter { it.tile.zone == zone }
    }

    override fun clear() {
        for (player in this) {
            player.emit(Despawn)
        }
        super.clear()
    }
}