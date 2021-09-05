package world.gregs.voidps.world.interact.entity.player.combat

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing

on<CombatSwing>({ player -> player.specialAttack }, Priority.LOWEST) { player: Player ->
    player.specialAttack = false
}