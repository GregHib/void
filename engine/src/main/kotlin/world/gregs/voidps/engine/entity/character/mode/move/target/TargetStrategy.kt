package world.gregs.voidps.engine.entity.character.mode.move.target

import org.rsmod.game.pathfinder.reach.ReachStrategy
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.size
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.get
import world.gregs.voidps.type.Tile

interface TargetStrategy {
    val bitMask: Int
    val tile: Tile
    /*
        Rotated values
     */
    val width: Int
    val height: Int
    /*
        Original sizes
     */
    val sizeX: Int
    val sizeY: Int
    val rotation: Int
    val exitStrategy: Int

    fun reached(character: Character): Boolean {
        return ReachStrategy.reached(
            flags = get(),
            srcX = character.tile.x,
            srcZ = character.tile.y,
            level = character.tile.level,
            srcSize = character.size,
            destX = tile.x,
            destZ = tile.y,
            destWidth = sizeX,
            destHeight = sizeY,
            objRot = rotation,
            objShape = exitStrategy,
            blockAccessFlags = bitMask
        )
    }

    companion object {
        operator fun <T : Any> invoke(entity: T): TargetStrategy = when (entity) {
            is Tile -> TileTargetStrategy(entity)
            is GameObject -> when (entity.id) {
                "archery_target" -> TileTargetStrategy(entity.tile.addX(5))
                "gnome_obstacle_pipe_east", "gnome_obstacle_pipe_west" -> TileTargetStrategy(entity.tile.addY(-1))
                "lumbridge_church_bell" -> TileTargetStrategy(entity.tile.addY(-1))
                else -> ObjectTargetStrategy(entity)
            }
            is FloorItem -> FloorItemTargetStrategy(entity)
            is Character -> CharacterTargetStrategy(entity)
            else -> DefaultTargetStrategy
        }
    }
}