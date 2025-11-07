package content.skill.ranged.weapon.special

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class EnchantedBolts : Script {

    init {
        combatAttack("range") { (_, damage) ->
            if (!hasClock("life_leech") || damage < 4) {
                return@combatAttack
            }
            levels.restore(Skill.Constitution, damage / 4)
        }
    }
}
