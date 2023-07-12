package world.gregs.voidps.tools.definition

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
        val yaml = Yaml()
        val path = "./data/definitions/inventories.yml"
        val data: Map<String, Any> = yaml.load(path)
        yaml.save(path, data.toList().sortedBy { (_, value) -> if (value is Int) value else (value as Map<String, Any>)["id"] as Int }.toMap())
    }
}