package content.skill.farming

import content.entity.player.dialogue.type.intEntry
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.inv.*

class FarmingEquipmentStore : Script {
    init {
        interfaceOpened("farming_equipment_store") {
            open("farming_equipment_store_side")
            sendVariable("farming_tool_rake")
            sendVariable("farming_tool_seed_dibber")
            sendVariable("farming_tool_spade")
            sendVariable("farming_tool_secateurs")
            sendVariable("farming_tool_gardening_trowel")
            sendVariable("farming_tool_scarecrow")
            sendVariable("farming_tool_bucket")
            sendVariable("farming_tool_compost")
            sendVariable("farming_tool_supercompost")
            sendVariable("farming_tool_watering_can")
            sendVariable("farming_tool_secateurs_type")
        }

        interfaceClosed("farming_equipment_store") {
            close("farming_equipment_store_side")
        }

        interfaceOption("*", "farming_equipment_store:*") {
            val component = it.component
            val current = get("farming_tool_$component", 0)
            val amount = when (it.option) {
                "Remove", "Remove 1" -> 1
                "Remove 5" -> 5
                "Remove All" -> current
                "Remove X" -> intEntry("Enter amount:")
                else -> return@interfaceOption
            }
            val item = when (component) {
                "secateurs" -> get("farming_tool_secateurs_type", "secateurs")
                "watering_can" -> "watering_can_${current - 1}"
                else -> component
            }
            if (inventory.isFull()) {
                message("You don't have room to hold that.")
                return@interfaceOption
            }
            val added = inventory.addToLimit(item, amount.coerceAtMost(current))
            if (component == "watering_can") {
                set("farming_tool_$component", 0)
            } else {
                set("farming_tool_$component", current - added)
            }
        }

        interfaceOption("*", "farming_equipment_store_side:*") {
            val component = it.component
            val current = get("farming_tool_$component", 0)
            val limit = when (component) {
                "bucket" -> 31
                "scarecrow" -> 4
                "compost", "supercompost" -> 255
                else -> 1
            }
            if (current == limit) {
                message(
                    "You cannot store ${
                        when (component) {
                            "bucket", "scarecrow" -> "that many ${component.plural(limit)}"
                            "compost", "supercompost" -> "that much $component"
                            "secateurs" -> "more than one pair of secateurs"
                            else -> "more than one ${component.toLowerSpaceCase()}"
                        }
                    } in here.",
                )
                return@interfaceOption
            }
            val amount = when (it.option) {
                "Store", "Store 1" -> 1
                "Store 5" -> 5
                "Store All" -> current
                "Store X" -> intEntry("Enter amount:")
                else -> return@interfaceOption
            }.coerceAtMost(limit - current)
            val item = when (component) {
                "watering_can" -> inventory.items.maxByOrNull { item -> item.id.startsWith("watering_can") }!!.id
                "secateurs" -> if (inventory.contains("magic_secateurs")) "magic_secateurs" else component
                else -> component
            }
            val removed = inventory.removeToLimit(item, amount)
            if (removed == 0) {
                message("You haven't got a ${component.toLowerSpaceCase()} to store.")
                return@interfaceOption
            }
            set("farming_tool_$component", (current + removed).coerceAtMost(limit))
            if (component == "secateurs") {
                set("farming_tool_secateurs_type", item)
            } else if (component == "watering_can") {
                set("farming_tool_$component", (item.substringAfterLast("_").toIntOrNull() ?: 0) + 1)
            }
        }
    }
}
