package content.skill.magic.book.lunar

import content.entity.sound.sound
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.epochSeconds
import content.skill.magic.spell.removeSpellItems
import world.gregs.voidps.engine.client.ui.interact.interfaceOnPlayerApproach

val definitions: SpellDefinitions by inject()

interfaceOnPlayerApproach(id = "lunar_spellbook", component = "vengeance_other") {
    approachRange(2)
    val spell = component
    if (target.contains("vengeance")) {
        player.message("This player already has vengeance cast.")
        return@interfaceOnPlayerApproach
    }
    if (player.remaining("vengeance_delay", epochSeconds()) > 0) {
        player.message("You can only cast vengeance spells once every 30 seconds.")
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
    target["vengeance"] = true
    player.start("vengeance_delay", definition["delay_seconds"], epochSeconds())
}