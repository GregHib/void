package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatHit
import world.gregs.voidps.world.interact.entity.player.combat.melee.multiTargetHit

characterCombatHit { character ->
    if (spell.isNotBlank()) {
        character.setGraphic("${spell}_hit")
    }
}

multiTargetHit({ Spell.isMultiTarget(spell) }, { 9 })