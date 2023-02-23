package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.data.definition.extra.SpellDefinitions
import world.gregs.voidps.engine.entity.character.forceChat
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.world.interact.entity.combat.CombatHit
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.player.combat.magic.Runes

val definitions: SpellDefinitions by inject()

on<InterfaceOption>({ id == "lunar_spellbook" && component == "vengeance" && option == "Cast" }) { player: Player ->
    val spell = component
    if (player.hasVar("vengeance")) {
        player.message("You already have vengeance cast.")
        return@on
    }
    if (player.remaining("vengeance_delay", epochSeconds()) > 0) {
        player.message("You can only cast vengeance spells once every 30 seconds.")
        return@on
    }
    if (!Runes.hasSpellRequirements(player, spell)) {
        return@on
    }
    val definition = definitions.get(spell)
    player.setAnimation(spell)
    player.setGraphic(spell)
    player.experience.add(Skill.Magic, definition.experience)
    player.setVar("vengeance", true)
    player.start("vengeance_delay", definition["delay_seconds"], epochSeconds())
}

on<CombatHit>({ target -> target.hasVar("vengeance") && type != "damage" && damage >= 4 }) { player: Player ->
    player.forceChat = "Taste vengeance!"
    player.hit(source, null, "damage", 0, "", false, damage = (damage * 0.75).toInt())
    player.stop("vengeance")
}