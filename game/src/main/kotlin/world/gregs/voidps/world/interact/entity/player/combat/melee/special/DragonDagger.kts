package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit

val handler: suspend CombatSwing.(Player) -> Unit = { player ->
    player.setAnimation("dragon_dagger_${
        when (player.attackType) {
            "slash" -> "slash"
            else -> "attack"
        }
    }")
    player.hit(target)
    delay = 4
}
combatSwing("corrupt_dragon_dagger*", "melee", block = handler)
combatSwing("dragon_dagger*", "melee", block = handler)

val special: suspend CombatSwing.(Player) -> Unit = special@{ player ->
    player.setAnimation("puncture")
    player.setGraphic("puncture")
    player.hit(target)
    player.hit(target)
    delay = 4
}
combatSwing("dragon_dagger*", "melee", special = true, block = special)