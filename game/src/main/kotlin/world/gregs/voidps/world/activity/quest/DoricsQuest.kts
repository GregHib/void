package world.gregs.voidps.world.activity.quest

import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

on<InterfaceOption>({ id == "quest_journals" && component == "journals" && itemSlot == 3 }) { player: Player ->
    val lines = when (player["dorics_quest", "unstarted"]) {
        "completed" -> listOf(
            "<str>I have spoken to Doric.",
            "",
            "<str>I have collected some Clay, Copper Ore, and Iron Ore.",
            "",
            "<str>Doric rewarded me for all my hard work.",
            "<str>I can now use Doric's Anvils whenever I want.",
            "<red>QUEST COMPLETE!"
        )
        "started" -> listOf(
            "<str>I have spoken to Doric.",
            "",
            "<navy>I need to collect some items and bring them to <maroon>Doric.",
            requiredItem(player, "clay", 6),
            requiredItem(player, "copper_ore", 4),
            requiredItem(player, "iron_ore", 2),
        )
        else -> listOf(
            "<navy>I can start this quest by speaking to <maroon>Doric<navy> who is <maroon>North of",
            "<maroon>Falador",
            "",
            "<navy>There aren't any requirements but <maroon>Level 15 Mining<navy> will help"
        )
    }
    player.sendQuestJournal("Doric's Quest", lines)
}

fun requiredItem(player: Player, item: String, required: Int): String {
    val count = player.inventory.count(item)
    return buildString {
        if (count >= required) {
            append("<str>")
        } else {
            append("<maroon>")
        }
        append("$required ${item.toTitleCase()}")
        if (count < required) {
            append("<navy> - I need ${required - count} more")
        }
        append(".")
    }
}