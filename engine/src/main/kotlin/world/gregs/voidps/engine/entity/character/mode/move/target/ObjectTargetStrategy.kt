package world.gregs.voidps.engine.entity.character.mode.move.target

import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.type.Tile

data class ObjectTargetStrategy(
    private val obj: GameObject
) : TargetStrategy {
    override val bitMask: Int = obj.def.blockFlag
    override val tile: Tile = obj.tile
    override val width: Int = obj.width
    override val height: Int = obj.height
    override val sizeX = obj.def.sizeX
    override val sizeY = obj.def.sizeY
    override val rotation: Int = obj.rotation
    override val shape: Int = obj.shape

    override fun requiresLineOfSight(): Boolean {
        return false
    }
}