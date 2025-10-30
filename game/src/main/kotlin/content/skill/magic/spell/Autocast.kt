package content.skill.magic.spell

import content.skill.melee.weapon.attackRange
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventoryChanged
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class Autocast : Script {

    val interfaceDefinitions: InterfaceDefinitions by inject()

    init {
        variableSet("autocast") { player, _, _, to ->
            if (to == null) {
                player.clear("autocast_spell")
            }
        }

        interfaceOption("Autocast", id = "*_spellbook") {
            toggle()
        }

        inventoryChanged("worn_equipment", EquipSlot.Weapon) { player ->
            player.clear("autocast")
        }
    }

    fun InterfaceOption.toggle() {
        val value: Int? = interfaceDefinitions.getComponent(id, component)?.getOrNull("cast_id")
        if (value == null || player["autocast", 0] == value) {
            player.clear("autocast")
        } else {
            player["autocast_spell"] = component
            player.attackRange = 8
            player["autocast"] = value
        }
    }
}
