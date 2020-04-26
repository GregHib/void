package rs.dusk.engine.entity.model

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 26, 2020
 */
data class Changes(
    var localUpdate: Int = -1,
    var localValue: Int = 0,
    var regionUpdate: Int = 0,
    var regionValue: Int = 0
) {

    companion object {
        // Local
        const val UPDATE = 0
        const val NONE = 0
        const val WALK = 1
        const val RUN = 2
        const val TELE = 3

        // Region
        const val HEIGHT = 1
        const val LOCAL_REGION = 2
        const val OTHER_REGION = 3
    }
}