package world.gregs.voidps.world.interact.entity.player.combat.special

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.combatSwing

combatSwing({ player -> player.specialAttack }, Priority.LOWEST) { player: Player ->
    player.specialAttack = false
}