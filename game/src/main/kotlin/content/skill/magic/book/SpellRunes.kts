package content.skill.magic.book

import content.entity.combat.combatPrepare
import content.skill.magic.spell.hasSpellItems
import content.skill.magic.spell.spell

combatPrepare(style = "magic") { player ->
    if (player.spell.isNotBlank() && !player.hasSpellItems(player.spell)) {
        player.clear("autocast")
        cancel()
        return@combatPrepare
    }
}
