package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book

import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.spellSwing
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.Spell
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell

spellSwing(priority = Priority.HIGHER) { player ->
    if (!Spell.removeRequirements(player, player.spell)) {
        player.clear("autocast")
        delay = -1
        cancel()
        return@spellSwing
    }
}