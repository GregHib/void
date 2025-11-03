package content.skill.magic.spell

import content.skill.melee.weapon.attackRange
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventoryChanged
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class Autocast : Script {

    val interfaceDefinitions: InterfaceDefinitions by inject()

    init {
        variableSet("autocast") { _, _, to ->
            if (to == null) {
                clear("autocast_spell")
            }
        }

        interfaceOption("Autocast", id = "*_spellbook:*") {
            toggle(it.id, it.component)
        }

        inventoryChanged("worn_equipment", EquipSlot.Weapon) { player ->
            player.clear("autocast")
        }
    }

    fun Player.toggle(id: String, component: String) {
        val value: Int? = interfaceDefinitions.getComponent(id, component)?.getOrNull("cast_id")
        if (value == null || get("autocast", 0) == value) {
            clear("autocast")
        } else {
            set("autocast_spell", component)
            attackRange = 8
            set("autocast", value)
        }
    }
}
