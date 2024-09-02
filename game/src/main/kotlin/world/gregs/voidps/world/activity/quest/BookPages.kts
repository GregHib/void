package world.gregs.voidps.world.activity.quest

import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.dialogue.continueDialogue
import world.gregs.voidps.engine.client.ui.event.interfaceRefresh
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.player.equip.inventoryOption

val books: Books by inject()

inventoryOption("Read") {
    player.openBook(item.def.getOrNull("book") ?: return@inventoryOption)
}

interfaceRefresh("book", "book_long") { player ->
    refreshBook(player, id)
}

continueDialogue("book", "turn_page_right") { player ->
    player.inc("book_page")
    player.close("book")
    player.open("book")
}

continueDialogue("book", "turn_page_left") { player ->
    player.dec("book_page")
    player.close("book")
    player.open("book")
}

continueDialogue("book_long", "turn_page_right") { player ->
    player.inc("book_page")
    player.close("book_long")
    player.open("book_long")
}

continueDialogue("book_long", "turn_page_left") { player ->
    player.dec("book_page")
    player.close("book_long")
    player.open("book_long")
}

fun refreshBook(player: Player, book: String) {
    val name: String = player["book"] ?: return
    val pageNumber: Int = player["book_page"] ?: return
    player.interfaces.apply {
        sendText(book, "title", books.title(name))
        sendText(book, "page_number_left", (pageNumber + 1).toString())
        sendText(book, "page_number_right", (pageNumber + 2).toString())
        val pages = books.get(name)
        sendVisibility(book, "turn_page_left", pageNumber > 0)
        sendVisibility(book, "turn_page_right", pageNumber < pages.lastIndex)
        val page = pages.getOrNull(pageNumber)?.lines()
        for (i in 0 until if (book == "book_long") 30 else 21) {
            sendText(book, "line${i + 1}", page?.getOrNull(i) ?: "")
        }
    }
}