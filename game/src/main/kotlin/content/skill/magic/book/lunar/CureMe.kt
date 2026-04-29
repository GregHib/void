package content.skill.magic.book.lunar

import content.entity.effect.toxin.curePoison
import content.entity.effect.toxin.poisoned
import content.skill.magic.spell.removeSpellItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound

class CureMe : Script {

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
            anim("lunar_cast")
            gfx(spell)
            sound(spell)
            exp(Skill.Magic, Tables.int("spells.${spell}.xp") / 10.0)
            curePoison()
        }
    }
}
