package world.gregs.voidps.engine.entity.character.target

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.Tile

interface TargetStrategy<T> {
    fun reached(tile: Tile, size: Size, target: T): Boolean
}