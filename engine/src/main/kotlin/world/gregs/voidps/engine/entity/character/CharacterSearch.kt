package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

interface CharacterSearch<C : Character> {

    fun find(tile: Tile, filter: (C) -> Boolean) = at(tile).first(filter)

    fun findOrNull(tile: Tile, filter: (C) -> Boolean) = at(tile).firstOrNull(filter)

    fun at(tile: Tile): List<C>

    fun find(zone: Zone, filter: (C) -> Boolean) = at(zone).first(filter)

    fun findOrNull(zone: Zone, filter: (C) -> Boolean) = at(zone).firstOrNull(filter)

    fun at(zone: Zone): List<C>
}
