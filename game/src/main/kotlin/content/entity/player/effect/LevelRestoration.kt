package content.entity.player.effect

import content.skill.prayer.praying
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerTick
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

@Script
class LevelRestoration : Api {

    val skills = Skill.all.filterNot { it == Skill.Prayer || it == Skill.Summoning || it == Skill.Constitution }

    override fun levelChanged(player: Player, skill: Skill, from: Int, to: Int) {
        if (skill == Skill.Prayer || skill == Skill.Summoning || skill == Skill.Constitution) {
            return
        }
        if (to == player.levels.getMax(skill) || player.softTimers.contains("restore_stats")) {
            return
        }
        player.softTimers.start("restore_stats")
    }

    override fun spawn(player: Player) {
        if (skills.any { player.levels.getOffset(it) != 0 }) {
            player.softTimers.start("restore_stats")
        }
    }

    init {
        timerStart("restore_stats") {
            interval = TimeUnit.SECONDS.toTicks(60)
        }

        timerTick("restore_stats") { player ->
            val berserker = player.praying("berserker") && player.hasClock("berserker_cooldown")
            val skip = player.praying("berserker") && !player.hasClock("berserker_cooldown")
            if (skip) {
                nextInterval = TimeUnit.SECONDS.toTicks(9)
                player.start("berserker_cooldown", nextInterval + 1)
            }
            var fullyRestored = true
            for (skill in skills) {
                val offset = player.levels.getOffset(skill)
                if (offset != 0) {
                    fullyRestored = false
                }
                if (offset > 0 && !skip) {
                    player.levels.drain(skill, 1)
                } else if (offset < 0 && !berserker) {
                    val restore = if (player.praying("rapid_restore")) 2 else 1
                    player.levels.restore(skill, restore)
                }
            }
            if (fullyRestored) {
                cancel()
            }
        }
    }
}
