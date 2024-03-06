package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.melee.drainByDamage

combatSwing("bone_dagger*", "melee", special = true) { player ->
    player.setAnimation("backstab")
    player.setGraphic("backstab")
    val damage = player.hit(target)
    target.drainByDamage(damage, Skill.Defence)
    delay = 4
}