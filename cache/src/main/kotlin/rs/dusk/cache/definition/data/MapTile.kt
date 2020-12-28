package rs.dusk.cache.definition.data

inline class MapTile(val hash: Long) {

    constructor(
        height: Int = 0,
        opcode: Int = 0,
        overlay: Int = 0,
        path: Int = 0,
        rotation: Int = 0,
        settings: Int = 0,
        underlay: Int = 0
    ) : this(getHash(height, opcode, overlay, path, rotation, settings, underlay))

    val height: Int
        get() = getHeight(hash)

    val attrOpcode: Int
        get() = getOpcode(hash)

    val overlayId: Int
        get() = getOverlay(hash)

    val overlayPath: Int
        get() = getPath(hash)

    val overlayRotation: Int
        get() = getRotation(hash)

    val settings: Int
        get() = getSettings(hash)

    val underlayId: Int
        get() = getUnderlay(hash)

    fun isTile(flag: Int) = settings and flag == flag

    companion object {

        val EMPTY = MapTile()

        fun getHash(height: Int, opcode: Int, overlay: Int, path: Int, rotation: Int, settings: Int, underlay: Int): Long {
            return getHash(height.toLong(), opcode.toLong(), overlay.toLong(), path.toLong(), rotation.toLong(), settings.toLong(), underlay.toLong())
        }

        fun getHash(height: Long, opcode: Long, overlay: Long, path: Long, rotation: Long, settings: Long, underlay: Long): Long {
            return height + (opcode shl 8) + (overlay shl 16) + (path shl 24) + (rotation shl 32) + (settings shl 40) + (underlay shl 48)
        }

        fun getHeight(hash: Long): Int = (hash and 0xff).toInt()
        fun getOpcode(hash: Long): Int = (hash shr 8 and 0xff).toInt()
        fun getOverlay(hash: Long): Int = (hash shr 16 and 0xff).toInt()
        fun getPath(hash: Long): Int = (hash shr 24 and 0xff).toInt()
        fun getRotation(hash: Long): Int = (hash shr 32 and 0xff).toInt()
        fun getSettings(hash: Long): Int = (hash shr 40 and 0xff).toInt()
        fun getUnderlay(hash: Long): Int = (hash shr 48 and 0xff).toInt()
    }
}