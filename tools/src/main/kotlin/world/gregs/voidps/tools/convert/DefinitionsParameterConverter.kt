package world.gregs.voidps.tools.convert

import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.fileProperties
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.*
import world.gregs.voidps.cache.Index.ITEMS
import world.gregs.voidps.cache.definition.Parameterized
import world.gregs.voidps.cache.definition.decoder.ItemDecoderFull
import world.gregs.voidps.cache.definition.decoder.NPCDecoderFull
import world.gregs.voidps.cache.definition.decoder.ObjectDecoderFull
import world.gregs.voidps.cache.definition.encoder.ItemEncoder
import world.gregs.voidps.cache.definition.encoder.NPCEncoder
import world.gregs.voidps.cache.definition.encoder.ObjectEncoder

object DefinitionsParameterConverter {
    @Suppress("USELESS_CAST")
    @JvmStatic
    fun main(args: Array<String>) {
        val cache667 = module {
            single { CacheDelegate("${System.getProperty("user.home")}/Downloads/rs634_cache/") as Cache }
        }
        val cache718 = module {
            single { CacheDelegate("${System.getProperty("user.home")}/Downloads/rs718_cache/") as Cache }
        }

        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cache718)
        }.koin

        val itemDefinitions718 = ItemDecoder718().loadCache(koin.get())
        val npcDefinitions718 = NPCDecoder718().loadCache(koin.get())
        val objectDefinitions718 = ObjectDecoder718().loadCache(koin.get())

        koin.unloadModules(listOf(cache718))
        koin.loadModules(listOf(cache667))

        val cache = koin.get<Cache>() as CacheDelegate

        val itemDecoder = ItemDecoderFull()
        val itemDefinitions = itemDecoder.loadCache(koin.get())
        val itemEncoder = ItemEncoder()
        val itemCount = definition(itemDecoder, itemEncoder, itemDefinitions, itemDefinitions, itemDefinitions718, cache)
        println("Parameters transferred from $itemCount item definitions.")

        val npcDecoder = NPCDecoderFull(members = false)
        val npcDefinitions = npcDecoder.loadCache(koin.get())
        val npcDecoderMembers = NPCDecoderFull(members = true)
        val npcDefinitionsMembers = npcDecoderMembers.loadCache(koin.get())
        val npcEncoder = NPCEncoder()
        val npcCount = definition(npcDecoder, npcEncoder, npcDefinitions, npcDefinitionsMembers, npcDefinitions718, cache)
        println("Parameters transferred from $npcCount npc definitions.")

        val objectDecoder = ObjectDecoderFull()
        val objectDefinitions = objectDecoder.loadCache(koin.get())
        val objectDecoderMembers = ObjectDecoderFull()
        val objectDefinitionsMembers = objectDecoderMembers.loadCache(koin.get())
        val objectEncoder = ObjectEncoder()
        val objectCount = definition(objectDecoder, objectEncoder, objectDefinitions, objectDefinitionsMembers, objectDefinitions718, cache)
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
        cache: Cache
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
                }
            }
            if (modified) {
                val writer = BufferWriter(capacity = 2048)
                with(encoder) {
                    writer.encode(def, membersDef)
                }
                cache.write(ITEMS, decoder.getArchive(id), decoder.getFile(id), writer.toArray())
                count++
            }
        }
        return count
    }
}