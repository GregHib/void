package rs.dusk.tools.detail

import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.entity.EntityDetails.Companion.toIdentifier

abstract class NameDumper {

    abstract fun createName(id: Int): String?

    internal abstract fun createData(id: Int): Map<String, Any>

    fun dump(loader: FileLoader, path: String, name: String, count: Int) {
        val entities = getNamedEntities(count)
        val unique = getUniqueList(entities)
        val sorted = unique.toList().sortedBy { it.second["id"] as Int }.toMap()
        loader.save(path, sorted)
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
        return map
    }

    private fun getUniqueList(map: MutableMap<String, MutableList<Int>>): MutableMap<String, Map<String, Any>> {
        val unique = mutableMapOf<String, Map<String, Any>>()
        map.forEach { (name, list) ->
            if (list.size > 1) {
                list.forEachIndexed { index, id ->
                    if(index == 0) {
                        unique[toIdentifier(name)] = createData(id)
                    } else {
                        unique["${toIdentifier(name)}_${index + 1}"] = createData(id)
                    }
                }
            } else {
                unique[toIdentifier(name)] = createData(list.first())
            }
        }
        return unique
    }

}