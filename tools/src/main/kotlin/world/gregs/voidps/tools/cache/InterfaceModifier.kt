package world.gregs.voidps.tools.cache

import com.displee.cache.CacheLibrary
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinitionFull
import world.gregs.voidps.cache.definition.decoder.InterfaceDecoderFull
import world.gregs.voidps.cache.definition.encoder.InterfaceEncoder

abstract class InterfaceModifier {

    fun modifyInterface(cache: CacheLibrary, id: Int, modifications: Map<Int, (InterfaceComponentDefinitionFull) -> Unit>) {
        val interfaceDecoder = InterfaceDecoderFull()
        val encoder = InterfaceEncoder()
        for (file in cache.index(Index.INTERFACES).archive(id)!!.fileIds()) {
            val data = cache.data(Index.INTERFACES, id, file) ?: return

            // Read interface definition
            val definition = InterfaceComponentDefinitionFull()
            with(interfaceDecoder) {
                definition.read(ArrayReader(data))
            }

            val mod = modifications[file] ?: continue
            definition.id = file
            println("Modified interface $id $file")
            mod.invoke(definition)

            // Write
            val buffer = ArrayWriter(1024)
            with(encoder) {
                buffer.encode(definition)
            }

            cache.put(Index.INTERFACES, id, file, buffer.toArray())
        }
    }
}
