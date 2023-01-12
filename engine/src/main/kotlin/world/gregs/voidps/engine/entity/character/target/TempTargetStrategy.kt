package world.gregs.voidps.engine.entity.character.target

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.Tile

@Deprecated("Temp to bridge old usages")
data class TempTargetStrategy(
    override val tile: Tile,
    override val size: Size
) : TargetStrategy {
    override val bitMask = 0
    override val rotation = 0
    override val exitStrategy = -1
}