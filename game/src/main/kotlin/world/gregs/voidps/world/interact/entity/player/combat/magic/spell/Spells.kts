package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatHit
import world.gregs.voidps.world.interact.entity.combat.hit.combatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.directHit
import world.gregs.voidps.world.interact.entity.combat.inMultiCombat
import world.gregs.voidps.world.interact.entity.player.combat.melee.multiTargets
import kotlin.random.nextInt

characterCombatHit { character ->
    if (spell.isNotBlank()) {
        character.setGraphic("${spell}_hit")
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