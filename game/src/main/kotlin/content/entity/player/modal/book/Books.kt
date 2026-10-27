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

    private val types: MutableMap<String, String> = Object2ObjectOpenHashMap()
    private lateinit var books: Map<String, List<List<String>>>
    private var titles: MutableMap<String, String> = Object2ObjectOpenHashMap()

    fun type(name: String) = types[name] ?: "normal"

    private val generators = Object2ObjectOpenHashMap<String, (Player) -> List<List<String>>>()

    fun get(name: String) = books.getOrDefault(name, emptyList())

    /**
     * Pages for [name], or generated for the [player] if the book changes with their progress
     */
    fun get(player: Player, name: String) = generators[name]?.invoke(player) ?: get(name)

    /**
     * Generate the pages of book [name] per player
     */
    fun generate(name: String, title: String, type: String = "normal", pages: (Player) -> List<List<String>>) {
        titles[name] = title
        types[name] = type
        generators[name] = pages
    }

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
            this.types.putAll(types)
            this.titles.putAll(titles)
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
    open(if (type == "normal") "book" else "book_$type")
    anim("human_read_book")
}
