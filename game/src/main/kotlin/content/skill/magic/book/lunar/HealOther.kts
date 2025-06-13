package content.skill.magic.book.lunar

import content.entity.combat.hit.damage
import content.entity.sound.sound
import content.skill.magic.spell.removeSpellItems
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.interfaceOnPlayerApproach
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inject

val definitions: SpellDefinitions by inject()

interfaceOnPlayerApproach(id = "lunar_spellbook", component = "heal_other") {
    approachRange(2)
    val spell = component
    if (target.levels.getOffset(Skill.Constitution) >= 0) {
        player.message("This player does not need healing.")
        return@interfaceOnPlayerApproach
    }
    if (player.levels.get(Skill.Constitution) < player.levels.getMax(Skill.Constitution) * 0.11) {
        player.message("You don't have enough life points.")
        return@interfaceOnPlayerApproach
    }
    if (!player["accept_aid", true]) {
        player.message("This player is not currently accepting aid.") // TODO proper message
        return@interfaceOnPlayerApproach
    }
    if (!player.removeSpellItems(spell)) {
        return@interfaceOnPlayerApproach
    }
    val definition = definitions.get(spell)
    val amount = (player.levels.get(Skill.Constitution) * 0.75).toInt() + 1
    player.start("movement_delay", 2)
    player.anim("lunar_cast")
    player.sound(spell)
    target.gfx(spell)
    target.sound("heal_other_impact")
    player.experience.add(Skill.Magic, definition.experience)
    val restored = target.levels.restore(Skill.Constitution, amount)
    target.message("You have been healed by ${player.name}.")
    player.damage(restored, delay = 2)
}
