package world.gregs.toml.read

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList

@Suppress("UNCHECKED_CAST")
class TomlMapApi : TomlStream.Api {

    private fun list() = ObjectArrayList<Any>(2)
    private fun map() = Object2ObjectOpenHashMap<String, Any>(8, .25f)

    val root: MutableMap<String, Any> = map()

    override fun table(addressBuffer: Array<String>, addressSize: Int) {
        getOrCreateNestedMap(addressBuffer, addressSize)
    }

    override fun inlineTable(addressBuffer: Array<String>, addressSize: Int) {
        getOrCreateNestedMap(addressBuffer, addressSize)
    }

    override fun appendMap(addressBuffer: Array<String>, addressSize: Int, key: String, value: Double) {
        val map = getOrCreateNestedMap(addressBuffer, addressSize)
        map[key] = value
    }

    override fun appendMap(addressBuffer: Array<String>, addressSize: Int, key: String, value: Long) {
        val map = getOrCreateNestedMap(addressBuffer, addressSize)
        map[key] = value
    }

    override fun appendMap(addressBuffer: Array<String>, addressSize: Int, key: String, value: String) {
        val map = getOrCreateNestedMap(addressBuffer, addressSize)
        map[key] = value
    }

    override fun appendMap(addressBuffer: Array<String>, addressSize: Int, key: String, value: Boolean) {
        val map = getOrCreateNestedMap(addressBuffer, addressSize)
        map[key] = value
    }

    // Get or create a nested map based on the address path
    private fun getOrCreateNestedMap(addressBuffer: Array<String>, addressSize: Int): MutableMap<String, Any> {
        var current: MutableMap<String, Any> = root

        // Navigate through the address path
        for (i in 0 until addressSize) {
            val key = addressBuffer[i]
            if (key.isEmpty()) {
                break
            }

            // Create intermediate maps if they don't exist
            if (key !in current) {
                current[key] = map()
            }

            val next = current[key]
            if (next is MutableMap<*, *>) {
                current = next as MutableMap<String, Any>
            } else {
                // Create a new map if the path was previously a non-map value
                val newMap = map()
                current[key] = newMap
                current = newMap
            }
        }

        return current
    }

    override fun list(addressBuffer: Array<String>, addressSize: Int) {
        getOrCreateList(addressBuffer, addressSize)
    }

    override fun appendList(addressBuffer: Array<String>, addressSize: Int, value: Double) {
        val list = getOrCreateList(addressBuffer, addressSize)
        list.add(value)
    }

    override fun appendList(addressBuffer: Array<String>, addressSize: Int, value: Long) {
        val list = getOrCreateList(addressBuffer, addressSize)
        list.add(value)
    }

    override fun appendList(addressBuffer: Array<String>, addressSize: Int, value: String) {
        val list = getOrCreateList(addressBuffer, addressSize)
        list.add(value)
    }

    override fun appendList(addressBuffer: Array<String>, addressSize: Int, value: Boolean) {
        val list = getOrCreateList(addressBuffer, addressSize)
        list.add(value)
    }

    // Get or create a list at the specified address
    private fun getOrCreateList(addressBuffer: Array<String>, addressSize: Int): MutableList<Any> {
        val parentMap = getOrCreateNestedMap(addressBuffer, addressSize - 1)
        val listKey = addressBuffer[addressSize - 1]

        if (listKey !in parentMap) {
            parentMap[listKey] = list()
        }

        val list = parentMap[listKey]
        if (list is MutableList<*>) {
            return list as MutableList<Any>
        } else {
            // Convert to list if needed
            val newList = list()
            parentMap[listKey] = newList
            return newList
        }
    }
}