package world.gregs.voidps.tools.cache

import com.displee.cache.CacheLibrary
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.definition.data.GraphicDefinition
import world.gregs.voidps.cache.definition.decoder.GraphicDecoder
import world.gregs.voidps.cache.definition.encoder.GraphicEncoder

object FixGraphics {

    /** The barker toad's Toad Bark cannonball, whose model faces backwards in this revision. */
    private const val TOAD_BARK_CANNONBALL = 1402

    fun fix(library: CacheLibrary) {
        println("Fixing graphic definitions...")
        val indexId = Index.GRAPHICS

        /*
            Rotate graphic models whose projectiles fly backwards. Rotation is in degrees
            (existing cache values are only ever 90/180/270).
         */
        val rotationFixes = mapOf(
            TOAD_BARK_CANNONBALL to 180,
        )

        val cache = CacheDelegate(library)
        val decoder = GraphicDecoder()
        val encoder = GraphicEncoder()

        val fixed = mutableListOf<GraphicDefinition>()
        for ((id, rotation) in rotationFixes) {
            val definition = GraphicDefinition(id)
            val data = library.data(indexId, decoder.getArchive(id), decoder.getFile(id)) ?: continue
            decoder.readLoop(definition, ArrayReader(data))
            definition.rotation = (definition.rotation + rotation) % 360
            fixed.add(definition)
        }

        for (definition in fixed) {
            val writer = ArrayWriter(500)
            with(encoder) {
                writer.encode(definition)
            }
            val out = writer.toArray()
            val actual = GraphicDefinition()
            decoder.readLoop(actual, ArrayReader(out))
            check(actual.copy(id = definition.id) == definition) { "Re-encoded graphic ${definition.id} doesn't round-trip." }
            library.put(indexId, decoder.getArchive(definition.id), decoder.getFile(definition.id), out)
        }
        library.index(indexId).flag()
        cache.update()
        println("Fixed ${fixed.size} graphic definitions.")
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val path = "./data/cache/"
        val lib = CacheLibrary(path)

        fix(lib)
    }
}
