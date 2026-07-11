package content.activity.event.random

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.playerCommand
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

/**
 * Rolls for a random event on each experience drop, so only active players are targeted.
 * The persisted cooldown clock starts on first login and restarts whenever an event
 * begins or ends, keeping events infrequent regardless of experience rates.
 */
class RandomEventTrigger : Script {

    init {
        playerSpawn {
            if (!Settings["events.randomEvents.active", true]) {
                return@playerSpawn
            }
            if (RandomEvents.optedOut(this)) {
                return@playerSpawn
            }
            timers.startIfAbsent("random_event")
        }

        timerStart("random_event") { TimeUnit.MINUTES.toTicks(5) }

        timerTick("random_event") {
            RandomEvents.roll(this)
            Timer.CONTINUE
        }

        playerCommand("random_events", desc = "Toggle random events on or off") {
            if (!Settings["events.randomEvents.optOut", false]) {
                message("Random events can't be turned off on this world.")
                return@playerCommand
            }
            if (toggle("random_events_disabled")) {
                message("Random events are now off.")
            } else {
                message("Random events are now on.")
            }
        }

        adminCommand("random_event", stringArg("event", optional = true), desc = "Start a random event") { args ->
            val event = args.getOrNull(0) ?: RandomEvents.pick()
            if (event == null || !RandomEvents.start(this, event)) {
                message("No random event found${if (args.isEmpty()) "" else " for '${args[0]}'"}.")
            }
        }
    }
}
