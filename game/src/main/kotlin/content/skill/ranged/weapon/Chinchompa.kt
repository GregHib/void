package content.skill.ranged.weapon

import content.area.wilderness.inMultiCombat
import content.entity.combat.hit.directHit
import content.skill.melee.weapon.multiTargets
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.combat.CombatDamage
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.type.random
import kotlin.random.nextInt

class Chinchompa : Script {

    init {
        combatDamage("range", ::damage)
        npcCombatDamage(style = "range", handler = ::damage)

        combatAttack("range") { (target, damage, type, weapon, spell) ->
            if (weapon.id.endsWith("chinchompa") && target.inMultiCombat) {
                val targets = multiTargets(target, if (target is Player) 9 else 11)
                for (targ in targets) {
                    targ.directHit(this, random.nextInt(0..damage), type, weapon, spell)
                }
            }
        }
    }

    fun damage(character: Character, it: CombatDamage) {
        if (it.weapon.id.endsWith("seercull")) {
            return
        }
        it.source.sound("chinchompa_explode", delay = 40)
        character.gfx("chinchompa_impact")
    }
}
