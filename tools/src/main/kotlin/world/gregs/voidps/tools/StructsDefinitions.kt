package world.gregs.voidps.tools

import org.koin.core.context.startKoin
import world.gregs.voidps.cache.definition.decoder.EnumDecoder
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule
import world.gregs.voidps.engine.map.Tile

object StructsDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = EnumDecoder(koin.get())
        val builder = StringBuilder()
        for (i in decoder.indices) {
            val def = decoder.getOrNull(i) ?: continue
            builder.append(def.toString()).append("\n")
        }
        println(Tile.getId(3088, 3571, 0))
//        File("enums.txt").writeText(builder.toString())
    }
}