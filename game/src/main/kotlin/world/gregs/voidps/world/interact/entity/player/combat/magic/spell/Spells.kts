package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.world.interact.entity.combat.characterCombatSwing
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatHit
import world.gregs.voidps.world.interact.entity.player.combat.melee.multiTargetHit

characterCombatHit { character ->
    if (spell.isNotBlank()) {
        character.setGraphic("${spell}_hit")
    }
}

/**
 * Clear one use spell
 */
combatSwing { player ->
    player.clear("spell")
}

characterCombatSwing { character ->
    if ((delay ?: -1) >= 0) {
        character.clear("spell")
        if (character is Player && !character.contains("autocast")) {
            character.queue.clearWeak()
        }
    }
}

multiTargetHit({ Spell.isMultiTarget(spell) }, { 9 })