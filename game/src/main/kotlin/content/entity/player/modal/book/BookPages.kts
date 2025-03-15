package content.entity.player.modal.book

import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.dialogue.continueDialogue
import world.gregs.voidps.engine.client.ui.event.interfaceRefresh
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inject
import content.entity.player.inv.inventoryOption

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
    val page: Int = player["book_page"] ?: return
    val pages = books.get(name)
    player.interfaces.display(book, books.title(book), page, pages)
}

fun Interfaces.display(book: String, title: String, pageNumber: Int, pages: List<List<String>>) {
    sendText(book, "title", title)
    sendText(book, "page_number_left", (pageNumber + 1).toString())
    sendText(book, "page_number_right", (pageNumber + 2).toString())
    sendVisibility(book, "turn_page_left", pageNumber > 0)
    sendVisibility(book, "turn_page_right", pageNumber < pages.lastIndex)
    val lines = pages.getOrNull(pageNumber)
    for (i in 0 until if (book == "book_long") 30 else 21) {
        println("Send line${i + 1} ${lines?.getOrNull(i)}")
        sendText(book, "line${i + 1}", lines?.getOrNull(i) ?: "")
    }
}