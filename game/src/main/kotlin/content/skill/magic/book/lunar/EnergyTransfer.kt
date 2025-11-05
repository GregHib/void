package content.skill.magic.book.lunar

import content.area.wilderness.inMultiCombat
import content.entity.combat.hit.damage
import content.entity.player.combat.special.MAX_SPECIAL_ATTACK
import content.entity.player.combat.special.specialAttackEnergy
import content.entity.player.effect.energy.MAX_RUN_ENERGY
import content.entity.player.effect.energy.runEnergy
import world.gregs.voidps.engine.entity.character.sound
import content.skill.magic.spell.removeSpellItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.random

class EnergyTransfer : Script {

    val definitions: SpellDefinitions by inject()

    init {
        onPlayerApproach("lunar_spellbook:energy_transfer") { (target) ->
            approachRange(2)
            if (target.specialAttackEnergy == MAX_SPECIAL_ATTACK) {
                message("This player has full special attack.")
                return@onPlayerApproach
            }
            if (specialAttackEnergy != MAX_SPECIAL_ATTACK) {
                message("You must have 100% special attack energy to transfer.")
                return@onPlayerApproach
            }
            if (levels.get(Skill.Constitution) < 100) {
                message("You need more hitpoints to cast this spell.")
                return@onPlayerApproach
            }
            if (!target.inMultiCombat) {
                message("This player is not in a multi-combat zone.")
                return@onPlayerApproach
            }
            if (!get("accept_aid", true)) {
                message("This player is not currently accepting aid.") // TODO proper message
                return@onPlayerApproach
            }
            if (!removeSpellItems("energy_transfer")) {
                return@onPlayerApproach
            }
            val definition = definitions.get("energy_transfer")
            start("movement_delay", 2)
            anim("lunar_cast")
            target.gfx("energy_transfer")
            sound("energy_transfer")
            experience.add(Skill.Magic, definition.experience)
            damage(random.nextInt(95, 100))
            specialAttackEnergy = 0
            target.specialAttackEnergy = MAX_SPECIAL_ATTACK
            target.runEnergy = MAX_RUN_ENERGY
        }
    }
}
