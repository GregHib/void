package world.gregs.voidps.tools.cache

import com.displee.cache.CacheLibrary
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.definition.data.EnumDefinition
import world.gregs.voidps.cache.definition.decoder.EnumDecoder
import world.gregs.voidps.cache.definition.encoder.EnumEncoder

object FixEnums {

    private const val SUMMONING_POUCH_CRAFTING_INGREDIENT_STRINGS = 1186

    private const val RUNE_MINOTAUR_POUCH = 12083

    private const val PET_DETAILS_CHATHEAD_ANIMATIONS_NORMAL = 1276

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

        /*
            Restore enum defaultInt values. Earlier tooling overwrote enum 1276's
            defaultInt to -1, which suppressed the fallback chathead animation for
            every pet not explicitly listed in varbit 4282's values map. The 8373
            below is the original 634-cache value.
         */
        val enumDefaultIntFixes = mapOf(
            PET_DETAILS_CHATHEAD_ANIMATIONS_NORMAL to 8373,
        )

        /*
            Strip stale keys written by earlier tooling. Adding NPC ids 13089/13090
            to enum 1276 was a dead-end — varbit 4282 never stores those values, so
            the entries were unreachable and just bloated the map.
         */
        val enumKeyRemovals = mapOf(
            PET_DETAILS_CHATHEAD_ANIMATIONS_NORMAL to setOf(13089, 13090),
        )

        val cache = CacheDelegate(library)
        val decoder = EnumDecoder()
        val encoder = EnumEncoder()

        val fixed = mutableListOf<EnumDefinition>()
        val ids = enumFixes.keys + enumDefaultIntFixes.keys + enumKeyRemovals.keys
        for (id in ids) {
            val definition = EnumDefinition(id)
            val data = library.data(indexId, decoder.getArchive(id), decoder.getFile(id)) ?: continue
            val buffer = ArrayReader(data)
            decoder.readLoop(definition, buffer)
            enumFixes[id]?.let { fixes ->
                val map = definition.map!! as MutableMap
                for ((key, value) in fixes) {
                    map[key] = value
                }
            }
            enumKeyRemovals[id]?.let { removals ->
                val map = definition.map!! as MutableMap<Int, Any>
                for (key in removals) {
                    map.remove(key)
                }
            }
            enumDefaultIntFixes[id]?.let { definition.defaultInt = it }
            fixed.add(definition)
        }

        for (definition in fixed) {
            val writer = ArrayWriter(10000)
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
