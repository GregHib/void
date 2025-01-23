package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.lunar

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.world.interact.entity.combat.hit.combatHit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.removeSpellItems

val definitions: SpellDefinitions by inject()

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
    player.experience.add(Skill.Magic, definition.experience)
    player["vengeance"] = true
    player.start("vengeance_delay", definition["delay_seconds"], epochSeconds())
}

combatHit { player ->
    if (!player.contains("vengeance") || type == "damage" || damage < 4) {
        return@combatHit
    }
    player.say("Taste vengeance!")
    player.hit(target = source, type = "damage", delay = 0, damage = (damage * 0.75).toInt())
    player.stop("vengeance")
}