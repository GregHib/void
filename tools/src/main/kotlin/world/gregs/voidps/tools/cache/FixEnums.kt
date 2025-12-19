package world.gregs.voidps.tools.cache

import com.displee.cache.CacheLibrary
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.definition.data.EnumDefinition
import world.gregs.voidps.cache.definition.decoder.EnumDecoder
import world.gregs.voidps.cache.definition.encoder.EnumEncoder

object FixEnums {

    private const val SUMMONING_POUCH_CRAFTING_INGREDIENT_STRINGS = 1186

    private const val RUNE_MINOTAUR_POUCH = 12083

    fun fix(library: CacheLibrary) {
        println("Fixing enums...")
        val indexId = Index.ENUMS
        val index = library.index(indexId)

        /*
            Update incorrect enum values
         */
        val enumFixes = mapOf(
            SUMMONING_POUCH_CRAFTING_INGREDIENT_STRINGS to mapOf(
                RUNE_MINOTAUR_POUCH to "This pouch requires 1 runite bar, 1 blue charm and 100 spirit shards.",
            ),
        )

        val cache = CacheDelegate(library)
        val decoder = EnumDecoder()
        val encoder = EnumEncoder()

        val fixed = mutableListOf<EnumDefinition>()
        for ((id, fixes) in enumFixes) {
            val definition = EnumDefinition(id)
            val data = library.data(indexId, decoder.getArchive(id), decoder.getFile(id)) ?: continue
            val buffer = ArrayReader(data)
            decoder.readLoop(definition, buffer)
            val map = definition.map!! as MutableMap
            for ((key, value) in fixes) {
                map[key] = value
            }
            fixed.add(definition)
        }

        for (definition in fixed) {
            val writer = BufferWriter(10000)
            with(encoder) {
                writer.encode(definition)
            }
            val out = writer.toArray()
            val actual = EnumDefinition()
            decoder.readLoop(actual, ArrayReader(out))
            library.put(indexId, decoder.getArchive(definition.id), decoder.getFile(definition.id), out)
        }
        index.flag()
        cache.update()
        println("Fixed ${fixed.size} enum definitions.")
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val path = "./data/cache/"
        val lib = CacheLibrary(path)

        fix(lib)
    }
}
