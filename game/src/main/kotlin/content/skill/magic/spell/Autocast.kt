package content.skill.magic.spell

import content.skill.melee.weapon.attackRange
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.variable.Variable
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventoryChanged
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

@Script
class Autocast : Api {

    val interfaceDefinitions: InterfaceDefinitions by inject()

    @Variable("autocast")
    override fun variableSet(player: Player, key: String, from: Any?, to: Any?) {
        if (to == null) {
            player.clear("autocast_spell")
        }
    }

    init {
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
