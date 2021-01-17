package rs.dusk.tools

import org.koin.core.context.startKoin
import rs.dusk.cache.Cache
import rs.dusk.cache.Configs
import rs.dusk.cache.definition.decoder.ClientScriptDecoder
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule

object ClientScriptDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin

        val cache: Cache = koin.get()
        val decoder: ClientScriptDecoder = koin.get()
        println(decoder.get(4731))
    }

    fun getScriptId(cache: Cache, id: Int, context: Int): Int {
        var scriptId = cache.getArchiveId(Configs.SEQUENCES, context or (id shl 10))
        if (scriptId != -1) {
            return scriptId
        }
        scriptId = cache.getArchiveId(Configs.SEQUENCES, (65536 + id shl 10) or context)
        if (scriptId != -1) {
            return scriptId
        }
        scriptId = cache.getArchiveId(Configs.SEQUENCES, context or 0x3fffc00)
        return scriptId
    }
}