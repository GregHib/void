package content.skill.magic.book.lunar

import content.entity.sound.sound
import content.skill.magic.spell.removeSpellItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.epochSeconds

class VengeanceOther : Script {

    val definitions: SpellDefinitions by inject()

    init {
        onPlayerApproach("lunar_spellbook:vengeance_other") { (target) ->
            approachRange(2)
            if (target.contains("vengeance")) {
                message("This player already has vengeance cast.")
                return@onPlayerApproach
            }
            if (remaining("vengeance_delay", epochSeconds()) > 0) {
                message("You can only cast vengeance spells once every 30 seconds.")
                return@onPlayerApproach
            }
            if (!get("accept_aid", true)) {
                message("This player is not currently accepting aid.") // TODO proper message
                return@onPlayerApproach
            }
            if (!removeSpellItems("vengeance_other")) {
                return@onPlayerApproach
            }
            val definition = definitions.get("vengeance_other")
            start("movement_delay", 2)
            anim("lunar_cast")
            target.gfx("vengeance_other")
            sound("vengeance_other")
            experience.add(Skill.Magic, definition.experience)
            target["vengeance"] = true
            start("vengeance_delay", definition["delay_seconds"], epochSeconds())
        }
    }
}
