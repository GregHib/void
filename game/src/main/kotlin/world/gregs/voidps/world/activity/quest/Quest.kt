package world.gregs.voidps.world.activity.quest

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item

fun Player.quest(name: String): String = this[name, "unstarted"]

fun Player.questComplete(name: String): Boolean = quest(name) == "completed"

fun Player.refreshQuestJournal() {
    sendScript(2165)
}

private const val QUEST_SCROLL_ID = "quest_scroll"

fun Player.sendQuestJournal(name: String, lines: List<String>) {
    if (!interfaces.open(QUEST_SCROLL_ID)) {
        return
    }
    sendScript(1207, lines.size + 1)
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