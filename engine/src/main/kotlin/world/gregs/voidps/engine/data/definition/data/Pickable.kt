package world.gregs.voidps.engine.data.definition.data

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

        operator fun invoke(map: Map<String, Any>) = Pickable(
            item = (map["item"] as? String) ?: EMPTY.item,
            respawnDelay = map["delay"] as? Int ?: EMPTY.respawnDelay,
            message = map["message"] as? String ?: EMPTY.message,
        )

        val EMPTY = Pickable()
    }
}