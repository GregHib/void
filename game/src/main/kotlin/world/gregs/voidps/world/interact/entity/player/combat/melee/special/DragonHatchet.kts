package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit

combatSwing("dragon_hatchet", "melee", special = true)  { player ->
    player.setAnimation("clobber")
    player.setGraphic("clobber")
    val damage = player.hit(target)
    if (damage != -1) {
        val drain = damage / 100
        if (drain > 0) {
            target.levels.drain(Skill.Defence, drain)
            target.levels.drain(Skill.Magic, drain)
        }
    }
}