package world.gregs.voidps.world.community.assist

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.world.community.assist.Assistance.getHoursRemaining
import world.gregs.voidps.world.community.assist.Assistance.hasEarnedMaximumExperience

/**
 * Assistance privacy filter settings
 */

interfaceOption("filter_buttons", "assist", "XP Earned/Time") {
    if (hasEarnedMaximumExperience(player)) {
        val hours = getHoursRemaining(player)
        player.message(
            "You've earned the maximum XP (30,000 Xp) from the Assist System within a 24-hour period.",
            ChatType.Assist
        )
        player.message("You can assist again in $hours ${"hour".plural(hours)}.", ChatType.Assist)
    } else {
        val earned = player["total_xp_earned", 0.0]
        player.message("You have earned $earned Xp. The Assist system is available to you.", ChatType.Assist)
    }
}

interfaceOption("filter_buttons", "assist", "On Assist") {
    player["assist_status"] = "on"
}

interfaceOption("filter_buttons", "assist", "Friends Assist") {
    player["assist_status"] = "friends"
    cancel(player)
}

interfaceOption("filter_buttons", "assist", "Off Assist") {
    player["assist_status"] = "off"
    cancel(player)
}

fun cancel(player: Player) {
    if (player.contains("assistant")) {
        val assistant: Player? = player["assistant"]
        assistant?.closeMenu()
    }

    if (player.contains("assisted")) {
        player.closeMenu()
    }
}