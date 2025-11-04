package content.entity.player.equip

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.ItemOption
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.InterfaceInteraction
import world.gregs.voidps.engine.inv.sendInventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class WornEquipment : Script {

    val logger = InlineLogger()

    init {
        interfaceRefresh("worn_equipment") { id ->
            sendInventory(id)
        }

        interfaceOption("Show Equipment Stats", "worn_equipment:bonuses") {
            set("equipment_bank_button", false)
            open("equipment_bonuses")
        }

        interfaceOption("Show Price-checker", "worn_equipment:price") {
            open("price_checker")
        }

        interfaceOption("Show Items Kept on Death", "worn_equipment:items") {
            open("items_kept_on_death")
        }

        interfaceOption(id = "worn_equipment:*_slot") {
            val equipOption = getEquipmentOption(it.item.def, it.optionIndex)
            if (equipOption == null) {
                logger.info { "Unhandled equipment option ${it.item} - ${it.optionIndex}" }
                return@interfaceOption
            }
            val slot = EquipSlot.by(it.component.removeSuffix("_slot"))
            closeInterfaces()
            InterfaceInteraction.itemOption(this, ItemOption(it.item, slot.index, "worn_equipment", equipOption))
        }
    }

    fun getEquipmentOption(itemDef: ItemDefinition, optionId: Int): String? {
        val equipOption: String? = itemDef.getOrNull<Map<Int, String>>("worn_options")?.get(optionId - 1)
        if (equipOption != null) {
            return equipOption
        }
        return when (optionId) {
            0 -> "Remove"
            9 -> "Examine"
            else -> null
        }
    }
}
