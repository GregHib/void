package content.area.misthalin.draynor_village.manor

import content.entity.effect.transform
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.areaSound

class Killerwatt : Script {
    init {
        npcCombatDamage("killerwatt") { (source) ->
            if (transform != "") {
                return@npcCombatDamage
            }
            areaSound("killerwatt_transforms", tile, radius = 5)
            transform("killerwatt_attacking")
        }
    }
}
