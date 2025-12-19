package world.gregs.voidps.tools.cache

import com.displee.cache.CacheLibrary
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.definition.data.ClientScriptDefinition
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinitionFull
import world.gregs.voidps.cache.definition.decoder.ClientScriptDecoder
import world.gregs.voidps.cache.definition.decoder.InterfaceDecoderFull
import world.gregs.voidps.cache.definition.encoder.ClientScriptEncoder
import world.gregs.voidps.cache.definition.encoder.InterfaceEncoder
import world.gregs.voidps.engine.data.Settings
import java.io.File

object MoveCameraClientScript {
    private val INTERFACES = mapOf(
        548 to 3,
        746 to 0,
    )
    private const val SCRIPT_ID = 4731

    fun convert(cache: CacheLibrary, other: File) {
        val otherCache = CacheLibrary(other.path)

        // Decode script
        val scriptDef = findMouseScript(otherCache)
        val newScriptId = addScript(cache, scriptDef)
        println("Add mouse move client script $newScriptId.")

        packInterfacesWithScript(cache, otherCache, newScriptId)
        cache.update()
    }

    private fun addScript(cache: CacheLibrary, scriptDef: ClientScriptDefinition): Int {
        val index = cache.index(Index.CLIENT_SCRIPTS)
        val scriptEncoder = ClientScriptEncoder()
        val writer = BufferWriter(1024)
        with(scriptEncoder) {
            writer.encode(scriptDef)
        }
        return index.add(writer.toArray()).id
    }

    private fun packInterfacesWithScript(cache: CacheLibrary, otherCache: CacheLibrary, newScriptId: Int) {
        val index = cache.index(Index.INTERFACES)
        val interfaceDecoder = InterfaceDecoderFull()
        val encoder = InterfaceEncoder()
        for ((id, parent) in INTERFACES) {
            for (file in otherCache.index(Index.INTERFACES).archive(id)!!.fileIds()) {
                val data = otherCache.data(Index.INTERFACES, id, file) ?: continue
                // Read interface definition
                val definition = InterfaceComponentDefinitionFull()
                with(interfaceDecoder) {
                    definition.read(ArrayReader(data))
                }

                // Find component with script
                val motionHandler = definition.mouseMotionHandler
                if (motionHandler == null || motionHandler.firstOrNull() != SCRIPT_ID) {
                    continue
                }

                val archive = index.archive(id)!!
                // Update
                val newId = archive.nextId()
                definition.id = newId
                definition.parent = parent
                motionHandler[0] = newScriptId
                // Write
                val buffer = BufferWriter(1024)
                with(encoder) {
                    buffer.encode(definition)
                }
                archive.add(buffer.toArray())
                println("Added new component $newId to interface $id.")
            }
        }
    }

    private fun findMouseScript(otherCache: CacheLibrary): ClientScriptDefinition {
        val scriptDecoder = ClientScriptDecoder(revision667 = true)
        val scriptData = otherCache.data(Index.CLIENT_SCRIPTS, SCRIPT_ID)!!
        val scriptDef = ClientScriptDefinition()
        scriptDef.id = SCRIPT_ID
        scriptDecoder.readLoop(scriptDef, ArrayReader(scriptData))
        return scriptDef
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val path718 = "./temp/cache/cache-667/"
        Settings.load()
        val cache = CacheLibrary(Settings["storage.cache.path"])
        convert(cache, File(path718))
    }
}
