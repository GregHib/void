package content.skill.magic.book.lunar

import content.entity.effect.toxin.curePoison
import content.entity.effect.toxin.poisoned
import content.entity.sound.sound
import content.skill.magic.spell.removeSpellItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.interfaceOnPlayerApproach
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inject

class CureOther : Script {

    val definitions: SpellDefinitions by inject()

    init {
        interfaceOnPlayerApproach(id = "lunar_spellbook", component = "cure_other") {
            approachRange(2)
            val spell = component
            if (!target.poisoned) {
                player.message("This player is not poisoned.")
                return@interfaceOnPlayerApproach
            }
            if (!player["accept_aid", true]) {
                player.message("This player is not currently accepting aid.") // TODO proper message
                return@interfaceOnPlayerApproach
            }
            if (!player.removeSpellItems(spell)) {
                return@interfaceOnPlayerApproach
            }
            val definition = definitions.get(spell)
            player.start("movement_delay", 2)
            player.anim("lunar_cast")
            target.gfx(spell)
            player.sound(spell)
            player.experience.add(Skill.Magic, definition.experience)
            target.curePoison()
            target.sound("cure_other_impact")
            target.message("You have been cured by ${player.name}.")
        }
    }
}
