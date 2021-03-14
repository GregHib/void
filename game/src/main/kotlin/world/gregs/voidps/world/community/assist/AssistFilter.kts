import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.entity.character.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.utility.func.plural
import world.gregs.voidps.world.community.assist.Assistance.getHoursRemaining
import world.gregs.voidps.world.community.assist.Assistance.hasEarnedMaximumExperience
import world.gregs.voidps.world.interact.entity.player.display.InterfaceOption

/**
 * Assistance privacy filter settings
 */

InterfaceOption where { name == "filter_buttons" && component == "assist" && option == "XP Earned/Time" } then {
    if (hasEarnedMaximumExperience(player)) {
        val hours = getHoursRemaining(player)
        player.message(
            "You've earned the maximum XP (30,000 Xp) from the Assist System within a 24-hour period.",
            ChatType.GameAssist
        )
        player.message("You can assist again in $hours ${"hour".plural(hours)}.", ChatType.GameAssist)
    } else {
        val earned = player.getVar("total_xp_earned", 0.0)
        player.message("You have earned $earned Xp. The Assist system is available to you.", ChatType.GameAssist)
    }
}

InterfaceOption where { name == "filter_buttons" && component == "assist" && option == "On Assist" } then {
    player["assist_filter", true] = "on"
}

InterfaceOption where { name == "filter_buttons" && component == "assist" && option == "Friends Assist" } then {
    player["assist_filter", true] = "friends"
    cancel(player)
}

InterfaceOption where { name == "filter_buttons" && component == "assist" && option == "Off Assist" } then {
    player["assist_filter", true] = "off"
    cancel(player)
}

fun cancel(player: Player) {
    if (player.has("assistant")) {
        val assistant: Player? = player.getOrNull("assistant")
        assistant?.action?.cancel(ActionType.Assisting)
    }

    if (player.has("assisted")) {
        player.action.cancel(ActionType.Assisting)
    }
}