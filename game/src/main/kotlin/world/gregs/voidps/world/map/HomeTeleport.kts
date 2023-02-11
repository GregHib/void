import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.remaining
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.engine.suspend.playAnimation
import world.gregs.voidps.engine.utility.TICKS
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.engine.utility.toTicks
import world.gregs.voidps.world.interact.entity.player.combat.magic.Runes.hasSpellRequirements
import java.util.concurrent.TimeUnit

val areas: Areas by inject()

on<InterfaceOption>({ id == "modern_spellbook" && component == "lumbridge_home_teleport" && option == "Cast" }) { player: Player ->
    if (player.hasEffect("home_teleport_timeout")) {
        val remaining = TICKS.toMinutes(player.remaining("home_teleport_timeout"))
        player.message("You have to wait $remaining ${"minute".plural(remaining)} before trying this again.")
        return@on
    }
    if (player.hasEffect("teleport_delay")) {
        return@on
    }
    player.weakQueue {
        if (!hasSpellRequirements(player, component)) {
            cancel()
            return@weakQueue
        }
        try {
            player.start("teleport_delay", 17)
            repeat(17) {
                player.setGraphic("home_tele_${it + 1}")
                player.playAnimation("home_tele_${it + 1}")
            }
            withContext(NonCancellable) {
                val lumbridge = areas.getValue("lumbridge_teleport")
                player.tele(lumbridge.area.random())
                player.start("home_teleport_timeout", TimeUnit.MINUTES.toTicks(30), persist = true)
            }
        } finally {
            player.start("teleport_delay", ticks = 1, quiet = true)
        }
    }
}