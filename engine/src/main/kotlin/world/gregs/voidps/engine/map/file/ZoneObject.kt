package world.gregs.voidps.engine.map.file

@JvmInline
value class ZoneObject(val packed: Int) {

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

    override fun toString(): String {
        return "ZoneObject(value=$packed, id=$id, x=$x, y=$y, plane=$plane, type=$type, rotation=$rotation)"
    }

    companion object {

        fun pack(id: Int, x: Int, y: Int, plane: Int, type: Int, rotation: Int): Int {
            return x  + (y shl 3) + (plane shl 6) + (rotation shl 8) + (type shl 10) + (id shl 15)
        }

        fun id(packed: Int): Int = packed shr 15 and 0x1ffff
        fun x(packed: Int): Int = packed and 0x7
        fun y(packed: Int): Int = packed shr 3 and 0x7
        fun plane(packed: Int): Int = packed shr 6 and 0x3
        fun type(packed: Int): Int = packed shr 10 and 0x1f
        fun rotation(packed: Int): Int = packed shr 8 and 0x3

        fun tile(x: Int, y: Int, group: Int): Int = (x and 0x7) or ((y and 0x7) shl 3) or (group shl 6)
        fun tile(value: Int): Int = value and 0x3f
        fun tileX(tile: Int) = tile and 0x7
        fun tileY(tile: Int) = tile shr 3 and 0x7
        fun tileGroup(tile: Int) = tile shr 6 and 0x7

        fun info(value: Int): Int = value shr 8
        fun infoId(info: Int) = info shr 7
        fun infoRotation(info: Int) = info and 0x3
        fun infoType(info: Int) = info shr 2 and 0x1f

    }
}