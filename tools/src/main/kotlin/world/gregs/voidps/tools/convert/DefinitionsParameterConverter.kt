package world.gregs.voidps.tools.convert

import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.cache.*
import world.gregs.voidps.cache.Index.ITEMS
import world.gregs.voidps.cache.Index.NPCS
import world.gregs.voidps.cache.Index.OBJECTS
import world.gregs.voidps.cache.definition.Parameterized
import world.gregs.voidps.cache.definition.decoder.ItemDecoderFull
import world.gregs.voidps.cache.definition.decoder.NPCDecoderFull
import world.gregs.voidps.cache.definition.decoder.ObjectDecoderFull
import world.gregs.voidps.cache.definition.encoder.ItemEncoder
import world.gregs.voidps.cache.definition.encoder.NPCEncoder
import world.gregs.voidps.cache.definition.encoder.ObjectEncoder
import java.io.File

object DefinitionsParameterConverter {

    fun convert(target: File, other: File) {
        val cache = CacheDelegate(target.path)
        val otherCache = CacheDelegate(other.path)

        val itemDefinitions718 = ItemDecoder718().load(otherCache)
        val npcDefinitions718 = NPCDecoder718().load(otherCache)
        val objectDefinitions718 = ObjectDecoder718().load(otherCache)

        val itemDecoder = ItemDecoderFull()
        val itemDefinitions = itemDecoder.load(cache)
        val itemEncoder = ItemEncoder()
        val itemCount = definition(itemDecoder, itemEncoder, itemDefinitions, itemDefinitions, itemDefinitions718, cache, ITEMS)
        println("Parameters transferred from $itemCount item definitions.")

        val npcDecoder = NPCDecoderFull(members = false)
        val npcDefinitions = npcDecoder.load(cache)
        val npcDecoderMembers = NPCDecoderFull(members = true)
        val npcDefinitionsMembers = npcDecoderMembers.load(cache)
        val npcEncoder = NPCEncoder()
        val npcCount = definition(npcDecoder, npcEncoder, npcDefinitions, npcDefinitionsMembers, npcDefinitions718, cache, NPCS)
        println("Parameters transferred from $npcCount npc definitions.")

        val objectDecoder = ObjectDecoderFull(members = false)
        val objectDefinitions = objectDecoder.load(cache)
        val objectDecoderMembers = ObjectDecoderFull(members = true)
        val objectDefinitionsMembers = objectDecoderMembers.load(cache)
        val objectEncoder = ObjectEncoder()
        val objectCount = definition(objectDecoder, objectEncoder, objectDefinitions, objectDefinitionsMembers, objectDefinitions718, cache, OBJECTS)
        println("Parameters transferred from $objectCount object definitions.")

        println("Writing changes to cache...")
        cache.update()
    }

    private fun <T> definition(
        decoder: DefinitionDecoder<T>,
        encoder: DefinitionEncoder<T>,
        definitions: Array<T>,
        members: Array<T>,
        definitions718: Array<T>,
        cache: Cache,
        index: Int,
    ): Int where T : Definition, T : Parameterized {
        var count = 0
        for (id in definitions.indices) {
            val def = definitions.getOrNull(id) ?: continue
            val membersDef = members.getOrNull(id) ?: continue
            val def718 = definitions718.getOrNull(id) ?: continue
            val params718 = def718.params ?: continue
            val params = def.params?.toMutableMap() ?: mutableMapOf()
            def.params = params
            var modified = false
            for ((key, value) in params718) {
                if (!params.containsKey(key)) {
                    params[key] = value
                    modified = true
                } else if (params[key] != value) {
//                    println("Override $key = ${params[key]} $value")
                }
            }
            if (modified) {
                val writer = ArrayWriter(capacity = 2048)
                with(encoder) {
                    writer.encode(def, membersDef)
                }
                cache.write(index, decoder.getArchive(id), decoder.getFile(id), writer.toArray())
                count++
            }
        }
        return count
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val cache = File("${System.getProperty("user.home")}/Downloads/rs634_cache/")
        val other = File("${System.getProperty("user.home")}/Downloads/rs718_cache/")
        convert(cache, other)
    }
}
