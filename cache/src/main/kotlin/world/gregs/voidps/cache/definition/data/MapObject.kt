package world.gregs.voidps.cache.definition.data

@JvmInline
value class MapObject(val packed: Long) {

    constructor(id: Int, x: Int, y: Int, plane: Int, type: Int, rotation: Int) : this(pack(id, x, y, plane, type, rotation))

    val id: Int
        get() = id(packed)
    val x: Int
        get() = x(packed)
    val y: Int
        get() = y(packed)
    val plane: Int
        get() = plane(packed)
    val type: Int
        get() = type(packed)
    val rotation: Int
        get() = rotation(packed)

    companion object {

        fun pack(id: Int, x: Int, y: Int, plane: Int, type: Int, rotation: Int): Long {
            return pack(id.toLong(), x.toLong(), y.toLong(), plane.toLong(), type.toLong(), rotation.toLong())
        }

        fun pack(id: Long, x: Long, y: Long, plane: Long, type: Long, rotation: Long): Long {
            return rotation + (type shl 2) + (plane shl 7) + (y shl 9) + (x shl 23) + (id shl 37)
        }

        fun id(packed: Long): Int = (packed shr 37 and 0x1ffff).toInt()
        fun x(packed: Long): Int = (packed shr 23 and 0x3fff).toInt()
        fun y(packed: Long): Int = (packed shr 9 and 0x3fff).toInt()
        fun plane(packed: Long): Int = (packed shr 7 and 0x3).toInt()
        fun type(packed: Long): Int = (packed shr 2 and 0x1f).toInt()
        fun rotation(packed: Long): Int = (packed and 0x3).toInt()

    }
}