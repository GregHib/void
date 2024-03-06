package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit

val handler: suspend CombatSwing.(Player) -> Unit = handler@{ player ->
    player.setAnimation("shatter")
    player.setGraphic("shatter")
    player.hit(target)
    delay = 4
}
combatSwing("dragon_mace*", "melee", special = true, block = handler)