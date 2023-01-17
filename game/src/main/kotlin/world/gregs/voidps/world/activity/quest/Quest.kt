package world.gregs.voidps.world.activity.quest

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.entity.character.player.Player

fun Player.completed(name: String): Boolean = false

fun Player.started(name: String): Boolean = false

fun Player.refreshQuestJournal() {
    sendScript(2165)
}


private const val QUEST_DETAILS_ID = "quest_scroll"

fun Interfaces.questDetails(name: String, vararg lines: String) {
    if (!open(QUEST_DETAILS_ID)) {
        return
    }
    sendText(QUEST_DETAILS_ID, "quest_name", name)
    for (i in 1..300) {
        sendText(QUEST_DETAILS_ID, "textline$i", lines.getOrNull(i) ?: "")
    }
}