package content.entity.player.modal.book

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
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
    private lateinit var books: Map<String, List<String>>
    private lateinit var titles: Map<String, String>

    fun isLong(name: String) = longBooks.contains(name)

    fun get(name: String) = books.getOrDefault(name, emptyList())

    fun title(name: String) = titles.getOrDefault(name, "")

    @Suppress("UNCHECKED_CAST")
    fun load(path: String = Settings["definitions.books"]): Books {
        timedLoad("book") {
            val longBooks = ObjectOpenHashSet<String>(10, Hash.VERY_FAST_LOAD_FACTOR)
            val titles = Object2ObjectOpenHashMap<String, String>(10, Hash.VERY_FAST_LOAD_FACTOR)
            val books = Object2ObjectOpenHashMap<String, List<String>>(10, Hash.VERY_FAST_LOAD_FACTOR)
            val reader = object : ConfigReader(50) {
                override fun set(section: String, key: String, value: Any) {
                    when (key) {
                        "long" -> if (value is Boolean && value) longBooks.add(section)
                        "title" -> titles[section] = value as String
                        "pages" -> books[section] = value as List<String>
                    }
                }
            }
            Config.decodeFromFile(path, reader)
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