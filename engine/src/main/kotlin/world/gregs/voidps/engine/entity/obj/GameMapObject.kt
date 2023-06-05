package world.gregs.voidps.engine.entity.obj

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.data.definition.extra.ObjectDefinitions
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.Tile

@JvmInline
value class GameMapObject(val hash: Long) : Entity {

    constructor(id: Int, x: Int, y: Int, plane: Int, type: Int, rotation: Int) : this(getHash(id, x, y, plane, type, rotation))

    constructor(value: Int, x: Int, y: Int, plane: Int) : this(id(value), x, y, plane, type(value), rotation(value))

    override val size: Size
        get() = Size(def.sizeX, def.sizeY)
    val value: Int
        get() = value(intId, type, rotation)
    val intId: Int
        get() = getId(hash)
    override var tile: Tile
        get() = Tile(x, y, plane)
        set(value) {}
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
    val id: String
        get() = def.stringId
    val group: Int
        get() = ObjectGroup.group(type)
    val def: ObjectDefinition
        get() = get<ObjectDefinitions>().get(intId)

    override val events: Events
        get() = Events(this)

    companion object {
        operator fun invoke(id: Int, tile: Tile, type: Int, rotation: Int): GameMapObject {
            return GameMapObject(id, tile.x, tile.y, tile.plane, type, rotation)
        }

        fun value(id: Int, type: Int, rotation: Int) = rotation + (type shl 2) + (id shl 7)

        fun id(value: Int): Int = value shr 7 and 0x1ffff

        fun type(value: Int): Int = value shr 2 and 0x1f

        fun rotation(value: Int): Int = value and 0x3

        fun getHash(id: Int, x: Int, y: Int, plane: Int, type: Int, rotation: Int): Long {
            return getHash(id.toLong(), x.toLong(), y.toLong(), plane.toLong(), type.toLong(), rotation.toLong())
        }

        fun getHash(id: Long, x: Long, y: Long, plane: Long, type: Long, rotation: Long): Long {
            return rotation + (type shl 2) + (plane shl 7) + (y shl 9) + (x shl 23) + (id shl 37)
        }

        fun getId(hash: Long): Int = (hash shr 37 and 0x1ffff).toInt()

        fun getX(hash: Long): Int = (hash shr 23 and 0x3fff).toInt()

        fun getY(hash: Long): Int = (hash shr 9 and 0x3fff).toInt()

        fun getPlane(hash: Long): Int = (hash shr 7 and 0x3).toInt()

        fun getType(hash: Long): Int = (hash shr 2 and 0x1f).toInt()

        fun getRotation(hash: Long): Int = (hash and 0x3).toInt()
    }
}