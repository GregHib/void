package world.gregs.voidps.world.activity.combat.consume.drink

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerStop
import world.gregs.voidps.engine.timer.TimerTick
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.world.activity.combat.consume.Consumable
import world.gregs.voidps.world.activity.combat.consume.Consume
import world.gregs.voidps.world.interact.entity.player.combat.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.specialAttackEnergy
import java.util.concurrent.TimeUnit

on<Consumable>({ item.id.startsWith("recover_special") }) { player: Player ->
    if (player.softTimers.contains("recover_special_delay")) {
        player.message("You may only use this pot once every 30 seconds.")
        cancel()
    }
}

on<Consume>({ item.id.startsWith("recover_special") }) { player: Player ->
    player.specialAttackEnergy = (MAX_SPECIAL_ATTACK / 100) * 25
    val percentage = (player.specialAttackEnergy / MAX_SPECIAL_ATTACK) * 100
    if (percentage == 0) {
        player.message("Your special attack energy is now $percentage%.")
    }
    player["recover_special_delay"] = TimeUnit.SECONDS.toTicks(30) / 10
    player.softTimers.start("recover_special")
}

on<TimerStart>({ timer == "recover_special" }) { _: Player ->
    interval = 10
}

on<TimerTick>({ timer == "recover_special" }) { player: Player ->
    if (player.dec("recover_special_delay") <= 0) {
        return@on cancel()
    }
}

on<TimerStop>({ timer == "recover_special" }) { player: Player ->
    player.clear("recover_special_delay")
}