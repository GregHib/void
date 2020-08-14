package rs.dusk.tools

import org.koin.core.context.startKoin
import rs.dusk.cache.definition.decoder.ObjectDecoder
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule

object ObjectDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = ObjectDecoder(koin.get(), false, false)
//        println(decoder.get(2262))
//        println(decoder.get(2259))
//        println(decoder.get(3))
//        println(decoder.get(1531))
//        decoder.findMatchingModels(23917)
//        decoder.findMatchingName("Long hall door")
        decoder.findMatchingSize(3, 2)
    }

    fun ObjectDecoder.findMatchingName(name: String) {
        for (i in 0 until size) {
            val def = getOrNull(i) ?: continue
            if(def.modelIds != null && def.name.contains(name, true)) {
                println("Found $i ${def.options?.get(0)} ${def.modelIds?.contentDeepToString()}")
            }
        }
    }

    fun ObjectDecoder.findMatchingSize(width: Int, height: Int) {
        for (i in 0 until size) {
            val def = getOrNull(i) ?: continue
            if(def.modelIds != null && def.sizeX == width && def.sizeY == height) {
                println("Found $i ${def.options?.get(0)} ${def.modelIds?.contentDeepToString()}")
            }
        }
    }

    fun ObjectDecoder.findMatchingModels(id: Int) {
        val original = getOrNull(id)!!
        for (i in 0 until size) {
            val def = getOrNull(i) ?: continue
            if(def.modelIds != null && def.modelIds!!.contentDeepEquals(original.modelIds!!)) {
                println("Found $i ${def.options?.get(0)}")
            }
        }
    }
}