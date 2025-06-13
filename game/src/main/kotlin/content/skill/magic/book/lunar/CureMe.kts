package content.skill.magic.book.lunar

import content.entity.effect.toxin.curePoison
import content.entity.effect.toxin.poisoned
import content.entity.sound.sound
import content.skill.magic.spell.removeSpellItems
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inject

val definitions: SpellDefinitions by inject()

interfaceOption("Cast", "cure_me", "lunar_spellbook") {
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
    player.sound(spell)
    player.experience.add(Skill.Magic, definition.experience)
    player.curePoison()
}
