package world.gregs.voidps.engine.entity.character.mode.move.target

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.Tile

object DefaultTargetStrategy : TargetStrategy {
    override val bitMask = 0
    override val tile = Tile.EMPTY
    override val size = Size.ONE
    override val rotation = 0
    override val exitStrategy = -1
    override val width: Int = 1
    override val height: Int = 1
}