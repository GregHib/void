package world.gregs.voidps.engine.entity.character.mode.move.target

import world.gregs.voidps.engine.entity.character.mode.move.step
import world.gregs.voidps.type.Tile

data class TileTargetStrategy(
    override val tile: Tile
) : TargetStrategy {
    override val bitMask = 0
    override val width: Int = 1
    override val height: Int = 1
    override val rotation = 0
    override val shape = -1
    override val sizeX: Int = 1
    override val sizeY: Int = 1

    companion object {
        operator fun invoke(tile: Tile, noCollision: Boolean, forceWalk: Boolean = false): TileTargetStrategy {
            return TileTargetStrategy(tile.step(noCollision, forceWalk))
        }
    }
}