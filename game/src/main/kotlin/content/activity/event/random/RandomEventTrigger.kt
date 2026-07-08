package content.activity.event.random

import content.bot.isBot
import content.entity.combat.inCombat
import content.quest.instance
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.type.random

/**
 * Rolls for a random event on each experience drop, so only active players are targeted.
 * The persisted cooldown clock starts on first login and restarts whenever an event
 * begins or ends, keeping events infrequent regardless of experience rates.
 */
class RandomEventTrigger : Script {

    init {
        experience { _, _, _ ->
            if (!contains("random_event_cooldown")) {
                // First activity; events unlock once the initial cooldown expires
                RandomEvents.cooldown(this)
                return@experience
            }
            if (eligible(this) && random.nextInt(Tables.int("random_event_settings.roll_chance.value")) == 0) {
                // Restart the cooldown immediately so a failed pick can't re-roll every drop
                RandomEvents.cooldown(this)
                summonOldMan(this)
            }
        }

        adminCommand("randomevent", stringArg("event", optional = true), desc = "Start a random event") { args ->
            val event = args.getOrNull(0) ?: RandomEvents.pick()
            if (event == null || !summonOldMan(this, event)) {
                message("No random event found${if (args.isEmpty()) "" else " for '${args[0]}'"}.")
            }
        }
    }

    private fun eligible(player: Player): Boolean = !player.isBot &&
        !player.contains("random_event") &&
        player.instance() == null &&
        !player.inCombat &&
        player.menu == null &&
        player.dialogue == null &&
        player.mode == EmptyMode &&
        !player.contains("delay") &&
        !player.hasClock("random_event_cooldown", epochSeconds()) &&
        Areas.get(player.tile.zone).none { it.tags.contains("no_random_events") }
}
