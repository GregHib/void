package world.gregs.voidps.world.activity.transport.teleport

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.engine.suspend.playAnimation
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.Spell
import java.util.concurrent.TimeUnit

val areas: AreaDefinitions by inject()

interfaceOption("Cast", "lumbridge_home_teleport", "modern_spellbook") {
    val seconds = player.remaining("home_teleport_timeout", epochSeconds())
    if (seconds > 0) {
        val remaining = TimeUnit.SECONDS.toMinutes(seconds.toLong())
        player.message("You have to wait $remaining ${"minute".plural(remaining)} before trying this again.")
        return@interfaceOption
    }
    if (player.hasClock("teleport_delay")) {
        return@interfaceOption
    }
    player.weakQueue("home_teleport") {
        if (!Spell.removeRequirements(player, component)) {
            cancel()
            return@weakQueue
        }
        onCancel = {
            player.start("teleport_delay", 1)
        }
        player.start("teleport_delay", 17)
        repeat(17) {
            player.setGraphic("home_tele_${it + 1}")
            player.playAnimation("home_tele_${it + 1}")
        }
        withContext(NonCancellable) {
            player.tele(areas["lumbridge_teleport"].random())
            player.start("home_teleport_timeout", TimeUnit.MINUTES.toSeconds(30).toInt(), epochSeconds())
        }
    }
}
