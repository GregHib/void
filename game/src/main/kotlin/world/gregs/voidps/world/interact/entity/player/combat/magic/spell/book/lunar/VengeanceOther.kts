package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.lunar

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnPlayerApproach
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.suspend.approachRange
import world.gregs.voidps.engine.suspend.pause
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.Spell

val definitions: SpellDefinitions by inject()

itemOnPlayerApproach(id = "lunar_spellbook", component = "vengeance_other") {
    player.approachRange(2)
    pause()
    val spell = component
    if (target.contains("vengeance")) {
        player.message("This player already has vengeance cast.")
        return@itemOnPlayerApproach
    }
    if (player.remaining("vengeance_delay", epochSeconds()) > 0) {
        player.message("You can only cast vengeance spells once every 30 seconds.")
        return@itemOnPlayerApproach
    }
    if (!Spell.removeRequirements(player, spell)) {
        return@itemOnPlayerApproach
    }
    val definition = definitions.get(spell)
    player.start("movement_delay", 2)
    player.setAnimation("lunar_cast")
    target.setGraphic(spell)
    player.experience.add(Skill.Magic, definition.experience)
    target["vengeance"] = true
    player.start("vengeance_delay", definition["delay_seconds"], epochSeconds())
}