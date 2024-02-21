package world.gregs.voidps.world.interact.entity.player.effect

import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.levelChange
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerTick
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.world.interact.entity.player.combat.prayer.praying
import java.util.concurrent.TimeUnit

val skills = Skill.all.filterNot { it == Skill.Prayer || it == Skill.Summoning || it == Skill.Constitution }

playerSpawn { player ->
    if (skills.any { player.levels.getOffset(it) != 0 }) {
        player.softTimers.start("restore_stats")
    }
}

levelChange { player ->
    if (skill == Skill.Prayer || skill == Skill.Summoning || skill == Skill.Constitution) {
        return@levelChange
    }
    if (to == player.levels.getMax(skill) || player.softTimers.contains("restore_stats")) {
        return@levelChange
    }
    player.softTimers.start("restore_stats")
}

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