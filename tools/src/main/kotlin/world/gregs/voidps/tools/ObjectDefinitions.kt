package world.gregs.voidps.tools

import org.koin.core.context.startKoin
import org.koin.fileProperties
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule

object ObjectDefinitions {

    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = ObjectDecoder(koin.get(), member = false, lowDetail = false, configReplace = false)
        repeat(decoder.last) {
            val def = decoder.getOrNull(it) ?: return@repeat
            if(def.name.contains("ladder", true) || def.name.contains("trapdoor", true)) {
                if(!def.options.contains("Climb-down")) {
                    println(def.options.toList())
                }
            }
        }
    }

    fun ObjectDecoder.findMatchingName(name: String) {
        for (i in 0 until last) {
            val def = getOrNull(i) ?: continue
            if (def.modelIds != null && def.name.contains(name, true)) {
                println("Found $i ${def.options.get(0)} ${def.modelIds?.contentDeepToString()}")
            }
        }
    }

    fun ObjectDecoder.findMatchingSize(width: Int, height: Int) {
        for (i in 0 until last) {
            val def = getOrNull(i) ?: continue
            if (def.modelIds != null && def.sizeX == width && def.sizeY == height) {
                println("Found $i ${def.options.get(0)} ${def.modelIds?.contentDeepToString()}")
            }
        }
    }

    fun ObjectDecoder.findMatchingModels(id: Int) {
        val original = getOrNull(id)!!
        for (i in 0 until last) {
            val def = getOrNull(i) ?: continue
            if (def.modelIds != null && def.modelIds!!.contentDeepEquals(original.modelIds!!)) {
                println("Found $i ${def.options.get(0)}")
            }
        }
    }
}