package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.modern

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.TICKS
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.Spell

val definitions: SpellDefinitions by inject()

on<InterfaceOption>({ id == "modern_spellbook" && component == "charge" }) { player: Player ->
    if (player.hasClock("charge_delay")) {
        val remaining = TICKS.toSeconds(player.remaining("charge_delay"))
        player.message("You must wait another $remaining ${"second".plural(remaining)} before casting this spell again.")
        return@on
    }
    val spell = component
    if (!Spell.removeRequirements(player, spell)) {
        return@on
    }

    val definition = definitions.get(spell)
    player.setAnimation(spell)
    player.experience.add(Skill.Magic, definition.experience)
    player.start("charge", definition["effect_ticks"])
    player.start("charge_delay", definition["delay_ticks"])
}