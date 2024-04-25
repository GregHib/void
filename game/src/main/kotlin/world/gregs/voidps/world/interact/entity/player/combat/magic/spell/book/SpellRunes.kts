package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book

import world.gregs.voidps.world.interact.entity.combat.combatPrepare
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.hasSpellItems
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell

combatPrepare(style = "magic") { player ->
    if (player.spell.isNotBlank() && !player.hasSpellItems(player.spell)) {
        player.clear("autocast")
        cancel()
        return@combatPrepare
    }
}