package content.skill.magic.book.lunar

import content.entity.combat.hit.combatDamage
import content.entity.combat.hit.hit
import content.entity.sound.sound
import content.skill.magic.spell.removeSpellItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.epochSeconds

class Vengeance : Script {

    val definitions: SpellDefinitions by inject()

    init {
        interfaceOption("Cast", "vengeance", "lunar_spellbook") {
            val spell = component
            if (player.contains("vengeance")) {
                player.message("You already have vengeance cast.")
                return@interfaceOption
            }
            if (player.remaining("vengeance_delay", epochSeconds()) > 0) {
                player.message("You can only cast vengeance spells once every 30 seconds.")
                return@interfaceOption
            }
            if (!player.removeSpellItems(spell)) {
                return@interfaceOption
            }
            val definition = definitions.get(spell)
            player.anim(spell)
            player.gfx(spell)
            player.sound(spell)
            player.experience.add(Skill.Magic, definition.experience)
            player["vengeance"] = true
            player.start("vengeance_delay", definition["delay_seconds"], epochSeconds())
        }

        combatDamage { player ->
            if (!player.contains("vengeance") || type == "damage" || damage < 4) {
                return@combatDamage
            }
            player.say("Taste vengeance!")
            player.hit(target = source, offensiveType = "damage", delay = 0, damage = (damage * 0.75).toInt())
            player.stop("vengeance")
        }
    }
}
