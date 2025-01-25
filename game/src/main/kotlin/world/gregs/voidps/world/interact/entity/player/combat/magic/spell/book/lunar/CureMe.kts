package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.lunar

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.removeSpellItems
import content.entity.effect.toxin.curePoison
import content.entity.effect.toxin.poisoned

val definitions: SpellDefinitions by inject()

interfaceOption(component = "cure_me", id = "lunar_spellbook") {
    val spell = component
    if (!player.poisoned) {
        player.message("You are not poisoned.")
        return@interfaceOption
    }
    if (!player.removeSpellItems(spell)) {
        return@interfaceOption
    }
    val definition = definitions.get(spell)
    player.anim("lunar_cast")
    player.gfx(spell)
    player.experience.add(Skill.Magic, definition.experience)
    player.curePoison()
}
