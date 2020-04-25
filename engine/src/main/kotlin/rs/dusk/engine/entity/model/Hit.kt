package rs.dusk.engine.entity.model

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class Hit(
    val amount: Int,
    val mark: Mark,
    val percentage: Int,
    val delay: Int = 0,
    val critical: Boolean = false,
    val source: Int = -1,
    val soak: Int = -1
) {

    sealed class Mark(val id: Int) {
        object Melee : Mark(0)
        object Range : Mark(1)
        object Magic : Mark(2)
        object Regular : Mark(3)
        object Reflected : Mark(4)
        object Absorb : Mark(5)
        object Poison : Mark(6)
        object Diseased : Mark(7)
        object Missed : Mark(8)
        object Healed : Mark(9)
        object Cannon : Mark(13)
    }
}