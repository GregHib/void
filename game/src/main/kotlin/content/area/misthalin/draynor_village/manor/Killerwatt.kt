package content.area.misthalin.draynor_village.manor

import content.entity.effect.transform
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class Killerwatt : Script {
    init {
        npcCombatDamage("killerwatt") { (source) ->
            if (transform != "") {
                return@npcCombatDamage
            }
            areaSound("killerwatt_transforms", tile, radius = 5)
            transform("killerwatt_attacking")
        }

        npcCondition("insulated") { it is Player && it.equipped(EquipSlot.Feet).id == "insulated_boots" }
        npcCondition("not_insulated") { it is Player && it.equipped(EquipSlot.Feet).id != "insulated_boots" }
    }
}
