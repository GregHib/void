package world.gregs.voidps.engine.data.config

import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.cache.definition.Extra
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

data class GearDefinition(
    val type: String = "",
    val levels: IntRange = 0..0,
    val equipment: Map<EquipSlot, List<Item>> = emptyMap(),
    val inventory: List<List<Item>> = emptyList(),
    override var stringId: String = "",
    override var extras: Map<String, Any>? = null,
) : Extra {
    companion object {
        @Suppress("UNCHECKED_CAST")
        operator fun invoke(type: String, map: Map<String, Any>): GearDefinition {
            val range = (map["levels"] as String).toIntRange()
            val equip = map["equipment"] as Map<String, List<Map<String, Any>>>? ?: emptyMap()
            val equipment: Map<EquipSlot, List<Item>> = equip.map { (type, list) ->
                EquipSlot.valueOf(type.toSentenceCase()) to list.map { Item(it["id"] as String, it["amount"] as? Int ?: 1) }
            }.toMap()
            val inv = (map["inventory"] as? List<Map<String, Any>>) ?: emptyList()
            val inventory: List<List<Item>> = inv.mapNotNull { item ->
                val amount = item["amount"] as? Int ?: 1
                when (val id = item["id"]) {
                    is String -> listOf(Item(id, amount))
                    is List<*> -> (id as List<String>).map { Item(it, amount) }
                    else -> null
                }
            }
            val extras = map as MutableMap<String, Any>
            extras.remove("type")
            extras.remove("levels")
            extras.remove("equipment")
            extras.remove("inventory")
            return GearDefinition(type, range, equipment, inventory, extras = extras)
        }
    }
}
