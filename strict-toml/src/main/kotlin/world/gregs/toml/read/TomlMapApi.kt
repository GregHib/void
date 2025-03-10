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
                    @Suppress("UNCHECKED_CAST")
                    val list = current as MutableList<Any>
                    val index = addressPart

                    // Ensure the list has enough elements
                    while (list.size <= index) {
                        list.add(mutableMapOf<String, Any>())
                    }

                    current = list[index]
                }

                // Handle map navigation
                addressPart is String && current is MutableMap<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    val map = current as MutableMap<String, Any>

                    if (addressPart !in map) {
                        map[addressPart] = mutableMapOf<String, Any>()
                    }

                    current = map[addressPart]!!
                }

                // Unexpected state: can't navigate further
                else -> {
                    // Create a new empty map and return it
                    return mutableMapOf<String, Any>()
                }
            }
        }

        return current
    }

    // Get or create a nested map based on the address path
    private fun getOrCreateNestedMap(addressBuffer: Array<Any>, addressSize: Int): MutableMap<String, Any> {
        val current = navigateAddress(addressBuffer, addressSize)

        return if (current is MutableMap<*, *>) {
            @Suppress("UNCHECKED_CAST")
            current as MutableMap<String, Any>
        } else {
            // If we can't get a map at this address, create a new one
            val newMap = mutableMapOf<String, Any>()

            // Update the parent to point to this new map
            if (addressSize > 0) {
                val parentSize = addressSize - 1
                val lastKey = addressBuffer[parentSize]
                val parent = navigateAddress(addressBuffer, parentSize)

                if (parent is MutableMap<*, *> && lastKey is String) {
                    @Suppress("UNCHECKED_CAST")
                    (parent as MutableMap<String, Any>)[lastKey] = newMap
                } else if (parent is MutableList<*> && lastKey is Int) {
                    @Suppress("UNCHECKED_CAST")
                    (parent as MutableList<Any>)[lastKey] = newMap
                }
            }

            newMap
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
            @Suppress("UNCHECKED_CAST")
            val map = parent as MutableMap<String, Any>

            if (lastKey !in map || map[lastKey] !is MutableList<*>) {
                map[lastKey] = mutableListOf<Any>()
            }

            @Suppress("UNCHECKED_CAST")
            return map[lastKey] as MutableList<Any>
        } else if (parent is MutableList<*> && lastKey is Int) {
            @Suppress("UNCHECKED_CAST")
            val list = parent as MutableList<Any>
            val index = lastKey

            // Ensure the list has enough elements
            while (list.size <= index) {
                list.add(mutableListOf<Any>())
            }

            if (list[index] !is MutableList<*>) {
                list[index] = mutableListOf<Any>()
            }

            @Suppress("UNCHECKED_CAST")
            return list[index] as MutableList<Any>
        } else {
            // Fallback: create a new list
            val newList = mutableListOf<Any>()
            return newList
        }
    }
}