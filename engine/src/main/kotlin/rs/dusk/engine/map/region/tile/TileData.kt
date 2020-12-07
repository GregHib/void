package rs.dusk.engine.map.region.tile

data class TileData(
    var height: Int = 0,
    var attrOpcode: Int = 0,
    var settings: Byte = 0,
    var overlayId: Int = 0,
    var overlayPath: Int = 0,
    var overlayRotation: Int = 0,
    var underlayId: Int = 0
) {

    fun modified() = height != 0 || attrOpcode != 0 || settings != 0.toByte() || overlayId != 0 || overlayPath != 0 || overlayRotation != 0 || underlayId != 0

    fun isTile(flag: Int) = settings.toInt() and flag == flag
}
