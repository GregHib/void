package world.gregs.voidps.engine.entity.character.target

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.Tile

object DefaultTargetStrategy : TargetStrategy<Any> {
    override fun reached(tile: Tile, size: Size, target: Any): Boolean {
        return false
    }
}