package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.CombatHit
import world.gregs.voidps.world.interact.entity.player.combat.melee.multiTargetHit

on<CombatHit>({ spell.isNotBlank() }) { character: Character ->
    character.setGraphic("${spell}_hit")
}

/**
 * Clear one use spell
 */
on<CombatSwing>({ it.contains("spell") }, Priority.LOWEST) { player: Player ->
    player.clear("spell")
}

on<CombatSwing>({ (delay ?: -1) >= 0 && it.spell.isNotBlank() }, Priority.LOWEST) { character: Character ->
    character.clear("spell")
    if (character is Player && !character.contains("autocast")) {
        character.queue.clearWeak()
    }
}

multiTargetHit({ Spell.isMultiTarget(spell) }, { 9 })