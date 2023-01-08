package world.gregs.voidps.engine.entity.character.target

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.Tile

data class TileTargetStrategy(
    override val tile: Tile
) : TargetStrategy {
    override val bitMask = 0
    override val size = Size.ONE
    override val rotation = 0
    override val exitStrategy = -1
}