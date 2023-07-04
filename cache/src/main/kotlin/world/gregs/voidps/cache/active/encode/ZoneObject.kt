package world.gregs.voidps.cache.active.encode

@JvmInline
value class ZoneObject(val packed: Int) {

    constructor(id: Int, x: Int, y: Int, plane: Int, shape: Int, rotation: Int) : this(pack(id, x, y, plane, shape, rotation))

    val id: Int
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
        return "ZoneObject(id=$id, x=$x, y=$y, plane=$plane, shape=$shape, rotation=$rotation)"
    }

    companion object {

        fun pack(id: Int, x: Int, y: Int, plane: Int, shape: Int, rotation: Int): Int {
            return x  + (y shl 3) + (plane shl 6) + (rotation shl 8) + (shape shl 10) + (id shl 15)
        }

        fun id(packed: Int): Int = packed shr 15 and 0x1ffff
        fun x(packed: Int): Int = packed and 0x7
        fun y(packed: Int): Int = packed shr 3 and 0x7
        fun plane(packed: Int): Int = packed shr 6 and 0x3
        fun shape(packed: Int): Int = packed shr 10 and 0x1f
        fun rotation(packed: Int): Int = packed shr 8 and 0x3

        /**
         * Takes the first half of the [packed] value which is equivalent to Tile#index
         */
        fun tile(value: Int): Int = value and 0x3f

        /**
         * Takes the second half of [packed] which is the id, rotation and shape
         * @see world.gregs.voidps.engine.entity.obj.GameObjects for usage
         */
        fun info(value: Int): Int = value shr 8
        fun infoId(info: Int) = info shr 7
        fun infoRotation(info: Int) = info and 0x3
        fun infoShape(info: Int) = info shr 2 and 0x1f

    }
}