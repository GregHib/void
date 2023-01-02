package world.gregs.voidps.engine.entity.character.target

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.Overlap
import world.gregs.voidps.engine.map.Tile

object TileTargetStrategy : TargetStrategy<Tile> {
    override fun reached(tile: Tile, size: Size, target: Tile): Boolean {
        return Overlap.isUnder(tile, size, target, Size.ONE)
    }
}