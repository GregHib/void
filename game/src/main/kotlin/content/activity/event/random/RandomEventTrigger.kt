package content.activity.event.random

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message

/**
 * Rolls for a random event on each experience drop, so only active players are targeted.
 * The persisted cooldown clock starts on first login and restarts whenever an event
 * begins or ends, keeping events infrequent regardless of experience rates.
 */
class RandomEventTrigger : Script {

    init {
        experience { _, _, _ ->
            RandomEvents.roll(this)
        }

        adminCommand("randomevent", stringArg("event", optional = true), desc = "Start a random event") { args ->
            val event = args.getOrNull(0) ?: RandomEvents.pick()
            if (event == null || !RandomEvents.start(this, event)) {
                message("No random event found${if (args.isEmpty()) "" else " for '${args[0]}'"}.")
            }
        }
    }
}
