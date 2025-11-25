package content.skill.farming

import content.entity.player.dialogue.type.intEntry
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.inv.*

class FarmingEquipmentStore : Script {
    init {
        interfaceOpened("farming_equipment_store") {
            open("farming_equipment_store_side")
        }

        interfaceClosed("farming_equipment_store") {
            close("farming_equipment_store_side")
        }

        interfaceOption("Remove*", "farming_equipment_store:*") {
            val current = get("farming_tool_${it.component}", 0)
            val amount = when (it.option) {
                "Remove", "Remove 1" -> 1
                "Remove 5" -> 5
                "Remove-All" -> current
                "Remove-X" -> intEntry("Enter amount:")
                else -> return@interfaceOption
            }
            val item = when (it.component) {
                "secateurs" -> if (get("farming_tool_secateurs_type", "normal") == "magic") "magic_secateurs" else "secateurs"
                "watering_can" -> "watering_can_${get("farming_tool_watering_can", 0) - 1}"
                else -> it.component
            }
            if (inventory.isFull()) {
                inventoryFull() // TODO proper messages
                return@interfaceOption
            }
            val added = inventory.addToLimit(item, amount)
            dec("farming_tool_${it.component}", (current - added).coerceAtLeast(0))
            if (added < amount) {
                // TODO proper message
            }
        }

        interfaceOption("Store*", "farming_equipment_store_side:*") {
            val component = it.component
            val current = get("farming_tool_$component", 0)
            val limit = 1
            var amount = when (it.option) {
                "Store", "Store 1" -> 1
                "Store 5" -> 5
                "Store-All" -> current
                "Store-X" -> intEntry("Enter amount:")
                else -> return@interfaceOption
            }.coerceAtMost(limit)
            val item = when(component) {
                "watering_can" -> {
                    inventory.items.maxByOrNull { item -> item.id.startsWith("watering_can") }!!.id
                }
                else -> component
            }
            if (inventory.isFull()) {
                inventoryFull() // TODO proper messages
                return@interfaceOption
            }
            if (component == "watering_can") {
                amount = (item.substringAfterLast("_").toIntOrNull() ?: 0) + 1
            }
            val removed = inventory.removeToLimit(item, amount)
            inc("farming_tool_$component", (current + removed).coerceAtMost(limit))
            if (removed < amount) {
                // TODO proper message
            }
        }
    }
}