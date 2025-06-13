package world.gregs.voidps.tools.definition

import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration
import world.gregs.yaml.write.YamlWriterConfiguration

/**
 * Sorts yml file by id
 * Expected format
 * name:
 *   id: 0
 */
@Suppress("UNCHECKED_CAST")
object YamlSorter {

    private fun anchor(key: String) = key == "<<" || key == "&"

    private val prioritise = setOf(
        "id",
    )
    private val deprioritise = setOf(
        "examine",
        "amount",
    )

    private val comparator = object : Comparator<Pair<String, Any>> {
        private fun length(any: Any): Int = when (any) {
            is List<*> -> (any as List<Any>).sumOf { length(it) }
            is Map<*, *> -> any.size
            else -> any.toString().length
        }

        override fun compare(o1: Pair<String, Any>, o2: Pair<String, Any>): Int {
            val key1 = o1.first
            val key2 = o2.first
            if (anchor(key1)) {
                return -1
            } else if (anchor(key2)) {
                return 1
            }
            if (prioritise.contains(key1)) {
                return -1
            } else if (prioritise.contains(key2)) {
                return 1
            }
            if (deprioritise.contains(key1)) {
                return 1
            } else if (deprioritise.contains(key2)) {
                return -1
            }
            return (key1.length + length(o1.second)).compareTo(key2.length + length(o2.second))
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val yaml = Yaml()
        val path = "./data/definitions/items.toml"
        val readConfig = YamlReaderConfiguration(ignoreAnchors = true)
        val data: Map<String, Any> = yaml.load(path, readConfig)
        val writeConfig = object : YamlWriterConfiguration() {
            override fun explicit(list: List<*>, indent: Int, parentMap: String?) = parentMap != "items"
        }
        yaml.save(
            "./items.toml",
            data.toList()
                .map { (key, value) -> key to sort(value) }
                .sortedBy { (_, value) -> if (value is Int) value else (value as? Map<String, Any>)?.get("id") as? Int ?: -1 }
                .toMap(),
            writeConfig,
        )
    }

    private fun sort(value: Any): Any = when (value) {
        is Map<*, *> -> (value as Map<String, Any>).mapValues { sort(it.value) }.toList().sortedWith(comparator).toMap()
        is List<*> -> (value as List<Any>).map { sort(it) }
        else -> value
    }
}
