package content.area.fremennik_province.rellekka

import content.skill.prayer.getActivePrayerVarKey
import content.skill.prayer.isCurses
import content.skill.prayer.protectMagic
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player

class Aquanite : Script {
    init {
        npcCondition("target_protect_magic") { target ->
            target is Player && target.protectMagic()
        }

        npcAttack("aquanite", "special") { target ->
            if (target !is Player) {
                return@npcAttack
            }
            val key = target.getActivePrayerVarKey()
            if (target.isCurses()) {
                // https://youtu.be/bCC4wZeCjgY
                target.message("The creature's attack turns off your Deflect Magic curse!")
                target.removeVarbit(key, "deflect_magic")
            } else {
                // https://youtu.be/vuzxRaAqRWY?t=236
                target.message("The creature's attack turns off your Protect from Magic prayer!")
                target.removeVarbit(key, "protect_from_magic")
            }
        }
    }
}
