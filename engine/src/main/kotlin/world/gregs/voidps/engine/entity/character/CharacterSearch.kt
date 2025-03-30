package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

interface CharacterSearch<C : Character> {

    operator fun get(tile: Tile): List<C>

    operator fun get(zone: Zone): List<C>

}