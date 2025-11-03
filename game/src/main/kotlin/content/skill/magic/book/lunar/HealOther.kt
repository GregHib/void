package content.skill.magic.book.lunar

import content.entity.combat.hit.damage
import content.entity.sound.sound
import content.skill.magic.spell.removeSpellItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inject

class HealOther : Script {

    val definitions: SpellDefinitions by inject()

    init {
        onPlayerApproach("lunar_spellbook:heal_other") { (target) ->
            approachRange(2)
            if (target.levels.getOffset(Skill.Constitution) >= 0) {
                message("This player does not need healing.")
                return@onPlayerApproach
            }
            if (levels.get(Skill.Constitution) < levels.getMax(Skill.Constitution) * 0.11) {
                message("You don't have enough life points.")
                return@onPlayerApproach
            }
            if (!get("accept_aid", true)) {
                message("This player is not currently accepting aid.") // TODO proper message
                return@onPlayerApproach
            }
            if (!removeSpellItems("heal_other")) {
                return@onPlayerApproach
            }
            val definition = definitions.get("heal_other")
            val amount = (levels.get(Skill.Constitution) * 0.75).toInt() + 1
            start("movement_delay", 2)
            anim("lunar_cast")
            sound("heal_other")
            target.gfx("heal_other")
            target.sound("heal_other_impact")
            experience.add(Skill.Magic, definition.experience)
            val restored = target.levels.restore(Skill.Constitution, amount)
            target.message("You have been healed by $name.")
            damage(restored, delay = 2)
        }
    }
}
