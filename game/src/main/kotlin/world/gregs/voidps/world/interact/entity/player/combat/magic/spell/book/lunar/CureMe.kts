package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.lunar

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.Spell
import world.gregs.voidps.world.interact.entity.player.toxin.curePoison
import world.gregs.voidps.world.interact.entity.player.toxin.poisoned

val definitions: SpellDefinitions by inject()

on<InterfaceOption>({ id == "lunar_spellbook" && component == "cure_me" }) { player: Player ->
    val spell = component
    if (!player.poisoned) {
        player.message("You are not poisoned.")
        return@on
    }
    if (!Spell.removeRequirements(player, spell)) {
        return@on
    }
    val definition = definitions.get(spell)
    player.setAnimation("lunar_cast")
    player.setGraphic(spell)
    player.experience.add(Skill.Magic, definition.experience)
    player.curePoison()
}
