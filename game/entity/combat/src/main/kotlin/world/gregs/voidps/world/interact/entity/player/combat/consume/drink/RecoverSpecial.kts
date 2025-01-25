package world.gregs.voidps.world.interact.entity.player.combat.consume.drink

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerStop
import world.gregs.voidps.engine.timer.timerTick
import world.gregs.voidps.world.interact.entity.player.combat.consume.canConsume

canConsume("recover_special*") { player ->
    if (player.softTimers.contains("recover_special_delay")) {
        player.message("You may only use this pot once every 30 seconds.")
        cancel()
    }
}

timerStart("recover_special") {
    interval = 10
}

timerTick("recover_special") { player ->
    if (player.dec("recover_special_delay") <= 0) {
        cancel()
        return@timerTick
    }
}

timerStop("recover_special") { player ->
    player.clear("recover_special_delay")
}