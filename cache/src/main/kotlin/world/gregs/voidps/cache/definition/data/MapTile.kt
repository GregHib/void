package world.gregs.voidps.cache.definition.data

@JvmInline
value class MapTile(val packed: Long) {

    constructor(
        height: Int = 0,
        opcode: Int = 0,
        overlay: Int = 0,
        path: Int = 0,
        rotation: Int = 0,
        settings: Int = 0,
        underlay: Int = 0,
    ) : this(pack(height, opcode, overlay, path, rotation, settings, underlay))

    val height: Int
        get() = height(packed)
    val attrOpcode: Int
        get() = opcode(packed)
    val overlayId: Int
        get() = overlay(packed)
    val overlayPath: Int
        get() = path(packed)
    val overlayRotation: Int
        get() = rotation(packed)
    val settings: Int
        get() = settings(packed)
    val underlayId: Int
        get() = underlay(packed)

    fun isTile(flag: Int) = settings and flag == flag

    companion object {

        val EMPTY = MapTile()

        fun pack(height: Int, opcode: Int, overlay: Int, path: Int, rotation: Int, settings: Int, underlay: Int): Long = pack(height.toLong(), opcode.toLong(), overlay.toLong(), path.toLong(), rotation.toLong(), settings.toLong(), underlay.toLong())

        fun pack(height: Long, opcode: Long, overlay: Long, path: Long, rotation: Long, settings: Long, underlay: Long): Long = height + (opcode shl 8) + (overlay shl 16) + (path shl 24) + (rotation shl 32) + (settings shl 40) + (underlay shl 48)

        fun height(packed: Long): Int = (packed and 0xff).toInt()
        fun opcode(packed: Long): Int = (packed shr 8 and 0xff).toInt()
        fun overlay(packed: Long): Int = (packed shr 16 and 0xff).toInt()
        fun path(packed: Long): Int = (packed shr 24 and 0xff).toInt()
        fun rotation(packed: Long): Int = (packed shr 32 and 0xff).toInt()
        fun settings(packed: Long): Int = (packed shr 40 and 0xff).toInt()
        fun underlay(packed: Long): Int = (packed shr 48 and 0xff).toInt()
    }
}
