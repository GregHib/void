package rs.dusk.engine.entity.model

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class Graphic(
    val id: Int,
    val delay: Int = 0,
    val height: Int = 0,
    val rotation: Int = 0,
    val forceRefresh: Boolean = false
) {
    var index: Int = -1
}