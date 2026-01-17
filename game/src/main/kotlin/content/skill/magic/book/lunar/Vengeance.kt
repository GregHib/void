package content.skill.magic.book.lunar

import content.entity.combat.hit.hit
import content.skill.magic.spell.removeSpellItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.timer.epochSeconds

class Vengeance(val definitions: SpellDefinitions) : Script {

    init {
        interfaceOption("Cast", "lunar_spellbook:vengeance") {
            val spell = it.component
            if (contains("vengeance")) {
                message("You already have vengeance cast.")
                return@interfaceOption
            }
            if (remaining("vengeance_delay", epochSeconds()) > 0) {
                message("You can only cast vengeance spells once every 30 seconds.")
                return@interfaceOption
            }
            if (!removeSpellItems(spell)) {
                return@interfaceOption
            }
            val definition = definitions.get(spell)
            anim(spell)
            gfx(spell)
            sound(spell)
            experience.add(Skill.Magic, definition.experience)
            set("vengeance", true)
            start("vengeance_delay", definition["delay_seconds"], epochSeconds())
        }

        combatDamage { (source, type, damage) ->
            if (!contains("vengeance") || type == "damage" || damage < 4) {
                return@combatDamage
            }
            say("Taste vengeance!")
            hit(target = source, offensiveType = "damage", delay = 0, damage = (damage * 0.75).toInt())
            stop("vengeance")
        }
    }
}
