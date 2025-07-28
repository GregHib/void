package world.gregs.voidps.engine.map.collision

import org.rsmod.game.pathfinder.StepValidator
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.get
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

fun Character.blocked(direction: Direction) = blocked(tile, direction)

fun Character.blocked(tile: Tile, direction: Direction): Boolean = !get<StepValidator>().canTravel(
    x = tile.x,
    z = tile.y,
    level = tile.level,
    size = size,
    offsetX = direction.delta.x,
    offsetZ = direction.delta.y,
    extraFlag = blockMove,
    collision = collision,
)
