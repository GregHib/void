package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.world.interact.entity.combat.Target
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy

combatSwing("darklight", "melee", special = true) { player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 2)) {
        delay = -1
        return@combatSwing
    }
    player.setAnimation("darklight_weaken")
    player.setGraphic("darklight_weaken")
    val damage = player.hit(target)
    if (damage > 0) {
        val amount = if (Target.isDemon(target)) 0.10 else 0.05
        target.levels.drain(Skill.Attack, multiplier = amount)
        target.levels.drain(Skill.Strength, multiplier = amount)
        target.levels.drain(Skill.Defence, multiplier = amount)
    }
    delay = 5
}