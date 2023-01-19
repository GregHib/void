package world.gregs.voidps.engine.map.collision

import org.rsmod.game.pathfinder.StepValidator
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.map.Tile

fun Character.blocked(direction: Direction) = blocked(tile, direction)

fun Character.blocked(tile: Tile, direction: Direction): Boolean {
    return !world.gregs.voidps.engine.utility.get<StepValidator>().canTravel(tile.x, tile.y, tile.plane, size.width, direction.delta.x, direction.delta.y, 0, collision)
}