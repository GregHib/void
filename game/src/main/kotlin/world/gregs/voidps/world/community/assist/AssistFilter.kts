package world.gregs.voidps.world.community.assist

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.closeInterface
import world.gregs.voidps.engine.client.variable.getOrNull
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.hasVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.community.assist.Assistance.getHoursRemaining
import world.gregs.voidps.world.community.assist.Assistance.hasEarnedMaximumExperience

/**
 * Assistance privacy filter settings
 */

on<InterfaceOption>({ id == "filter_buttons" && component == "assist" && option == "XP Earned/Time" }) { player: Player ->
    if (hasEarnedMaximumExperience(player)) {
        val hours = getHoursRemaining(player)
        player.message(
            "You've earned the maximum XP (30,000 Xp) from the Assist System within a 24-hour period.",
            ChatType.Assist
        )
        player.message("You can assist again in $hours ${"hour".plural(hours)}.", ChatType.Assist)
    } else {
        val earned = player.getVar("total_xp_earned", 0.0)
        player.message("You have earned $earned Xp. The Assist system is available to you.", ChatType.Assist)
    }
}

on<InterfaceOption>({ id == "filter_buttons" && component == "assist" && option == "On Assist" }) { player: Player ->
    player.setVar("assist_status", "on")
}

on<InterfaceOption>({ id == "filter_buttons" && component == "assist" && option == "Friends Assist" }) { player: Player ->
    player.setVar("assist_status", "friends")
    cancel(player)
}

on<InterfaceOption>({ id == "filter_buttons" && component == "assist" && option == "Off Assist" }) { player: Player ->
    player.setVar("assist_status", "off")
    cancel(player)
}

fun cancel(player: Player) {
    if (player.hasVar("assistant")) {
        val assistant: Player? = player.getOrNull("assistant")
        assistant?.closeInterface()
    }

    if (player.hasVar("assisted")) {
        player.closeInterface()
    }
}