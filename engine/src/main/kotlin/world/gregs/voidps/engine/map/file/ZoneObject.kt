package world.gregs.voidps.engine.map.file

@JvmInline
value class ZoneObject(val value: Int) {

    constructor(id: Int, x: Int, y: Int, plane: Int, type: Int, rotation: Int) : this(value(id, x, y, plane, type, rotation))

    val id: Int
        get() = id(value)
    val x: Int
        get() = x(value)
    val y: Int
        get() = y(value)
    val plane: Int
        get() = plane(value)
    val type: Int
        get() = type(value)
    val rotation: Int
        get() = rotation(value)

    companion object {

        fun value(id: Int, x: Int, y: Int, plane: Int, type: Int, rotation: Int): Int {
            return rotation + (type shl 2) + (plane shl 7) + (y shl 9) + (x shl 12) + (id shl 15)
        }

        fun id(hash: Int): Int = hash shr 15 and 0x1ffff

        fun x(hash: Int): Int = hash shr 12 and 0x7

        fun y(hash: Int): Int = hash shr 9 and 0x7

        fun plane(hash: Int): Int = hash shr 7 and 0x3

        fun type(hash: Int): Int = hash shr 2 and 0x1f

        fun rotation(hash: Int): Int = hash and 0x3

    }
}