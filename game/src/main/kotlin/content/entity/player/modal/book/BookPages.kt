package content.entity.player.modal.book

import content.entity.player.inv.inventoryOption
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.dialogue.ContinueDialogue
import world.gregs.voidps.engine.client.ui.dialogue.continueDialogue
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inject

class BookPages : Script {

    val books: Books by inject()

    val turnRight: suspend ContinueDialogue.(Player) -> Unit = { player ->
        player.inc("book_page")
        player.close(id)
        player.open(id)
    }
    val turnLeft: suspend ContinueDialogue.(Player) -> Unit = { player ->
        player.dec("book_page")
        player.close(id)
        player.open(id)
    }

    init {
        inventoryOption("Read") {
            player.openBook(item.def.getOrNull("book") ?: return@inventoryOption)
        }

        interfaceRefresh("book,book_long,book_indexed") { id ->
            refreshBook(this, id)
        }

        continueDialogue("book", "turn_page_right", handler = turnRight)

        continueDialogue("book_long", "turn_page_right", handler = turnRight)

        continueDialogue("book_indexed", "turn_page_right", handler = turnRight)

        continueDialogue("book", "turn_page_left", handler = turnLeft)

        continueDialogue("book_long", "turn_page_left", handler = turnLeft)

        continueDialogue("book_indexed", "turn_page_left", handler = turnLeft)

        continueDialogue("book_indexed", "index") { player ->
            player["book_page"] = 0
            player.close(id)
            player.open(id)
        }

        continueDialogue("book_indexed", "line_click*") { player ->
            val name: String = player["book"] ?: return@continueDialogue
            val pages = books.get(name)
            val indices = pages.first()
            val index = component.removePrefix("line_click").toInt() - 1
            val selected = indices[index]
            val page = pages.indexOfFirst { it.contains("<navy>$selected") }
            player["book_page"] = page
            player.close(id)
            player.open(id)
        }

        interfaceClose("book") {
            clearAnim()
        }
    }

    fun refreshBook(player: Player, book: String) {
        val name: String = player["book"] ?: return
        val page: Int = player["book_page"] ?: return
        val pages = books.get(name)
        player.interfaces.display(book, books.title(name), page, pages)
    }

    fun Interfaces.display(book: String, title: String, pageNumber: Int, pages: List<List<String>>) {
        sendText(book, "title", title)
        sendText(book, "page_number_left", ((pageNumber * 2) + 1).toString())
        sendText(book, "page_number_right", ((pageNumber * 2) + 2).toString())
        sendVisibility(book, "turn_page_left", pageNumber > 0)
        sendVisibility(book, "turn_page_right", pageNumber < pages.lastIndex)
        val lines = pages.getOrNull(pageNumber)
        if (book == "book_indexed") {
            for (i in 0 until 30) {
                val line = lines?.getOrNull(i) ?: ""
                val clickable = pageNumber == 0 && i > 0 && i < 15 && line.isNotBlank()
                val type = if (clickable) "line_click" else "line"
                sendVisibility(book, "line_click${i + 1}", clickable)
                sendVisibility(book, "line${i + 1}", !clickable)
                sendText(book, "$type${i + 1}", line)
            }
        } else {
            for (i in 0 until if (book == "book") 22 else 30) {
                sendText(book, "line${i + 1}", lines?.getOrNull(i) ?: "")
            }
        }
    }
}
