package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.hit.combatAttack

combatAttack(priority = Priority.MEDIUM) { player ->
    if (type != "range" || !player.hasClock("life_leech") || damage < 4) {
        return@combatAttack
    }
    player.levels.restore(Skill.Constitution, damage / 4)
}