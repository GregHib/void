package rs.dusk.tools.detail

import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.entity.EntityDetails.Companion.toIdentifier

abstract class NameDumper {
    private data class Ids(val id: Int)

    fun dump(loader: FileLoader, path: String, name: String, count: Int, function: (Int) -> String?) {
        val entities = getNamedEntities(count, function)
        val unique = getUniqueList(entities)
        val sorted = unique.toList().sortedBy { it.second.id }.toMap()
        loader.save(path, sorted)
        println("${unique.size} $name identifiers dumped to $path.")
    }

    private fun getNamedEntities(count: Int, function: (Int) -> String?): MutableMap<String, MutableList<Int>> {
        val map = mutableMapOf<String, MutableList<Int>>()
        repeat(count) { id ->
            val name = function(id) ?: return@repeat
            val list = map.getOrPut(toIdentifier(name)) { mutableListOf() }
            list.add(id)
        }
        map.remove("null")
        map.remove("")
        return map
    }

    private fun getUniqueList(map: MutableMap<String, MutableList<Int>>): MutableMap<String, Ids> {
        val unique = mutableMapOf<String, Ids>()
        map.forEach { (name, list) ->
            if (list.size > 1) {
                list.forEachIndexed { index, id ->
                    unique["${toIdentifier(name)}_${index + 1}"] = Ids(id)
                }
            } else {
                unique[toIdentifier(name)] = Ids(list.first())
            }
        }
        return unique
    }

}