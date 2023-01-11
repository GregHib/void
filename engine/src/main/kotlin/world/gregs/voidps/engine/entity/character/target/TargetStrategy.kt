package world.gregs.voidps.engine.entity.character.target

import org.rsmod.pathfinder.reach.DefaultReachStrategy
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.mode.Interact
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.utility.get

interface TargetStrategy {
    val bitMask: Int
    val tile: Tile
    val size: Size
    val rotation: Int
    val exitStrategy: Int

    fun reached(interact: Interact): Boolean {
        return DefaultReachStrategy.reached(
            flags = get<Collisions>().data,
            x = interact.character.tile.x,
            y = interact.character.tile.y,
            z = interact.character.tile.plane,
            srcSize = interact.character.size.width,
            destX = tile.x,
            destY = tile.y,
            destWidth = size.width,
            destHeight = size.height,
            rotation = rotation,
            shape = exitStrategy,
            accessBitMask = bitMask
        )
    }
}