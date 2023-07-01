package world.gregs.voidps.tools.cache

import org.koin.core.context.startKoin
import org.koin.fileProperties
import world.gregs.voidps.cache.definition.decoder.EnumDecoder
import world.gregs.voidps.engine.client.cacheModule
import java.io.File

object DumpEnums {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule)
        }.koin
        val decoder = EnumDecoder(koin.get())
        val builder = StringBuilder()
        for (i in decoder.indices) {
            val def = decoder.getOrNull(i) ?: continue
            builder.append(def.toString()).append("\n")
        }
        File("enums.txt").writeText(builder.toString())
    }
}