package content.area.fremennik_province.rellekka

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class Cockatrice : Script {
    init {
        npcCondition("mirror_shield") { it is Player && it.equipped(EquipSlot.Shield).id == "mirror_shield" }
        npcCondition("no_mirror_shield") { it is Player && it.equipped(EquipSlot.Shield).id != "mirror_shield" }
    }
}
