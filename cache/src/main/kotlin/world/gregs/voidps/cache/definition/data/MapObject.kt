package world.gregs.voidps.cache.definition.data

@JvmInline
value class MapObject(val packed: Long) {

    constructor(id: Int, x: Int, y: Int, level: Int, shape: Int, rotation: Int) : this(pack(id, x, y, level, shape, rotation))

    val id: Int
        get() = id(packed)
    val x: Int
        get() = x(packed)
    val y: Int
        get() = y(packed)
    val level: Int
        get() = level(packed)
    val shape: Int
        get() = shape(packed)
    val rotation: Int
        get() = rotation(packed)

    companion object {

        fun pack(id: Int, x: Int, y: Int, level: Int, shape: Int, rotation: Int): Long = rotation.toLong() + (shape.toLong() shl 2) + (level.toLong() shl 7) + (y.toLong() shl 9) + (x.toLong() shl 23) + (id.toLong() shl 37)

        fun id(packed: Long): Int = (packed shr 37 and 0x1ffff).toInt()
        fun x(packed: Long): Int = (packed shr 23 and 0x3fff).toInt()
        fun y(packed: Long): Int = (packed shr 9 and 0x3fff).toInt()
        fun level(packed: Long): Int = (packed shr 7 and 0x3).toInt()
        fun shape(packed: Long): Int = (packed shr 2 and 0x1f).toInt()
        fun rotation(packed: Long): Int = (packed and 0x3).toInt()
    }

    override fun toString(): String = "MapObject(id=$id, x=$x, y=$y, level=$level, shape=$shape, rotation=$rotation)"
}
