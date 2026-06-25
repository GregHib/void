package content.skill.magic.book.lunar

import content.skill.magic.spell.removeSpellItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound

class StatSpy : Script {

    init {
        interfaceOption("Close", "player_stat_spy:close") {
            close("player_stat_spy")
        }

        onPlayerApproach("lunar_spellbook:stat_spy") { (target) ->
            approachRange(8)
            if (hasClock("action_delay")) {
                return@onPlayerApproach
            }
            if (!target["accept_aid", true]) {
                message("This player is not currently accepting aid.")
                return@onPlayerApproach
            }
            if (!removeSpellItems("stat_spy")) {
                return@onPlayerApproach
            }
            start("action_delay", 2)
            anim("lunar_examine")
            gfx("stat_spy")
            target.gfx("stat_spy_target")
            sound("stat_spy")
            target.sound("stat_spy_impact")
            exp(Skill.Magic, Tables.int("spells.stat_spy.xp") / 10.0)
            open("player_stat_spy")
            clear("spell")
            for (skill in Skill.all) {
                val name = name(skill)
                // Constitution is stored as lifepoints (x10); the interface shows levels
                val divisor = if (skill == Skill.Constitution) 10 else 1
                interfaces.sendText("player_stat_spy", "${name}_current", (target.levels.get(skill) / divisor).toString())
                interfaces.sendText("player_stat_spy", "${name}_base", (target.levels.getMax(skill) / divisor).toString())
            }
            interfaces.sendText("player_stat_spy", "name", target.name)
        }
    }

    private fun name(skill: Skill): String = skill.name.lowercase()
}
