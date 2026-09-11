package world.gregs.voidps.web.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One entry in the public world table — and in the developer panel's compact world list, which
 * shows the same data rather than a staff-only twin.
 */
@Serializable
data class World(
    val number: Int,
    val region: String,
    val country: String? = null,
    val members: Boolean = false,
    val mode: String,
    val players: Int,
    val capacity: Int,
    val ping: Int? = null,
    val status: WorldStatus,
    val hostname: String? = null,
) {
    /** Occupancy as a whole percentage, for the table's capacity bar. */
    val occupancy: Int
        get() = if (capacity <= 0) 0 else (players * 100 / capacity).coerceIn(0, 100)
}

@Serializable
enum class WorldStatus(val wire: String) {
    @SerialName("online")
    Online("online"),

    @SerialName("full")
    Full("full"),

    @SerialName("restarting")
    Restarting("restarting"),

    @SerialName("offline")
    Offline("offline"),
    ;

    companion object {
        fun of(value: String?): WorldStatus? = entries.firstOrNull { it.wire.equals(value, ignoreCase = true) }
    }
}

@Serializable
data class WorldList(
    val items: List<World>,
    val totalPlayers: Int,
)
