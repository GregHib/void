package world.gregs.voidps.world.interact.entity.player.combat.consume.drink

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerStop
import world.gregs.voidps.engine.timer.timerTick
import world.gregs.voidps.world.interact.entity.combat.hit.directHit
import world.gregs.voidps.world.interact.entity.combat.inWilderness
import world.gregs.voidps.world.interact.entity.player.combat.consume.canConsume

canConsume("overload*") { player ->
    if (player.timers.contains("overload")) {
        player.message("You may only use this potion every five minutes.")
        cancel()
    } else if (player.levels.get(Skill.Constitution) < 500) {
        player.message("You need more than 500 life points to survive the power of overload.")
        cancel()
    }
}

playerSpawn { player ->
    if (player["overload_refreshes_remaining", 0] > 0) {
        player.timers.restart("overload")
    }
}

timerStart("overload") { player ->
    interval = 25
    if (restart) {
        return@timerStart
    }
    applyBoost(player)
    player.queue(name = "overload_hits") {
        repeat(5) {
            player.directHit(100)
            player.setAnimation("overload")
            player.setGraphic("overload")
            pause(2)
        }
    }
}

timerTick("overload") { player ->
    if (player.dec("overload_refreshes_remaining") <= 0) {
        cancel()
        removeBoost(player)
        return@timerTick
    }
}

timerStop("overload") { player ->
    player.message("<dark_red>The effects of overload have worn off and you feel normal again.")
}

fun applyBoost(player: Player) {
    if (player.inWilderness) {
        player.levels.boost(Skill.Attack, 5, 0.15)
        player.levels.boost(Skill.Strength, 5, 0.15)
        player.levels.boost(Skill.Defence, 5, 0.15)
        player.levels.boost(Skill.Magic, 5, 0.15)
        player.levels.boost(Skill.Ranged, 5, 0.15)
    } else {
        player.levels.boost(Skill.Attack, 5, 0.22)
        player.levels.boost(Skill.Strength, 5, 0.22)
        player.levels.boost(Skill.Defence, 5, 0.22)
        player.levels.boost(Skill.Magic, 7)
        player.levels.boost(Skill.Ranged, 4, 0.1923)
    }
}

fun removeBoost(player: Player) {
    val skillsToReset = listOf(
        Skill.Attack,
        Skill.Strength,
        Skill.Defence,
        Skill.Magic,
        Skill.Ranged
    )

    for (skill in skillsToReset) {
        reset(player, skill)
    }

    player.levels.restore(Skill.Constitution, 500)
}

fun reset(player: Player, skill: Skill) {
    if (player.levels.getOffset(skill) > 0) {
        player.levels.clear(skill)
    }
}