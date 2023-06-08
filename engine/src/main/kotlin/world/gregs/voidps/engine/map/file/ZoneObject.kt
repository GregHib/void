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
            return x  + (y shl 3) + (plane shl 6) + (rotation shl 8) + (type shl 10) + (id shl 15)
        }

        fun id(value: Int): Int = value shr 15 and 0x1ffff

        fun x(value: Int): Int = value and 0x7

        fun y(value: Int): Int = value shr 3 and 0x7

        fun plane(value: Int): Int = value shr 6 and 0x3

        fun rotation(value: Int): Int = value shr 8 and 0x3

        fun type(value: Int): Int = value shr 10 and 0x1f

        fun tile(value: Int): Int = value and 0x3f
        fun tileX(tile: Int) = tile and 0x7
        fun tileY(tile: Int) = tile shr 3 and 0x7

        fun info(value: Int): Int = value shr 8
        fun infoId(info: Int) = info shr 7
        fun infoRotation(info: Int) = info and 0x3
        fun infoType(info: Int) = info shr 2 and 0x1f

    }

    override fun toString(): String {
        return "ZoneObject(value=$value, id=$id, x=$x, y=$y, plane=$plane, type=$type, rotation=$rotation)"
    }
}