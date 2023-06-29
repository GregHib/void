package world.gregs.voidps.engine.entity.obj

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.data.definition.extra.ObjectDefinitions
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.Tile

/**
 * Interactive Object
 */
@JvmInline
value class GameObject(internal val packed: Long) : Entity {

    constructor(id: Int, x: Int, y: Int, plane: Int, shape: Int, rotation: Int) : this(pack(id, x, y, plane, shape, rotation))

    val id: String
        get() = def.stringId
    override var tile: Tile
        get() = Tile(x, y, plane)
        set(value) {}
    val width: Int
        get() = if (rotation and 0x1 == 1) def.sizeY else def.sizeX
    val height: Int
        get() = if (rotation and 0x1 == 1) def.sizeX else def.sizeY
    val def: ObjectDefinition
        get() = get<ObjectDefinitions>().get(intId)

    val intId: Int
        get() = id(packed)
    val x: Int
        get() = x(packed)
    val y: Int
        get() = y(packed)
    val plane: Int
        get() = plane(packed)
    val shape: Int
        get() = shape(packed)
    val rotation: Int
        get() = rotation(packed)

    override fun toString(): String {
        return "GameObject(id=$intId, tile=$tile, shape=$shape, rotation=$rotation)"
    }

    companion object {
        operator fun invoke(id: Int, tile: Tile, shape: Int, rotation: Int): GameObject {
            return GameObject(id, tile.x, tile.y, tile.plane, shape, rotation)
        }

        internal fun pack(id: Int, x: Int, y: Int, plane: Int, shape: Int, rotation: Int): Long {
            return pack(id.toLong(), x.toLong(), y.toLong(), plane.toLong(), shape.toLong(), rotation.toLong())
        }

        private fun pack(id: Long, x: Long, y: Long, plane: Long, shape: Long, rotation: Long): Long {
            return y or (x shl 14) or (plane shl 28) or (rotation shl 30) or (shape shl 32) or (id shl 37)
        }

        fun id(packed: Long): Int = (packed shr 37 and 0x1ffff).toInt()
        fun x(packed: Long): Int = (packed shr 14 and 0x3fff).toInt()
        fun y(packed: Long): Int = (packed and 0x3fff).toInt()
        fun plane(packed: Long): Int = (packed shr 28 and 0x3).toInt()
        fun shape(packed: Long): Int = (packed shr 32 and 0x1f).toInt()
        fun rotation(packed: Long): Int = (packed shr 30 and 0x3).toInt()
    }
}