package world.gregs.voidps.engine.entity.character.mode.move.target

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.Tile

data class ObjectTargetStrategy(
    private val obj: GameObject
) : TargetStrategy {
    override val bitMask = ((obj.def.blockFlag shl obj.rotation) and 0xf) or (obj.def.blockFlag shr (4 - obj.rotation))
    override val tile: Tile
        get() = obj.tile
    override val size: Size
        get() = obj.size
    override val rotation: Int
        get() = obj.rotation
    override val exitStrategy: Int
        get() = obj.type
}