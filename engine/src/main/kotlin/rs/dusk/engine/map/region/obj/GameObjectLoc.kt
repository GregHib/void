package rs.dusk.engine.map.region.obj

data class GameObjectLoc(
    val id: Int,
    val regionX: Int,
    val regionY: Int,
    val localX: Int,
    val localY: Int,
    val plane: Int,
    val type: Int,
    val rotation: Int
) {
    val x = regionX * 64 + localX
    val y = regionY * 64 + localY
}