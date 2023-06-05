package world.gregs.voidps.engine.entity.character.mode.move.target

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.Tile

data class ObjectTargetStrategy(
    private val obj: GameObject
) : TargetStrategy {
    override val bitMask: Int = obj.def.blockFlag
    override val tile: Tile
        get() = obj.tile
    override val size: Size
        get() = obj.size
    override val width = obj.def.sizeX
    override val height = obj.def.sizeY
    override val rotation: Int = obj.rotation
    override val exitStrategy: Int = obj.type
}