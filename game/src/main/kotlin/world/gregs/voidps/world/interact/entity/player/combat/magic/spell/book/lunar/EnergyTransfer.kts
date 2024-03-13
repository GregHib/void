package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.lunar

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnPlayerApproach
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.suspend.approachRange
import world.gregs.voidps.engine.suspend.pause
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.hit.damage
import world.gregs.voidps.world.interact.entity.combat.inMultiCombat
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.Spell
import world.gregs.voidps.world.interact.entity.player.combat.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttackEnergy
import world.gregs.voidps.world.interact.entity.player.energy.MAX_RUN_ENERGY
import world.gregs.voidps.world.interact.entity.player.energy.runEnergy

val definitions: SpellDefinitions by inject()

itemOnPlayerApproach(id = "lunar_spellbook", component = "energy_transfer") {
    player.approachRange(2)
    pause()
    val spell = component
    if (target.specialAttackEnergy == MAX_SPECIAL_ATTACK) {
        player.message("This player has full special attack.")
        return@itemOnPlayerApproach
    }
    if (player.specialAttackEnergy != MAX_SPECIAL_ATTACK) {
        player.message("You must have 100% special attack energy to transfer.")
        return@itemOnPlayerApproach
    }
    if (player.levels.get(Skill.Constitution) < 100) {
        player.message("You need more hitpoints to cast this spell.")
        return@itemOnPlayerApproach
    }
    if (!target.inMultiCombat) {
        player.message("This player is not in a multi-combat zone.")
        return@itemOnPlayerApproach
    }
    if (!Spell.removeRequirements(player, spell)) {
        return@itemOnPlayerApproach
    }
    val definition = definitions.get(spell)
    player.start("movement_delay", 2)
    player.setAnimation("lunar_cast")
    target.setGraphic(spell)
    player.experience.add(Skill.Magic, definition.experience)
    player.damage(random.nextInt(95, 100))
    player.specialAttackEnergy = 0
    target.specialAttackEnergy = MAX_SPECIAL_ATTACK
    target.runEnergy = MAX_RUN_ENERGY
}
