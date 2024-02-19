package world.gregs.voidps.world.interact.entity.player.combat.special

import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.combatSwing

combatSwing(priority = Priority.LOWEST) { player ->
    player.specialAttack = false
}