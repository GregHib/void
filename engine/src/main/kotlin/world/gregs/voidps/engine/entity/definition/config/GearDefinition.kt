package world.gregs.voidps.engine.entity.definition.config

import world.gregs.voidps.cache.definition.Extra
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.Item

data class GearDefinition(
    val style: String = "",
    val levels: IntRange = 0..0,
    val equipment: Map<EquipSlot, List<Item>> = emptyMap(),
    val inventory: List<Item> = emptyList(),
    override var stringId: String = "",
    override var extras: Map<String, Any> = emptyMap()
) : Extra {
    companion object {
        operator fun invoke(style: String, map: Map<String, Any>): GearDefinition {
            val levels = map["levels"] as String
            val split = levels.split("-")
            val range = split.first().toInt()..split.last().toInt()
            val equipment = (map["equipment"] as? Map<String, List<Map<String, Any>>>)
                ?.map { (key, value) -> EquipSlot.valueOf(key.capitalize()) to value.map { item(it) } }
                ?.toMap() ?: emptyMap()
            val inventory = (map["inventory"] as? List<Map<String, Any>>)
                ?.map { item(it) } ?: emptyList()
            val extras = map.toMutableMap()
            extras.remove("style")
            extras.remove("levels")
            extras.remove("equipment")
            extras.remove("inventory")
            return GearDefinition(style, range, equipment, inventory, extras = extras)
        }

        private fun item(value: Map<String, Any>): Item {
            val id = value["id"] as String
            val amount = value["amount"] as? Int ?: 1
            return Item(id, amount)
        }
    }
}