package content.skill.magic.book.modern

import content.skill.magic.spell.Spell
import content.skill.magic.spell.spell
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.inject

class DrainSpells : Script {

    val spellDefinitions: SpellDefinitions by inject()

    init {
        combatPrepare("magic") { target ->
            val definition = spellDefinitions.get(spell)
            !(definition.contains("drain_skill") && !Spell.canDrain(target, definition))
        }
    }
}
