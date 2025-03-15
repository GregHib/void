package content.entity.player.modal.book

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad

class Books {

    private lateinit var longBooks: Set<String>
    private lateinit var books: Map<String, List<List<String>>>
    private lateinit var titles: Map<String, String>

    fun isLong(name: String) = longBooks.contains(name)

    fun get(name: String) = books.getOrDefault(name, emptyList())

    fun title(name: String) = titles.getOrDefault(name, "")

    fun load(path: String = Settings["definitions.books"]): Books {
        timedLoad("book") {
            val longBooks = ObjectOpenHashSet<String>(10, Hash.VERY_FAST_LOAD_FACTOR)
            val titles = Object2ObjectOpenHashMap<String, String>(10, Hash.VERY_FAST_LOAD_FACTOR)
            val books = Object2ObjectOpenHashMap<String, List<List<String>>>(10, Hash.VERY_FAST_LOAD_FACTOR)
            Config.fileReader(path, 50) {
                while (nextSection()) {
                    val book = section()
                    while (nextPair()) {
                        val key = key()
                        when (key) {
                            "long" -> if (boolean()) longBooks.add(book)
                            "title" -> titles[book] = string()
                            "pages" -> {
                                val pages = ObjectArrayList<List<String>>(4)
                                while (nextElement()) {
                                    val lines = ObjectArrayList<String>(20)
                                    while (nextElement()) {
                                        lines.add(string())
                                    }
                                    pages.add(lines)
                                }
                                books[book] = pages
                            }
                        }
                    }
                }
            }
            this.longBooks = longBooks
            this.titles = titles
            this.books = books
            titles.size
        }
        return this
    }
}

fun Player.openBook(name: String) {
    this["book"] = name
    this["book_page"] = 0
    open(if (get<Books>().isLong(name)) "book_long" else "book")
}