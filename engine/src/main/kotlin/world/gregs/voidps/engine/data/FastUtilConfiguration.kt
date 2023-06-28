package world.gregs.voidps.engine.data

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.yaml.read.YamlReaderConfiguration

/**
 * Faster version of [YamlReaderConfiguration] using most common sizes and fast utils collections
 */
open class FastUtilConfiguration : YamlReaderConfiguration() {
    override fun createList(): MutableList<Any> = ObjectArrayList(EXPECTED_LIST_SIZE)

    override fun createMap(): MutableMap<String, Any> = Object2ObjectOpenHashMap(EXPECTED_MAP_SIZE)

    companion object {
        const val EXPECTED_LIST_SIZE = 2
        const val EXPECTED_MAP_SIZE = 8
    }
}