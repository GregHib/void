package content.skill.magic.book.lunar

import content.entity.combat.hit.damage
import content.skill.magic.spell.removeSpellItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound

class HealOther : Script {

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
            val amount = (levels.get(Skill.Constitution) * 0.75).toInt() + 1
            start("movement_delay", 2)
            anim("lunar_cast")
            sound("heal_other")
            target.gfx("heal_other")
            target.sound("heal_other_impact")
            exp(Skill.Magic, Tables.int("spells.heal_other.xp") / 10.0)
            val restored = target.levels.restore(Skill.Constitution, amount)
            target.message("You have been healed by $name.")
            damage(restored, delay = 2)
        }
    }
}
