package world.gregs.voidps.engine.entity.character.mode.move.target

import org.rsmod.game.pathfinder.reach.ReachStrategy
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.Tile

interface TargetStrategy {
    val bitMask: Int
    val tile: Tile
    val size: Size
    val rotation: Int
    val exitStrategy: Int

    fun reached(interact: Interact): Boolean {
        return ReachStrategy.reached(
            flags = get(),
            x = interact.character.tile.x,
            y = interact.character.tile.y,
            level = interact.character.tile.plane,
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

    companion object {
        operator fun <T : Any> invoke(entity: T): TargetStrategy = when (entity) {
            is Tile -> TileTargetStrategy(entity)
            is GameObject -> ObjectTargetStrategy(entity)
            is FloorItem -> FloorItemTargetStrategy(entity)
            is Entity -> EntityTargetStrategy(entity)
            else -> DefaultTargetStrategy
        }
    }
}