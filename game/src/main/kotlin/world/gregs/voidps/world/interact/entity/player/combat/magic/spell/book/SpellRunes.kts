package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book

import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.Spell
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell

combatSwing(style = "magic", priority = Priority.HIGHER) { player ->
    if (player.spell.isNotBlank() && !Spell.removeRequirements(player, player.spell)) {
        player.clear("autocast")
        delay = -1
        cancel()
        return@combatSwing
    }
}