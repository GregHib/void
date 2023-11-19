package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.Spell
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell

on<CombatSwing>({ it.spell.isNotBlank() }, Priority.HIGHER) { player: Player ->
    if (!Spell.removeRequirements(player, player.spell)) {
        player.clear("autocast")
        delay = -1
        cancel()
        return@on
    }
}