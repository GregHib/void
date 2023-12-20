package world.gregs.voidps.tools

import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.data.ObjectDefinitionFull
import world.gregs.voidps.cache.definition.decoder.ObjectDecoderFull

object ObjectDefinitions {

    @JvmStatic
    fun main(args: Array<String>) {
        val cache = CacheDelegate("./data/cache")

        val decoder = ObjectDecoderFull(false, true).loadCache(cache)

        decoder.findMatchingModels(24368)
//        for (def in decoder) {
//            if(def.params != null) {
//                println("${def.id} ${def.name} ${def.params}")
//            }
//        }
    }

    fun Array<ObjectDefinitionFull>.findMatchingName(name: String): List<ObjectDefinitionFull> {
        return indices.mapNotNull {
            val def = getOrNull(it) ?: return@mapNotNull null
            if (def.modelIds != null && def.name.contains(name, true)) {
                println("Found $it ${def.options?.get(0)} ${def.modelIds?.contentDeepToString()}")
                return@mapNotNull def
            } else {
                return@mapNotNull null
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

    fun Array<ObjectDefinitionFull>.findTransforms(id: Int): List<ObjectDefinitionFull> {
        return indices.mapNotNull {
            val def = getOrNull(it) ?: return@mapNotNull null
            if (def.transforms?.contains(id) == true) {
                println("Found $it ${def.transforms?.contentToString()}")
                return@mapNotNull def
            }
            return@mapNotNull null
        }
    }

    fun Array<ObjectDefinitionFull>.findVarbit(id: Int): List<ObjectDefinitionFull> {
        return indices.mapNotNull {
            val def = getOrNull(it) ?: return@mapNotNull null
            if (def.varbit == id) {
                println("Found $it ${def.varbit}")
                return@mapNotNull def
            }
            return@mapNotNull null
        }
    }
}