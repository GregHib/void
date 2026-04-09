package content.skill.magic.book.modern

import content.skill.magic.spell.Spell
import content.skill.magic.spell.spell
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.SpellDefinitions

class DrainSpells(val spellDefinitions: SpellDefinitions) : Script {
    init {
        combatPrepare("magic") { target ->
            val definition = spellDefinitions.get(spell)
            if (definition.contains("drain_skill") && !Spell.canDrain(target, definition)) {
                message("Your foe's ${definition.get<String>("drain_skill").lowercase()} has already been ${if (spell == "confuse" || spell == "weaken") "lowered" else "weakened"}.")
                false
            } else {
                true
            }
        }
    }
}
