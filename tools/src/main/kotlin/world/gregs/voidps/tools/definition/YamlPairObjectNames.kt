package world.gregs.voidps.tools.definition

import world.gregs.voidps.engine.data.FileStorage
import world.gregs.yaml.Yaml

/**
 * Sorts yml file pairs of object names
 */
object YamlPairObjectNames {

    @JvmStatic
    fun main(args: Array<String>) {
        val storage = FileStorage(quotes = true)
        val parser = Yaml()
        val update: Map<String, Map<String, Any>> = parser.load("./data/definitions/objects.yml")
        val list = mutableListOf<Pair<String, Map<String, Any>>>()
        val keys = update.keys.toMutableSet()
        for ((key, value) in update) {
            if (keys.contains(key)) {
                var putFirst = false
                val replaced = if (key.endsWith("_closed")) {
                    key.replace("_closed", "_opened")
                } else if (key.endsWith("_opened")) {
                    putFirst = true
                    key.replace("_opened", "_closed")
                } else if (key.endsWith("_stump")) {
                    key.removeSuffix("_stump")
                } else {
                    putFirst = true
                    "${key}_stump"
                }
                if (keys.contains(replaced)) {
                    if (putFirst) {
                        list.add(key to value)
                        keys.remove(key)
                    }
                    list.add(replaced to update[replaced]!!)
                    keys.remove(replaced)
                    if (!putFirst) {
                        list.add(key to value)
                        keys.remove(key)
                    }
                } else {
                    list.add(key to value)
                    keys.remove(key)
                }
            }

        }
        list.forEach(::println)
        storage.save("./paired-objects.yaml", list.toMap())
    }
}