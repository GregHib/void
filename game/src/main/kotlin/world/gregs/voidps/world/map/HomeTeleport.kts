package world.gregs.voidps.world.map

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.engine.suspend.playAnimation
import world.gregs.voidps.engine.timer.epochSecondRemaining
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.world.interact.entity.player.combat.magic.Runes.hasSpellRequirements
import java.util.concurrent.TimeUnit

val areas: Areas by inject()

on<InterfaceOption>({ id == "modern_spellbook" && component == "lumbridge_home_teleport" && option == "Cast" }) { player: Player ->
    val seconds = player.getVar("home_teleport_timeout", 0)
    if (seconds > epochSeconds()) {
        val remaining = TimeUnit.SECONDS.toMinutes(epochSecondRemaining(seconds).toLong())
        player.message("You have to wait $remaining ${"minute".plural(remaining)} before trying this again.")
        return@on
    }
    if (player.clocks.contains("teleport_delay")) {
        return@on
    }
    player.weakQueue {
        if (!hasSpellRequirements(player, component)) {
            cancel()
            return@weakQueue
        }
        onCancel = {
            player.clocks.start("teleport_delay", ticks = 1)
        }
        player.clocks.start("teleport_delay", 17)
        repeat(17) {
            player.setGraphic("home_tele_${it + 1}")
            player.playAnimation("home_tele_${it + 1}")
        }
        withContext(NonCancellable) {
            val lumbridge = areas.getValue("lumbridge_teleport")
            player.tele(lumbridge.area.random())
            player.setVar("home_teleport_timeout", epochSeconds() + TimeUnit.MINUTES.toSeconds(30).toInt())
        }
    }
}
