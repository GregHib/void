package world.gregs.voidps.world.interact.entity.player.combat.magic

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing

on<CombatSwing>({ it.contains("spell") }, Priority.LOWEST) { player: Player ->
    player.clear("spell")
}