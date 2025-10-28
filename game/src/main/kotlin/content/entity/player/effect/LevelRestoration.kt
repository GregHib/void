package content.entity.player.effect

import content.skill.prayer.praying
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.timer.*
import java.util.concurrent.TimeUnit

@Script
class LevelRestoration : Api {

    val skills = Skill.all.filterNot { it == Skill.Prayer || it == Skill.Summoning || it == Skill.Constitution }

    init {
        playerSpawn { player ->
            if (skills.any { player.levels.getOffset(it) != 0 }) {
                player.softTimers.start("restore_stats")
            }
        }

        for (skill in skills) {
            levelChanged(skill, ::startRestore)
        }

        timerStart("restore_stats") { TimeUnit.SECONDS.toTicks(60) }
        timerTick("restore_stats", ::restore)
    }

    fun startRestore(player: Player, skill: Skill, from: Int, to: Int) {
        if (to == player.levels.getMax(skill) || player.softTimers.contains("restore_stats")) {
            return
        }
        player.softTimers.start("restore_stats")
    }

    fun restore(player: Player): Int {
        val berserker = player.praying("berserker") && player.hasClock("berserker_cooldown")
        val skip = player.praying("berserker") && !player.hasClock("berserker_cooldown")
        var nextInterval = Timer.CONTINUE
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
            return Timer.CANCEL
        }
        return nextInterval
    }
}
