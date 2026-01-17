package content.skill.magic.book

import content.skill.magic.spell.hasSpellItems
import content.skill.magic.spell.spell
import world.gregs.voidps.engine.Script

class SpellRunes : Script {

    init {
        combatPrepare(style = "magic") { _ ->
            if (spell.isNotBlank() && !hasSpellItems(spell)) {
                clear("autocast")
                false
            } else {
                true
            }
        }
    }
}
