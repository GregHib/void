package world.gregs.voidps.engine.data.definition.data

import world.gregs.config.ConfigReader

/**
 * @param item id of the item given
 * @param respawnDelay seconds until object is respawned
 * @param message message to send when picked
 */
data class Pickable(
    val item: String = "",
    val respawnDelay: Int = -1,
    val message: String = ""
) {
    companion object {

        operator fun invoke(reader: ConfigReader): Pickable {
            var item = ""
            var delay = -1
            var message = ""
            while (reader.nextEntry()) {
                when (val key = reader.key()) {
                    "item" -> item = reader.string()
                    "delay" -> delay = reader.int()
                    "message" -> message = reader.string()
                    else -> throw IllegalArgumentException("Unexpected key: '$key' ${reader.exception()}")
                }
            }
            return Pickable(item = item, respawnDelay = delay, message = message,)
        }

        val EMPTY = Pickable()
    }
}