package world.gregs.voidps.tools.cache

import org.koin.core.context.startKoin
import org.koin.fileProperties
import world.gregs.voidps.cache.config.decoder.StructDecoder
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule
import java.io.File

object DumpStructs {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = StructDecoder(koin.get())
        val builder = StringBuilder()
        for (i in decoder.indices) {
            val def = decoder.getOrNull(i) ?: continue
            builder.append(def.toString()).append("\n")
        }
        File("structs.txt").writeText(builder.toString())
    }
}