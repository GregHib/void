package world.gregs.voidps.tools

import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.data.ObjectDefinitionFull
import world.gregs.voidps.cache.definition.decoder.ObjectDecoderFull

object ObjectDefinitions {

    @JvmStatic
    fun main(args: Array<String>) {
        val cache = CacheDelegate("./data/cache")

        val decoder = ObjectDecoderFull(false, true).loadCache(cache)
        val count = decoder.lastIndex
        println(count)
        decoder.findMatchingName("table")
    }

    fun Array<ObjectDefinitionFull>.findMatchingName(name: String) {
        for (i in indices) {
            val def = getOrNull(i) ?: continue
            if (def.modelIds != null && def.name.contains(name, true)) {
                println("Found $i ${def.options?.get(0)} ${def.modelIds?.contentDeepToString()}")
            }
        }
    }

    fun Array<ObjectDefinitionFull>.findMatchingSize(width: Int, height: Int) {
        for (i in indices) {
            val def = getOrNull(i) ?: continue
            if (def.modelIds != null && def.sizeX == width && def.sizeY == height) {
                println("Found $i ${def.options?.get(0)} ${def.modelIds?.contentDeepToString()}")
            }
        }
    }

    fun Array<ObjectDefinitionFull>.findMatchingModels(id: Int) {
        val original = getOrNull(id)!!
        for (i in indices) {
            val def = getOrNull(i) ?: continue
            if (def.modelIds != null && def.modelIds!!.contentDeepEquals(original.modelIds!!) && original.modifiedColours != null && def.modifiedColours.contentEquals(original.modifiedColours!!)) {
                println("Found $i ${def.options?.get(0)}")
            }
        }
    }

    fun Array<ObjectDefinitionFull>.findTransforms(id: Int) {
        for (i in indices) {
            val def = getOrNull(i) ?: continue
            if (def.transforms?.contains(id) == true) {
                println("Found $i ${def.transforms?.contentToString()}")
            }
        }
    }
}