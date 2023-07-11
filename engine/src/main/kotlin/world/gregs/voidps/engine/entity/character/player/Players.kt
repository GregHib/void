package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.entity.MAX_PLAYERS
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.map.zone.Zone
import world.gregs.voidps.type.Tile

class Players : CharacterList<Player>(MAX_PLAYERS) {

    override val indexArray: Array<Player?> = arrayOfNulls(MAX_PLAYERS)

    fun get(name: String): Player? = firstOrNull { it.name == name }

    override operator fun get(tile: Tile): List<Player> {
        return filter { it.tile == tile }
    }

    override operator fun get(zone: Zone): List<Player> {
        return filter { it.tile.zone == zone }
    }
}