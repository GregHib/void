package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit

combatSwing("ancient_mace", "melee", special = true) { player ->
    player.setAnimation("favour_of_the_war_god")
    player.setGraphic("favour_of_the_war_god")
    val damage = player.hit(target)
    if (damage != -1) {
        val drain = damage / 10
        if (drain > 0) {
            target.levels.drain(Skill.Prayer, drain)
            player.levels.restore(Skill.Prayer, drain)
        }
    }
}