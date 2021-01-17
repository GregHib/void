package rs.dusk.tools

import com.displee.cache.CacheLibrary
import rs.dusk.buffer.write.BufferWriter
import rs.dusk.cache.CacheDelegate
import rs.dusk.cache.Indices
import rs.dusk.cache.definition.decoder.ClientScriptDecoder
import rs.dusk.cache.definition.encoder.ClientScriptEncoder

object MoveCameraClientScript {

    @JvmStatic
    fun main(args: Array<String>) {
        val decoder = ClientScriptDecoder(CacheDelegate("./cache/data/cache/"))
        val cache = CacheLibrary("./cache/data/634/")
        val encoder = ClientScriptEncoder(true)

        val definition = decoder.get(4731)
        val writer = BufferWriter()
        with(encoder) {
            writer.encode(definition)
        }
        val data = writer.toArray()
        cache.index(Indices.CLIENT_SCRIPTS).remove(4731)
        cache.index(Indices.CLIENT_SCRIPTS).add(4731).add(data)
        cache.update()
    }

}