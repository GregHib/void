package content.skill.magic.book.modern

import content.skill.magic.spell.removeSpellItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.timer.TICKS

class Charge(val definitions: SpellDefinitions) : Script {

    init {
        interfaceOption("Cast", "modern_spellbook:charge") {
            if (hasClock("charge_delay")) {
                val remaining = TICKS.toSeconds(remaining("charge_delay"))
                message("You must wait another $remaining ${"second".plural(remaining)} before casting this spell again.")
                return@interfaceOption
            }
            val spell = it.component
            if (!removeSpellItems(spell)) {
                return@interfaceOption
            }

            val definition = definitions.get(spell)
            anim(spell)
            sound(spell)
            experience.add(Skill.Magic, definition.experience)
            start("charge", definition["effect_ticks"])
            start("charge_delay", definition["delay_ticks"])
        }
    }
}
