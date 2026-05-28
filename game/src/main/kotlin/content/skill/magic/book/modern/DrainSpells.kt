package content.skill.magic.book.modern

import content.skill.magic.spell.Spell
import content.skill.magic.spell.spell
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Tables

class DrainSpells : Script {
    init {
        combatPrepare("magic") { target ->
            if (Spell.canDrain(target, spell) == false) {
                message("Your foe's ${Tables.skill("spells.$spell.drain_skill").name.lowercase()} has already been ${if (spell == "confuse" || spell == "weaken") "lowered" else "weakened"}.")
                false
            } else {
                true
            }
        }
    }
}
