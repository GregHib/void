package world.gregs.voidps.tools.definition

import world.gregs.voidps.engine.data.file.FileStorage

/**
 * Sorts yml file by id
 * Expected format
 * name:
 *   id: 0
 */
object YamlSorter {

    @JvmStatic
    fun main(args: Array<String>) {
        val storage = FileStorage()
        val path = "./data/definitions/musics.yml"
        val data: Map<String, Map<String, Any>> = storage.load(path)
        storage.save(path, data.toList().sortedBy { it.second["id"] as Int }.toMap())
    }
}