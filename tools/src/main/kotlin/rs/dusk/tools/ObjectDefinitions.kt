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
        val original = decoder.get(89)!!
        for (i in 0 until decoder.size) {
            val def = decoder.get(i) ?: continue
            if(def.modelIds != null && def.modelIds!!.contentDeepEquals(original.modelIds!!)) {
                println("Found $i ${def.options?.get(0)}")
            }
        }
    }
}