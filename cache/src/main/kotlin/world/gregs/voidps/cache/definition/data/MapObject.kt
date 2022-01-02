package world.gregs.voidps.cache.definition.data

@JvmInline
value class MapObject(val hash: Long) {

    constructor(id: Int, x: Int, y: Int, plane: Int, type: Int, rotation: Int) : this(getHash(id, x, y, plane, type, rotation))

    val id: Int
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

    companion object {

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