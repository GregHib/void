package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.combatHit
import world.gregs.voidps.world.interact.entity.player.combat.melee.multiTargetHit

combatHit({ spell.isNotBlank() }) { character: Character ->
    character.setGraphic("${spell}_hit")
}

/**
 * Clear one use spell
 */
combatSwing({ it.contains("spell") }, Priority.LOWEST) { player: Player ->
    player.clear("spell")
}

combatSwing({ (delay ?: -1) >= 0 && it.spell.isNotBlank() }, Priority.LOWEST) { character: Character ->
    character.clear("spell")
    if (character is Player && !character.contains("autocast")) {
        character.queue.clearWeak()
    }
}

multiTargetHit({ Spell.isMultiTarget(spell) }, { 9 })