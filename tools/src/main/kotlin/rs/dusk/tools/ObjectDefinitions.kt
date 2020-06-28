package rs.dusk.tools

import org.koin.core.context.startKoin
import rs.dusk.cache.cacheDefinitionModule
import rs.dusk.cache.cacheModule
import rs.dusk.cache.definition.decoder.ObjectDecoder

object ObjectDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }
        val decoder = ObjectDecoder(false, false)
//        println(decoder.get(2262))
//        println(decoder.get(2259))
//        println(decoder.get(3))
//        println(decoder.get(1531))
//        decoder.findMatchingModels(23917)
        decoder.findMatchingName("Long hall door")
    }

    fun ObjectDecoder.findMatchingName(name: String) {
        for (i in 0 until size) {
            val def = get(i) ?: continue
            if(def.modelIds != null && def.name.contains(name, true)) {
                println("Found $i ${def.options?.get(0)} ${def.modelIds?.contentDeepToString()}")
            }
        }
    }
    fun ObjectDecoder.findMatchingModels(id: Int) {
        val original = get(id)!!
        for (i in 0 until size) {
            val def = get(i) ?: continue
            if(def.modelIds != null && def.modelIds!!.contentDeepEquals(original.modelIds!!)) {
                println("Found $i ${def.options?.get(0)}")
            }
        }
    }
}