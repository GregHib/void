package content.skill.magic.spell

import world.gregs.voidps.type.random
import content.entity.combat.hit.characterCombatHit
import content.entity.combat.hit.combatAttack
import content.entity.combat.hit.directHit
import content.area.wilderness.inMultiCombat
import content.skill.melee.weapon.multiTargets
import kotlin.random.nextInt

characterCombatHit { character ->
    if (spell.isNotBlank()) {
        character.gfx("${spell}_hit")
    }
}

combatAttack(type = "magic") { source ->
    if (!target.inMultiCombat) {
        return@combatAttack
    }
    if (spell.endsWith("_burst") || spell.endsWith("_barrage")) {
        val targets = multiTargets(target, 9)
        for (target in targets) {
            target.directHit(source, random.nextInt(0..damage), type, weapon, spell)
        }
    }
}