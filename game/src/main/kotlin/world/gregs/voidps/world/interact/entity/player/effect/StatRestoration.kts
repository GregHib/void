package world.gregs.voidps.world.interact.entity.player.effect

import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.CurrentLevelChanged
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerTick
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.world.interact.entity.player.combat.prayer.praying
import java.util.concurrent.TimeUnit

val skills = Skill.all.filterNot { it == Skill.Prayer || it == Skill.Summoning || it == Skill.Constitution }

on<Registered>({ player -> skills.any { player.levels.getOffset(it) != 0 } }) { player: Player ->
    player.softTimers.start("restore_stats")
}

on<CurrentLevelChanged>({ skill != Skill.Prayer && skill != Skill.Summoning && skill != Skill.Constitution && to != it.levels.getMax(skill) && !it.softTimers.contains("restore_stats") }) { player: Player ->
    player.softTimers.start("restore_stats")
}

on<TimerStart>({ timer == "restore_stats" }) { _: Player ->
    interval = TimeUnit.SECONDS.toTicks(60)
}

on<TimerTick>({ timer == "restore_stats" }) { player: Player ->
    val berserker = player.praying("berserker") && player.hasClock("berserker_cooldown")
    val skip = player.praying("berserker") && !player.hasClock("berserker_cooldown")
    if (skip) {
        nextInterval = TimeUnit.SECONDS.toTicks(9)
        player.start("berserker_cooldown", nextInterval)
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