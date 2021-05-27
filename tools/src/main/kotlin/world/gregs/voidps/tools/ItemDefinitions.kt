package world.gregs.voidps.tools

import org.koin.core.context.startKoin
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.entity.definition.ItemDefinitions

object ItemDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = ItemDecoder(koin.get())
        val definitions = ItemDefinitions(decoder).load(FileLoader())
        for (i in 0 until decoder.size) {
            val def = definitions.getOrNull(i) ?: continue
            if(!def.has("weight") && !def.noted && !def.lent && def.stackable == 0) {
                println("${definitions.getName(i)} ${def.extras}")
            }
        }
    }
}