package world.gregs.voidps.engine.entity.character.mode.move.target

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.move.followTile
import world.gregs.voidps.engine.map.Tile

data class FollowTargetStrategy(
    private val character: Character
) : TargetStrategy {
    override val bitMask = 0
    override val tile: Tile
        get() = character.followTile
    override val size: Size
        get() = character.size
    override val rotation = 0
    override val exitStrategy = -1
    override val width: Int = size.width
    override val height: Int = size.height
}