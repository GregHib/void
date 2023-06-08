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
value class GameObject(internal val hash: Long) : Entity {

    constructor(id: Int, x: Int, y: Int, plane: Int, type: Int, rotation: Int) : this(getHash(id, x, y, plane, type, rotation))

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
        get() = getId(hash)
    val x: Int
        get() = getX(hash)
    val y: Int
        get() = getY(hash)
    val plane: Int
        get() = getPlane(hash)
    val type: Int
        get() = getType(hash)
    val rotation: Int
        get() = getRotation(hash)

    override fun toString(): String {
        return "GameObject(id=$intId, tile=$tile, type=$type, rotation=$rotation)"
    }

    companion object {
        operator fun invoke(id: Int, tile: Tile, type: Int, rotation: Int): GameObject {
            return GameObject(id, tile.x, tile.y, tile.plane, type, rotation)
        }

        fun getHash(id: Int, x: Int, y: Int, plane: Int, type: Int, rotation: Int): Long {
            return getHash(id.toLong(), x.toLong(), y.toLong(), plane.toLong(), type.toLong(), rotation.toLong())
        }

        fun getHash(id: Long, x: Long, y: Long, plane: Long, type: Long, rotation: Long): Long {
            return y or (x shl 14) or (plane shl 28) or (rotation shl 30) or (type shl 32) or (id shl 37)
        }

        fun getId(hash: Long): Int = (hash shr 37 and 0x1ffff).toInt()

        fun getX(hash: Long): Int = (hash shr 14 and 0x3fff).toInt()

        fun getY(hash: Long): Int = (hash and 0x3fff).toInt()

        fun getPlane(hash: Long): Int = (hash shr 28 and 0x3).toInt()

        fun getType(hash: Long): Int = (hash shr 32 and 0x1f).toInt()

        fun getRotation(hash: Long): Int = (hash shr 30 and 0x3).toInt()
    }
}