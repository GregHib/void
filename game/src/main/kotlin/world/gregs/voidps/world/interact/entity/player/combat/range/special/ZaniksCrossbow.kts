package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.proj.shoot

combatSwing("zaniks_crossbow", style = "range", special = true) { player ->
    player.setAnimation("zaniks_crossbow_special")
    player.setGraphic("zaniks_crossbow_special")
    val time = player.shoot(id = "zaniks_crossbow_bolt", target = target)
    val damage = player.hit(target, delay = time)
    if (damage != -1) {
        target.levels.drain(Skill.Defence, damage / 10)
    }
}