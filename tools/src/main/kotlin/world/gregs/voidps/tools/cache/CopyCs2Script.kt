package world.gregs.voidps.tools.cache

import com.displee.cache.CacheLibrary
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.definition.data.ClientScriptDefinition
import world.gregs.voidps.cache.definition.decoder.ClientScriptDecoder
import world.gregs.voidps.cache.definition.encoder.ClientScriptEncoder
import world.gregs.voidps.engine.data.Settings
import java.io.File

object CopyCs2Script : InterfaceModifier() {
    fun convert(cache: CacheLibrary, other: File, scriptId: Int) {
        val otherCache = CacheLibrary(other.path)

        // Decode script
        val script = readScript(otherCache, scriptId)
        writeScript(cache, script)
        cache.update()
    }

    private fun writeScript(cache: CacheLibrary, scriptDef: ClientScriptDefinition) {
        val scriptEncoder = ClientScriptEncoder()
        val writer = BufferWriter(1024)
        with(scriptEncoder) {
            writer.encode(scriptDef)
        }
        cache.put(Index.CLIENT_SCRIPTS, scriptDef.id, writer.toArray())
    }

    private fun readScript(otherCache: CacheLibrary, scriptId: Int): ClientScriptDefinition {
        val scriptDecoder = ClientScriptDecoder(revision667 = true)
        val scriptData = otherCache.data(Index.CLIENT_SCRIPTS, scriptId)!!
        val scriptDef = ClientScriptDefinition()
        scriptDef.id = scriptId
        scriptDecoder.readLoop(scriptDef, BufferReader(scriptData))
        return scriptDef
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val path718 = "./temp/cache/cache-667/"
        Settings.load()
        val cache = CacheLibrary(Settings["storage.cache.path"])
        convert(cache, File(path718), 677)
    }
}
