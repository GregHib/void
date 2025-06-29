package world.gregs.voidps.tools.cache

import com.displee.cache.CacheLibrary
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.definition.data.ItemDefinitionFull
import world.gregs.voidps.cache.definition.decoder.ItemDecoderFull
import world.gregs.voidps.cache.definition.encoder.ItemEncoder

object FixItems {

    private const val RUNE_MINOTAUR_POUCH = 12083
    private const val RUNE_BULL_RUSH_SCROLL = 12466

    private const val SHARD_REFUND_AMOUNT = 457
    private const val SUMMONING_SHARD_AMOUNT = 541

    fun fix(library: CacheLibrary) {
        println("Fixing item parameters...")
        val indexId = Index.ITEMS
        val index = library.index(indexId)

        /*
            Update incorrect parameters on items
         */
        val itemFixes = mapOf(
            RUNE_MINOTAUR_POUCH to mapOf(
                SHARD_REFUND_AMOUNT to 70,
                SUMMONING_SHARD_AMOUNT to 100,
            ),
            RUNE_BULL_RUSH_SCROLL to mapOf(
                SHARD_REFUND_AMOUNT to 6,
            ),
        )

        val cache = CacheDelegate(library)
        val decoder = ItemDecoderFull()
        val encoder = ItemEncoder()

        val fixed = mutableListOf<ItemDefinitionFull>()
        for ((id, fixes) in itemFixes) {
            val definition = ItemDefinitionFull(id)
            val data = library.data(indexId, decoder.getArchive(id), decoder.getFile(id)) ?: continue
            val buffer = BufferReader(data)
            decoder.readLoop(definition, buffer)
            val params = definition.params!! as MutableMap
            for ((key, value) in fixes) {
                params[key] = value
            }
            fixed.add(definition)
        }

        for (definition in fixed) {
            val writer = BufferWriter(500)
            with(encoder) {
                writer.encode(definition)
            }
            val out = writer.toArray()
            val actual = ItemDefinitionFull()
            decoder.readLoop(actual, BufferReader(out))
            library.put(indexId, decoder.getArchive(definition.id), decoder.getFile(definition.id), out)
        }
        index.flag()
        cache.update()
        println("Fixed ${fixed.size} item definitions.")
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val path = "./data/cache/"
        val lib = CacheLibrary(path)

        fix(lib)
    }
}
