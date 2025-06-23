package world.gregs.voidps.tools.detail

import world.gregs.voidps.engine.data.definition.DefinitionsDecoder.Companion.toIdentifier
import world.gregs.yaml.Yaml

abstract class NameDumper {

    abstract fun createName(id: Int): String?

    internal open fun createData(name: String, id: Int): Map<String, Any> = createData(id)

    internal open fun createData(id: Int): Map<String, Any> = emptyMap()

    fun dump(yaml: Yaml, path: String, name: String, count: Int) {
        val entities = getNamedEntities(count)
        val unique = getUniqueList(entities)
        val sorted = unique.toList().sortedBy { it.second["id"] as Int }.toMap()
        yaml.save(path, sorted)
        println("${unique.size} $name identifiers dumped to $path.")
    }

    private fun getNamedEntities(count: Int): MutableMap<String, MutableList<Int>> {
        val map = mutableMapOf<String, MutableList<Int>>()
        repeat(count) { id ->
            val name = createName(id) ?: return@repeat
            val list = map.getOrPut(toIdentifier(name)) { mutableListOf() }
            list.add(id)
        }
        map.remove("null")
        map.remove("")

        return map.mapValues { (key, value) -> sortList(key, value) }.toMutableMap()
    }

    open fun sortList(key: String, list: MutableList<Int>): MutableList<Int> = list

    private fun getUniqueList(map: MutableMap<String, MutableList<Int>>): MutableMap<String, Map<String, Any>> {
        val unique = mutableMapOf<String, Map<String, Any>>()
        map.forEach { (name, list) ->
            if (list.size > 1) {
                list.forEachIndexed { index, id ->
                    if (index == 0) {
                        unique[toIdentifier(name)] = createData(name, id)
                    } else {
                        unique["${toIdentifier(name)}_${index + 1}"] = createData(name, id)
                    }
                }
            } else {
                unique[toIdentifier(name)] = createData(name, list.first())
            }
        }
        return unique
    }
}
