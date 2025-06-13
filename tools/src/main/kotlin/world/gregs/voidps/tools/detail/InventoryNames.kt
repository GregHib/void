package world.gregs.voidps.tools.detail

import world.gregs.voidps.engine.data.definition.DefinitionsDecoder.Companion.toIdentifier
import world.gregs.yaml.Yaml
import java.io.File

/**
 * Dumps unique string identifiers for inventories using shop csv
 */
private object InventoryNames {

    private data class Data(val name: String, val inventory: Int?, val sampleInventory: Int?, val skillcapeInventory: Int?, val trimmedInventory: Int?)
    private data class Ids(val id: Int)

    @JvmStatic
    fun main(args: Array<String>) {
        val file = File("./shop-info-667.csv")

        val map = mutableMapOf<Int, String>()

        file.readLines().map { it.split(",") }.map {
            Data(it[0], it[5].toIntOrNull(), it[6].toIntOrNull(), it[7].toIntOrNull(), it[8].toIntOrNull())
        }.forEach {
            if (it.inventory != null) {
                map[it.inventory] = toIdentifier(it.name)
            }
            if (it.sampleInventory != null) {
                map[it.sampleInventory] = "${toIdentifier(it.name)}_sample"
            }
            if (it.skillcapeInventory != null) {
                map[it.skillcapeInventory] = "${toIdentifier(it.name)}_skillcape"
            }
            if (it.trimmedInventory != null) {
                map[it.trimmedInventory] = "${toIdentifier(it.name)}_trimmed"
            }
        }
        println(map)
        val yaml = Yaml()
        val path = "./inventory-details.yml"
        val sorted = map.map { it.value to Ids(it.key) }.sortedBy { it.second.id }.toMap()
        yaml.save(path, sorted)
        println("${sorted.size} inventory identifiers dumped to $path.")
    }
}
