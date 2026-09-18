package world.gregs.voidps.engine.data

/**
 * @param time the event occurred in seconds since epoch
 */
data class RecentEvent(
    val time: Int = 0,
    val title: String = "",
    val description: String = ""
)