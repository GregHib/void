package world.gregs.voidps.tools.definition

import world.gregs.voidps.engine.data.FileStorage
import world.gregs.yaml.Yaml

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
        val path = "./data/definitions/containers.yml"
        val data: Map<String, Any> = Yaml().load(path)
        storage.save(path, data.toList().sortedBy { (_, value) -> if (value is Int) value else (value as Map<String, Any>)["id"] as Int }.toMap())
    }
}