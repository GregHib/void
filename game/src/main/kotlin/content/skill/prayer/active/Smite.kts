package content.skill.prayer.active

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.interact.entity.combat.hit.combatAttack
import content.skill.prayer.praying

combatAttack { player ->
    if (damage <= 40 || !player.praying("smite")) {
        return@combatAttack
    }
    target.levels.drain(Skill.Prayer, damage / 40)
}