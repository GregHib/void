package world.gregs.voidps.engine.entity.character.target

import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.Overlap
import world.gregs.voidps.engine.map.Tile

object EntityTargetStrategy : TargetStrategy<Entity> {
    override fun reached(tile: Tile, size: Size, target: Entity): Boolean {
        return Overlap.isUnder(tile, size, target.tile, target.size)
    }
}