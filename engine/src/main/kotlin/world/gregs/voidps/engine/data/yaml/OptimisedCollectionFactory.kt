package world.gregs.voidps.engine.data.yaml

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.data.YamlParser

open class OptimisedCollectionFactory : CollectionFactory() {
    override fun createList(): MutableList<Any> = ObjectArrayList(YamlParser.EXPECTED_EXPLICIT_LIST_SIZE)

    override fun createMap(): MutableMap<String, Any> = Object2ObjectOpenHashMap(YamlParser.EXPECTED_MAP_SIZE)
}