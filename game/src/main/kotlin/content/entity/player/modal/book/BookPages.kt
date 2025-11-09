package content.entity.player.modal.book

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inject

class BookPages : Script {

    val books: Books by inject()

    init {
        itemOption("Read") { (item) ->
            openBook(item.def.getOrNull("book") ?: return@itemOption)
        }

        interfaceRefresh("book,book_long,book_indexed") { id ->
            refreshBook(this, id)
        }

        continueDialogue("book:turn_page_right", ::turnRight)
        continueDialogue("book_long:turn_page_right", ::turnRight)
        continueDialogue("book_indexed:turn_page_right", ::turnRight)

        continueDialogue("book:turn_page_left", ::turnLeft)
        continueDialogue("book_long:turn_page_left", ::turnLeft)
        continueDialogue("book_indexed:turn_page_left", ::turnLeft)

        continueDialogue("book_indexed:index") {
            set("book_page", 0)
            val type = it.substringAfter(":")
            close(type)
            open(type)
        }

        continueDialogue("book_indexed:line_click*") {
            val name: String = get("book") ?: return@continueDialogue
            val pages = books.get(name)
            val indices = pages.first()
            val component = it.substringAfter(":")
            val index = component.removePrefix("line_click").toInt() - 1
            val selected = indices[index]
            val page = pages.indexOfFirst { page -> page.contains("<navy>$selected") }
            val id = it.substringBefore(":")
            set("book_page", page)
            close(id)
            open(id)
        }

        interfaceClosed("book") {
            clearAnim()
        }
    }

    fun turnRight(player: Player, id: String) {
        player.inc("book_page")
        val type = id.substringBefore(":")
        player.close(type)
        player.open(type)
    }

    fun turnLeft(player: Player, id: String) {
        player.dec("book_page")
        val type = id.substringBefore(":")
        player.close(type)
        player.open(type)
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
