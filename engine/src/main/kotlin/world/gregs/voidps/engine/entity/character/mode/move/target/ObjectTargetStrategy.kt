package world.gregs.voidps.engine.entity.character.mode.move.target

import org.rsmod.game.pathfinder.LineValidator
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.move.hasLineOfSight
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.type.Tile

/**
 * @param lineOfSight for objects which are shot at from a distance and can't be seen through walls
 */
data class ObjectTargetStrategy(
    private val obj: GameObject,
    private val lineOfSight: Boolean = false,
) : TargetStrategy {
    override val bitMask: Int = obj.def.blockFlag
    override val tile: Tile = obj.tile
    override val width: Int = obj.width
    override val height: Int = obj.height
    override val sizeX = obj.def.sizeX
    override val sizeY = obj.def.sizeY
    override val rotation: Int = obj.rotation
    override val shape: Int = obj.shape

    override fun requiresLineOfSight(): Boolean = lineOfSight

    // The object itself can block projectiles so line of sight is checked up to the tiles surrounding it instead
    override fun hasLineOfSight(validator: LineValidator, character: Character): Boolean = validator.hasLineOfSight(character, Tile(tile.x - 1, tile.y - 1, tile.level), width + 2, height + 2)
}
