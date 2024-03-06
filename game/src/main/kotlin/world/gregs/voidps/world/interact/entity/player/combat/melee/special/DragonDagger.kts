package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit

val special: suspend CombatSwing.(Player) -> Unit = special@{ player ->
    player.setAnimation("puncture")
    player.setGraphic("puncture")
    player.hit(target)
    player.hit(target)
    delay = 4
}
combatSwing("dragon_dagger*", "melee", special = true, block = special)