package content.skill.magic.jewellery

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.inv.itemAdded
import world.gregs.voidps.engine.inv.itemRemoved
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class RingOfWealth : Script {

    init {
        playerSpawn { player ->
            player["wearing_ring_of_wealth"] = player.equipped(EquipSlot.Ring).id == "ring_of_wealth"
        }

        itemAdded("ring_of_wealth", EquipSlot.Ring, "worn_equipment") { player ->
            player["wearing_ring_of_wealth"] = true
        }

        itemRemoved("ring_of_wealth", EquipSlot.Ring, "worn_equipment") { player ->
            player["wearing_ring_of_wealth"] = false
        }
    }
}
