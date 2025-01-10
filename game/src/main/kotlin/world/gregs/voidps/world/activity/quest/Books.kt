package world.gregs.voidps.world.activity.quest

import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration

class Books {

    private lateinit var longBooks: Set<String>
    private lateinit var books: Map<String, List<String>>
    private lateinit var titles: Map<String, String>

    fun isLong(name: String) = longBooks.contains(name)

    fun get(name: String) = books.getOrDefault(name, emptyList())

    fun title(name: String) = titles.getOrDefault(name, "")

    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.books"]): Books {
        timedLoad("book") {
            val config = object : YamlReaderConfiguration(2, 2) {
                override fun add(list: MutableList<Any>, value: Any, parentMap: String?) {
                    super.add(list, (value as String).trimIndent(), parentMap)
                }
            }
            val data = yaml.load<Map<String, Map<String, Any>>>(path, config)
            this.longBooks = data.mapNotNull { if (it.value["long"] as? Boolean == true) it.value["title"] as String else null }.toSet()
            this.titles = data.mapValues { it.value["title"] as String }
            this.books = data.mapValues { it.value["pages"] as List<String> }
            this.books.size
        }
        return this
    }
}

fun Player.openBook(name: String) {
    this["book"] = name
    this["book_page"] = 0
    open(if (get<Books>().isLong(name)) "book_long" else "book")
}