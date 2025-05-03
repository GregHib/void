package content.entity.player.modal.book

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.config.Config
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad

class Books {

    private lateinit var types: Map<String, String>
    private lateinit var books: Map<String, List<List<String>>>
    private lateinit var titles: Map<String, String>

    fun type(name: String) = types[name] ?: "normal"

    fun get(name: String) = books.getOrDefault(name, emptyList())

    fun title(name: String) = titles.getOrDefault(name, "")

    fun load(paths: List<String>): Books {
        timedLoad("book") {
            val types = Object2ObjectOpenHashMap<String, String>(10, Hash.VERY_FAST_LOAD_FACTOR)
            val titles = Object2ObjectOpenHashMap<String, String>(10, Hash.VERY_FAST_LOAD_FACTOR)
            val books = Object2ObjectOpenHashMap<String, List<List<String>>>(10, Hash.VERY_FAST_LOAD_FACTOR)
            for (path in paths) {
                Config.fileReader(path, 50) {
                    while (nextSection()) {
                        val book = section()
                        while (nextPair()) {
                            val key = key()
                            when (key) {
                                "type" -> types[book] = string()
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
            }
            this.types = types
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
    val type = get<Books>().type(name)
    open(if (type == "normal") "book" else "book_${type}")
}