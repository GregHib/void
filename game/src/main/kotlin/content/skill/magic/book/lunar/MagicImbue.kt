package content.skill.magic.book.lunar

import content.skill.magic.spell.removeSpellItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound

class MagicImbue : Script {

    init {
        interfaceOption("Cast", "lunar_spellbook:magic_imbue") {
            val spell = it.component
            if (hasClock("magic_imbue")) {
                message("You are already charged to combine runes!")
                return@interfaceOption
            }
            if (!removeSpellItems(spell)) {
                return@interfaceOption
            }
            anim("magic_imbue")
            gfx(spell)
            sound(spell)
            exp(Skill.Magic, Tables.int("spells.$spell.xp") / 10.0)
            start("magic_imbue", 20)
            message("You are charged to combine runes!")
        }
    }
}
