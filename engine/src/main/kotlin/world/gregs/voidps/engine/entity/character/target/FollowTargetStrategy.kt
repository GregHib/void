package world.gregs.voidps.engine.entity.character.target

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.map.Tile

object FollowTargetStrategy : TargetStrategy<Character> {
    override fun reached(tile: Tile, size: Size, target: Character): Boolean {
        return tile == target.movement.previousTile
    }
}