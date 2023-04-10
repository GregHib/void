package world.gregs.voidps.engine.entity.character.mode.move.target

import org.rsmod.game.pathfinder.reach.ReachStrategy
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.Character
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

    fun reached(character: Character): Boolean {
        return ReachStrategy.reached(
            flags = get(),
            x = character.tile.x,
            y = character.tile.y,
            level = character.tile.plane,
            srcSize = character.size.width,
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
            is GameObject -> if (entity.id == "archery_target") TileTargetStrategy(entity.tile.addX(5)) else ObjectTargetStrategy(entity)
            is FloorItem -> FloorItemTargetStrategy(entity)
            is Entity -> EntityTargetStrategy(entity)
            else -> DefaultTargetStrategy
        }
    }
}