package content.quest.free.dorics_quest

import content.quest.quest
import content.quest.questJournal
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory

class DoricsQuest : Script {

    init {
        questJournalOpen("dorics_quest") {
            val lines = when (quest("dorics_quest")) {
                "completed" -> listOf(
                    "<str>I have spoken to Doric.",
                    "",
                    "<str>I have collected some Clay, Copper Ore, and Iron Ore.",
                    "",
                    "<str>Doric rewarded me for all my hard work.",
                    "<str>I can now use Doric's Anvils whenever I want.",
                    "<red>QUEST COMPLETE!",
                )
                "started" -> listOf(
                    "<str>I have spoken to Doric.",
                    "",
                    "<navy>I need to collect some items and bring them to <maroon>Doric.",
                    requiredItem("clay", 6),
                    requiredItem("copper_ore", 4),
                    requiredItem("iron_ore", 2),
                )
                else -> listOf(
                    "<navy>I can start this quest by speaking to <maroon>Doric<navy> who is <maroon>North of",
                    "<maroon>Falador",
                    "",
                    "<navy>There aren't any requirements but <maroon>Level 15 Mining<navy> will help.",
                )
            }
            questJournal("Doric's Quest", lines)
        }
    }

    fun Player.requiredItem(item: String, required: Int): String {
        val count = inventory.count(item)
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
}
