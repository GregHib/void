package content.skill.ranged.weapon.special

import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import content.entity.combat.hit.combatAttack

combatAttack(type = "range") { player ->
    if (!player.hasClock("life_leech") || damage < 4) {
        return@combatAttack
    }
    player.levels.restore(Skill.Constitution, damage / 4)
}