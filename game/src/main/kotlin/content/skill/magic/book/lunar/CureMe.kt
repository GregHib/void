package content.skill.magic.book.lunar

import content.entity.effect.toxin.curePoison
import content.entity.effect.toxin.poisoned
import content.entity.sound.sound
import content.skill.magic.spell.removeSpellItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inject

class CureMe : Script {

    val definitions: SpellDefinitions by inject()

    init {
        interfaceOption("Cast", "lunar_spellbook:cure_me") {
            val spell = it.component
            if (!poisoned) {
                message("You are not poisoned.")
                return@interfaceOption
            }
            if (!removeSpellItems(spell)) {
                return@interfaceOption
            }
            val definition = definitions.get(spell)
            anim("lunar_cast")
            gfx(spell)
            sound(spell)
            experience.add(Skill.Magic, definition.experience)
            curePoison()
        }
    }
}
