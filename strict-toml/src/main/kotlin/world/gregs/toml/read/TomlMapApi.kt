package world.gregs.toml.read

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList

@Suppress("UNCHECKED_CAST")
class TomlMapApi : TomlStream.Api {

    private fun list() = ObjectArrayList<Any>(2)
    private fun map() = Object2ObjectOpenHashMap<String, Any>(8, .25f)

    val root: MutableMap<String, Any> = map()

    override fun table(addressBuffer: Array<Any>, addressSize: Int) {
        getOrCreateNestedMap(addressBuffer, addressSize)
    }

    override fun inlineTable(addressBuffer: Array<Any>, addressSize: Int) {
        getOrCreateNestedMap(addressBuffer, addressSize)
    }

    override fun appendMap(addressBuffer: Array<Any>, addressSize: Int, key: String, value: Double) {
        val map = getOrCreateNestedMap(addressBuffer, addressSize)
        map[key] = value
    }

    override fun appendMap(addressBuffer: Array<Any>, addressSize: Int, key: String, value: Long) {
        val map = getOrCreateNestedMap(addressBuffer, addressSize)
        map[key] = value
    }

    override fun appendMap(addressBuffer: Array<Any>, addressSize: Int, key: String, value: String) {
        val map = getOrCreateNestedMap(addressBuffer, addressSize)
        map[key] = value
    }

    override fun appendMap(addressBuffer: Array<Any>, addressSize: Int, key: String, value: Boolean) {
        val map = getOrCreateNestedMap(addressBuffer, addressSize)
        map[key] = value
    }

    override fun list(addressBuffer: Array<Any>, addressSize: Int) {
        getOrCreateList(addressBuffer, addressSize)
    }

    override fun appendList(addressBuffer: Array<Any>, addressSize: Int, value: Double) {
        val list = getOrCreateList(addressBuffer, addressSize)
        list.add(value)
    }

    override fun appendList(addressBuffer: Array<Any>, addressSize: Int, value: Long) {
        val list = getOrCreateList(addressBuffer, addressSize)
        list.add(value)
    }

    override fun appendList(addressBuffer: Array<Any>, addressSize: Int, value: String) {
        val list = getOrCreateList(addressBuffer, addressSize)
        list.add(value)
    }

    override fun appendList(addressBuffer: Array<Any>, addressSize: Int, value: Boolean) {
        val list = getOrCreateList(addressBuffer, addressSize)
        list.add(value)
    }

    private fun navigateAddress(addressBuffer: Array<Any>, addressSize: Int): Any {
        var current: Any = root

        for (i in 0 until addressSize) {
            val addressPart = addressBuffer[i]

            when {
                // Handle array indexing
                addressPart is Int && current is MutableList<*> -> {
                    val list = current as MutableList<Any>

                    // Ensure the list has enough elements
                    while (list.size <= addressPart) {
                        list.add(map())
                    }

                    current = list[addressPart]
                }

                // Handle map navigation
                addressPart is String && current is MutableMap<*, *> -> {
                    val map = current as MutableMap<String, Any>

                    if (addressPart !in map) {
                        map[addressPart] = map()
                    }

                    current = map[addressPart]!!
                }

                // Unexpected state: can't navigate further
                else -> throw IllegalArgumentException("Unknown address type: $addressPart")
            }
        }

        return current
    }

    // Get or create a nested map based on the address path
    private fun getOrCreateNestedMap(addressBuffer: Array<Any>, addressSize: Int): MutableMap<String, Any> {
        return when (val current = navigateAddress(addressBuffer, addressSize)) {
            is MutableMap<*, *> -> current as MutableMap<String, Any>
            is MutableList<*> -> {
                // If parent is a list, we should append a new map rather than overriding
                val list = current as MutableList<Any>

                if (list.isEmpty()) {
                    val map = map()
                    list.add(map)
                    return map
                }

                return list.last() as MutableMap<String, Any>
            }
            else -> throw IllegalArgumentException("Unknown type $current")
        }
    }

    // Get or create a list at the specified address
    private fun getOrCreateList(addressBuffer: Array<Any>, addressSize: Int): MutableList<Any> {
        if (addressSize == 0) {
            throw IllegalArgumentException("Cannot create list at root level")
        }

        val parentSize = addressSize - 1
        val lastKey = addressBuffer[addressSize - 1]
        val parent = navigateAddress(addressBuffer, parentSize)

        if (parent is MutableMap<*, *> && lastKey is String) {
            val map = parent as MutableMap<String, Any>

            if (lastKey !in map || map[lastKey] == null) {
                map[lastKey] = list()
            }

            return map[lastKey] as MutableList<Any>
        } else if (parent is MutableList<*> && lastKey is Int) {
            val list = parent as MutableList<Any>

            // Ensure the list has enough elements
            while (list.size <= lastKey) {
                list.add(list())
            }

            return list[lastKey] as MutableList<Any>
        } else {
            throw IllegalArgumentException("Unknown type $parent")
        }
    }

    override fun arrayOfTables(addressBuffer: Array<Any>, addressSize: Int) {
        if (addressSize == 0) {
            return
        }

        val parentSize = addressSize - 1
        val lastKey = addressBuffer[addressSize - 1]
        val parent = navigateAddress(addressBuffer, parentSize)

        if (parent is MutableMap<*, *> && lastKey is String) {
            val map = parent as MutableMap<String, Any>

            // If key doesn't exist create a new array of tables
            if (lastKey !in map || map[lastKey] == null) {
                map[lastKey] = list()
            }

            // Add a new table to the array
            val tableArray = map[lastKey] as MutableList<Any>
            tableArray.add(map())
        } else {
            throw IllegalArgumentException("Unknown type $parent")
        }
    }
}