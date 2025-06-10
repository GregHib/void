package content.skill.magic.book.lunar

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inject
import content.skill.magic.spell.removeSpellItems
import content.entity.effect.toxin.curePoison
import content.entity.effect.toxin.poisoned
import content.entity.sound.sound

val definitions: SpellDefinitions by inject()
val players: Players by inject()

interfaceOption("Cast", "cure_group", "lunar_spellbook") {
    val spell = component
    if (!player.removeSpellItems(spell)) {
        return@interfaceOption
    }
    val definition = definitions.get(spell)
    player.anim("lunar_cast_group")
    player.sound(spell)
    player.experience.add(Skill.Magic, definition.experience)
    players
        .filter { other -> other.tile.within(player.tile, 1) && other.poisoned }
        .forEach { target ->
            target.gfx(spell)
            target.sound("cure_other_impact")
            target.curePoison()
            target.message("You have been cured by ${player.name}")
        }
}
