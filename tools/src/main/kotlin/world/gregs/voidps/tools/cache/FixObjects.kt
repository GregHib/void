package world.gregs.voidps.tools.cache

import com.displee.cache.CacheLibrary
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.definition.data.ObjectDefinitionFull
import world.gregs.voidps.cache.definition.decoder.ObjectDecoderFull
import world.gregs.voidps.cache.definition.encoder.ObjectEncoder

object FixObjects {

    private const val IMP_CATCHER_TABLE = 16171

    fun fix(library: CacheLibrary) {
        println("Fixing object parameters...")
        val indexId = Index.OBJECTS
        val index = library.index(indexId)

        val objectFixes = setOf(
            IMP_CATCHER_TABLE,
        )

        val cache = CacheDelegate(library)
        val decoder = ObjectDecoderFull()
        val encoder = ObjectEncoder()

        val fixed = mutableListOf<ObjectDefinitionFull>()
        for (id in objectFixes) {
            val definition = ObjectDefinitionFull(id)
            val data = library.data(indexId, decoder.getArchive(id), decoder.getFile(id)) ?: continue
            val buffer = ArrayReader(data)
            decoder.readLoop(definition, buffer)
            fix(definition)
            fixed.add(definition)
        }

        for (definition in fixed) {
            val writer = ArrayWriter(500)
            with(encoder) {
                writer.encode(definition)
            }
            val out = writer.toArray()
            val actual = ObjectDefinitionFull()
            decoder.readLoop(actual, ArrayReader(out))
            library.put(indexId, decoder.getArchive(definition.id), decoder.getFile(definition.id), out)
        }
        index.flag()
        cache.update()
        println("Fixed ${fixed.size} object definitions.")
    }

    private fun fix(definition: ObjectDefinitionFull) {
        if (definition.id == IMP_CATCHER_TABLE) {
            // Prevent table disappearing on quest completion https://www.youtube.com/watch?v=zVLhKyRmXnc
            definition.transforms = intArrayOf(16169, 16169, 16170, 16170, -1)
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val path = "./data/cache/"
        val lib = CacheLibrary(path)

        fix(lib)
    }
}
