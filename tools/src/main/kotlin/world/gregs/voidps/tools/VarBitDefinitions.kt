package world.gregs.voidps.tools

import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.fileProperties
import world.gregs.voidps.cache.config.decoder.PlayerVariableParameterDecoder
import world.gregs.voidps.cache.definition.decoder.VarBitDecoder
import world.gregs.voidps.engine.client.cacheModule

object VarBitDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, module {
                single { PlayerVariableParameterDecoder(get()) }
            })
        }.koin
        val decoder = VarBitDecoder(koin.get())
        val varpDecoder = PlayerVariableParameterDecoder(koin.get())
        for (i in decoder.indices) {
            val def = decoder.getOrNull(i) ?: continue
            println(def)
        }
        for (i in varpDecoder.indices) {
            val def = varpDecoder.getOrNull(i) ?: continue
            println(def)
        }
    }
}