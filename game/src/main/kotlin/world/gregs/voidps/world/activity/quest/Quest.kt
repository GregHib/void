package world.gregs.voidps.world.activity.quest

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item

val quests = listOf(
    "unstable_foundations",
    "cooks_assistant",
    "demon_slayer",
    "dorics_quest",
    "gunnars_ground",
    "the_restless_ghost",
    "rune_mysteries",
    "the_knights_sword",
    // mini-quests
    "enter_the_abyss",
)

fun Player.quest(name: String): String = this[name, "unstarted"]

fun Player.questComplete(name: String): Boolean = quest(name) == "completed"

fun Player.refreshQuestJournal() {
    sendScript("quest_journal_refresh")
}

private const val QUEST_SCROLL_ID = "quest_scroll"

fun Player.sendQuestJournal(name: String, lines: List<String>) {
    if (!interfaces.open(QUEST_SCROLL_ID)) {
        return
    }
    sendScript("quest_journal_length", lines.size + 1)
    interfaces.sendText(QUEST_SCROLL_ID, "quest_name", name)
    interfaces.sendText(QUEST_SCROLL_ID, "line0", "")
    for (i in 0..301) {
        interfaces.sendText(QUEST_SCROLL_ID, "line${i + 1}", lines.getOrNull(i) ?: "")
    }
}

fun Player.sendQuestComplete(name: String, lines: List<String>, item: Item = Item.EMPTY) {
    open("quest_complete")
    interfaces.sendText("quest_complete", "quest_name", "You have completed $name!")
    interfaces.sendText("quest_complete", "quest_points", get("quest_points", 0).toString())
    if (item != Item.EMPTY) {
        interfaces.sendItem("quest_complete", "item_slot", item)
    }
    for (i in 0 until 8) {
        interfaces.sendText("quest_complete", "line${i + 1}", lines.getOrNull(i) ?: "")
    }
}

fun Player.sendLetterScroll(name: String, lines: List<String>) {
    if (!interfaces.open("letter_scroll")) {
        return
    }
    sendScript("message_scroll_max", lines.size + 1)
    interfaces.sendText("letter_scroll", "title", name)
    interfaces.sendText("letter_scroll", "line0", "")
    for (i in 0..87) {
        interfaces.sendText("letter_scroll", "line${i + 1}", lines.getOrNull(i) ?: "")
    }
}

fun Player.sendWomScroll(name: String, lines: List<String>) {
    if (!interfaces.open("wise_old_man_scroll")) {
        return
    }
    interfaces.sendText("wise_old_man_scroll", "title", name)
    for (i in 0..16) {
        interfaces.sendText("wise_old_man_scroll", "line${i + 1}", lines.getOrNull(i) ?: "")
    }
}

fun Player.sendMessageScroll(lines: List<String>, handwriting: Boolean = false) {
    val id = "message_scroll${if (handwriting) "_handwriting" else ""}"
    if (!interfaces.open(id)) {
        return
    }
    for (i in 0..if (handwriting) 10 else 14) {
        interfaces.sendText(id, "line${i}", lines.getOrNull(i) ?: "")
    }
}