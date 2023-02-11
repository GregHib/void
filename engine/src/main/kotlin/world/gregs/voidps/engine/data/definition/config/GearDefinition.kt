package world.gregs.voidps.engine.data.definition.config

import world.gregs.voidps.cache.definition.Extra
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.client.ui.chat.toSentenceCase
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.network.visual.update.player.EquipSlot

data class GearDefinition(
    val type: String = "",
    val levels: IntRange = 0..0,
    val equipment: Map<EquipSlot, List<Item>> = emptyMap(),
    val inventory: List<List<Item>> = emptyList(),
    override var stringId: String = "",
    override var extras: Map<String, Any>? = null
) : Extra {
    companion object {
        @Suppress("UNCHECKED_CAST")
        operator fun invoke(type: String, map: Map<String, Any>): GearDefinition {
            val range = (map["levels"] as String).toIntRange()
            val equipment = (map["equipment"] as? Map<String, List<Map<String, Any>>>)
                ?.map { (key, value) -> EquipSlot.valueOf(key.toSentenceCase()) to value.map { Item(it["id"] as String, it["amount"] as? Int ?: 1) } }
                ?.toMap() ?: emptyMap()
            val inventory = (map["inventory"] as? List<Map<String, Any>>)
                ?.map { itemList(it) } ?: emptyList()
            val extras = map.toMutableMap()
            extras.remove("type")
            extras.remove("levels")
            extras.remove("equipment")
            extras.remove("inventory")
            return GearDefinition(type, range, equipment, inventory, extras = extras)
        }

        private fun itemList(value: Map<String, Any>): List<Item> {
            val amount = value["amount"] as? Int ?: 1
            val id = value["id"]
            return if (id is List<*>) {
                id.map { Item(it as String, amount) }
            } else {
                listOf(Item(id as String, amount))
            }
        }
    }
}