package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.lunar

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnPlayerApproach
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.suspend.approachRange
import world.gregs.voidps.engine.suspend.pause
import world.gregs.voidps.world.interact.entity.combat.hit.damage
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.Spell

val definitions: SpellDefinitions by inject()

itemOnPlayerApproach(id = "lunar_spellbook", component = "heal_other") {
    player.approachRange(2)
    pause()
    val spell = component
    if (target.levels.getOffset(Skill.Constitution) >= 0) {
        player.message("This player does not need healing.")
        return@itemOnPlayerApproach
    }
    if (player.levels.get(Skill.Constitution) < player.levels.getMax(Skill.Constitution) * 0.11) {
        player.message("You don't have enough life points.")
        return@itemOnPlayerApproach
    }
    if (!Spell.removeRequirements(player, spell)) {
        return@itemOnPlayerApproach
    }
    val definition = definitions.get(spell)
    val amount = (player.levels.get(Skill.Constitution) * 0.75).toInt() + 1
    player.start("movement_delay", 2)
    player.setAnimation("lunar_cast")
    target.setGraphic(spell)
    player.experience.add(Skill.Magic, definition.experience)
    val restored = target.levels.restore(Skill.Constitution, amount)
    target.message("You have been healed by ${player.name}.")
    player.damage(restored, delay = 2)
}
