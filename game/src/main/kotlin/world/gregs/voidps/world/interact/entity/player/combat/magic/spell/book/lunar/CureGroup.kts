package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.lunar

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.removeSpellItems
import world.gregs.voidps.world.interact.entity.player.toxin.curePoison
import world.gregs.voidps.world.interact.entity.player.toxin.poisoned

val definitions: SpellDefinitions by inject()
val players: Players by inject()

interfaceOption(component = "cure_group", id = "lunar_spellbook") {
    val spell = component
    if (!player.removeSpellItems(spell)) {
        return@interfaceOption
    }
    val definition = definitions.get(spell)
    player.setAnimation("lunar_cast_group")
    player.experience.add(Skill.Magic, definition.experience)
    players
        .filter { other -> other.tile.within(player.tile, 1) && other.poisoned }
        .forEach { target ->
            target.gfx(spell)
            target.curePoison()
            target.message("You have been cured by ${player.name}")
        }
}
