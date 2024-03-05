package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book

import world.gregs.voidps.world.interact.entity.combat.combatPrepare
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.Spell
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell

combatPrepare(style = "magic") { player ->
    if (player.spell.isNotBlank() && !Spell.removeRequirements(player, player.spell)) {
        player.clear("autocast")
        cancel()
        return@combatPrepare
    }
}