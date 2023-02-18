package world.gregs.voidps.world.activity.combat.consume.drink

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.WarningRed
import world.gregs.voidps.engine.client.variable.decVar
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.suspend.pause
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerStop
import world.gregs.voidps.engine.timer.TimerTick
import world.gregs.voidps.world.activity.combat.consume.Consumable
import world.gregs.voidps.world.activity.combat.consume.Consume
import world.gregs.voidps.world.interact.entity.combat.hit

fun inWilderness() = false

on<Consumable>({ item.id.startsWith("overload") }) { player: Player ->
    if (player.timers.contains("overload")) {
        player.message("You may only use this potion every five minutes.")
        cancel()
    } else if (player.levels.get(Skill.Constitution) < 500) {
        player.message("You need more than 500 life points to survive the power of overload.")
        cancel()
    }
}

on<Consume>({ item.id.startsWith("overload") }) { player: Player ->
    player.setVar("overload_refreshes_remaining", 20)
    player.timers.start("overload")
}

on<Registered>({ it.getVar("overload_refreshes_remaining", 0) > 0 }) { player: Player ->
    player.timers.restart("overload")
}

on<TimerStart>({ timer == "overload" }) { _: Player ->
    interval = 25
}

on<TimerStart>({ timer == "overload" && !restart }) { player: Player ->
    player.queue {
        repeat(5) {
            hit(player, player, 100)
            player.setAnimation("overload")
            player.setGraphic("overload")
            pause(2)
        }
    }
}

on<TimerTick>({ timer == "overload" }) { player: Player ->
    if (player.decVar("overload_refreshes_remaining") <= 0) {
        return@on cancel()
    }
    if (inWilderness()) {
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

on<TimerStop>({ timer == "overload" }) { player: Player ->
    reset(player, Skill.Attack)
    reset(player, Skill.Strength)
    reset(player, Skill.Defence)
    reset(player, Skill.Magic)
    reset(player, Skill.Ranged)
    player.levels.restore(Skill.Constitution, 500)
    player.message(WarningRed { "The effects of overload have worn off and you feel normal again." })
    player.setVar("overload_refreshes_remaining", 0)
}

fun reset(player: Player, skill: Skill) {
    if (player.levels.getOffset(skill) > 0) {
        player.levels.clear(skill)
    }
}