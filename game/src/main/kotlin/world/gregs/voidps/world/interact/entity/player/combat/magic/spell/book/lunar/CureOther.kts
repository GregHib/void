package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.lunar

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.spellOnPlayerApproach
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.suspend.approachRange
import world.gregs.voidps.engine.suspend.pause
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.Spell
import world.gregs.voidps.world.interact.entity.player.toxin.curePoison
import world.gregs.voidps.world.interact.entity.player.toxin.poisoned

val definitions: SpellDefinitions by inject()

spellOnPlayerApproach("cure_other", "lunar_spellbook") {
    player.approachRange(2)
    pause()
    val spell = component
    if (!target.poisoned) {
        player.message("This player is not poisoned.")
        return@spellOnPlayerApproach
    }
    if (!Spell.removeRequirements(player, spell)) {
        return@spellOnPlayerApproach
    }
    val definition = definitions.get(spell)
    player.start("movement_delay", 2)
    player.setAnimation("lunar_cast")
    target.setGraphic(spell)
    player.experience.add(Skill.Magic, definition.experience)
    target.curePoison()
    target.message("You have been cured by ${player.name}.")
}
