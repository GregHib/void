package content.skill.magic.spell

import content.skill.melee.weapon.attackRange
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.variable.variableSet
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventoryChanged
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.engine.event.Script
@Script
class Autocast {

    val interfaceDefinitions: InterfaceDefinitions by inject()
    
    init {
        interfaceOption("Autocast", id = "*_spellbook") {
            toggle()
        }

        variableSet("autocast", to = null) { player ->
            player.clear("autocast_spell")
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
