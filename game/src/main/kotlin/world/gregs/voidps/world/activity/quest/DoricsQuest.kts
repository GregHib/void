package world.gregs.voidps.world.activity.quest

import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.chat.ChatBlue
import world.gregs.voidps.engine.client.ui.chat.ChatRed
import world.gregs.voidps.engine.client.ui.chat.Red
import world.gregs.voidps.engine.client.ui.chat.Strike
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

on<InterfaceOption>({ id == "quest_journals" && component == "journals" && itemSlot == 3 }) { player: Player ->
    val lines = when (player.get("dorics_quest", "unstarted")) {
        "completed" -> listOf(
            Strike { "I have spoken to Doric." },
            "",
            Strike { "I have collected some Clay, Copper Ore, and Iron Ore." },
            "",
            Strike { "Doric rewarded me for all my hard work." },
            Strike { "I can now use Doric's Anvils whenever I want." },
            Red { "QUEST COMPLETE!" }
        )
        "started" -> listOf(
            Strike { "I have spoken to Doric." },
            "",
            ChatBlue { "I need to collect some items and bring them to ${ChatRed { "Doric." }}" },
            requiredItem(player, "clay", 6),
            requiredItem(player, "copper_ore", 4),
            requiredItem(player, "iron_ore", 2),
        )
        else -> listOf(
            ChatBlue { "I can start this quest by speaking to ${ChatRed { "Doric" }} who is ${ChatRed { "North of" }}" },
            ChatRed { "Falador" },
            "",
            ChatBlue { "There aren't any requirements but ${ChatRed { "Level 15 Mining" }} will help" }
        )
    }
    player.sendQuestJournal("Doric's Quest", lines)
}

fun requiredItem(player: Player, item: String, required: Int): String {
    val count = player.inventory.count(item)
    return Strike(count >= required, or = ChatRed) {
        buildString {
            append("$required ${item.toTitleCase()}")
            if (count < required) {
                append(ChatBlue.open(" - I need ${required - count} more"))
            }
            append(".")
        }
    }
}