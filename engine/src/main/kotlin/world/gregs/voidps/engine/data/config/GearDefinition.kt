package world.gregs.voidps.engine.data.config

import world.gregs.voidps.cache.definition.Extra
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.network.protocol.visual.update.player.EquipSlot

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
            val range = map["levels"] as IntRange
            val equipment = (map["equipment"] as? Map<EquipSlot, List<Item>>) ?: emptyMap()
            val inventory = (map["inventory"] as? List<List<Item>>) ?: emptyList()
            val extras = map.toMutableMap()
            extras.remove("type")
            extras.remove("levels")
            extras.remove("equipment")
            extras.remove("inventory")
            return GearDefinition(type, range, equipment, inventory, extras = extras)
        }
    }
}