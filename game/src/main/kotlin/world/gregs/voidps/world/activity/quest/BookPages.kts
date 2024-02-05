package world.gregs.voidps.world.activity.quest

import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.dialogue.continueDialogue
import world.gregs.voidps.engine.client.ui.event.interfaceRefresh
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.player.equip.inventory

val books: Books by inject()

inventory({ item.def.contains("book") && option == "Read" }) { player: Player ->
    player.openBook(item.def["book"])
}

interfaceRefresh("book") { player: Player ->
    refreshBook(player)
}

continueDialogue({ id == "book" && component == "turn_page_right" }) { player: Player ->
    player.inc("book_page")
    player.close("book")
    player.open("book")
}

continueDialogue({ id == "book" && component == "turn_page_left" }) { player: Player ->
    player.dec("book_page")
    player.close("book")
    player.open("book")
}

fun refreshBook(player: Player) {
    val name: String = player["book"] ?: return
    val pageNumber: Int = player["book_page"] ?: return
    player.interfaces.apply {
        sendText("book", "title", books.title(name))
        sendText("book", "page_number_left", (pageNumber + 1).toString())
        sendText("book", "page_number_right", (pageNumber + 2).toString())
        val pages = books.get(name)
        sendVisibility("book", "turn_page_left", pageNumber > 0)
        sendVisibility("book", "turn_page_right", pageNumber < pages.lastIndex)
        val page = pages.getOrNull(pageNumber)?.lines()
        for (i in 0 until 30) {
            sendText("book", "line${i + 1}", page?.getOrNull(i) ?: "")
        }
    }
}