package world.gregs.voidps.engine.data.definition.data

import world.gregs.voidps.engine.entity.item.Item

/**
 * @param item id of the item given
 * @param respawnDelay seconds until object is respawned
 * @param message message to send when picked
 */
data class Pickable(
    val item: Item = Item.EMPTY,
    val respawnDelay: Int = -1,
    val message: String = ""
) {
    companion object {

        operator fun invoke(map: Map<String, Any>) = Pickable(
            item = (map["item"] as? Item) ?: EMPTY.item,
            respawnDelay = map["delay"] as? Int ?: EMPTY.respawnDelay,
            message = map["message"] as? String ?: EMPTY.message,
        )

        val EMPTY = Pickable()
    }
}