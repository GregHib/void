package content.skill.magic.book.lunar

import content.entity.effect.toxin.curePoison
import content.entity.effect.toxin.poisoned
import content.skill.magic.spell.removeSpellItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound

class CureGroup : Script {

    init {
        interfaceOption("Cast", "lunar_spellbook:cure_group") {
            val spell = it.component
            if (!removeSpellItems(spell)) {
                return@interfaceOption
            }
            anim("lunar_cast_group")
            sound(spell)
            exp(Skill.Magic, Tables.int("spells.${spell}.xp") / 10.0)
            Players
                .filter { other -> other.tile.within(tile, 1) && other.poisoned && get("accept_aid", true) }
                .forEach { target ->
                    target.gfx(spell)
                    target.sound("cure_other_impact")
                    target.curePoison()
                    target.message("You have been cured by $name")
                }
        }
    }
}
