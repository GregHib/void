package world.gregs.voidps.cache

import com.displee.cache.CacheLibrary
import kotlinx.serialization.*
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import java.io.File

object Flatten {

    object ParameterSerializer : KSerializer<Any> {
        override fun deserialize(decoder: Decoder): Any {
            if(decoder is JsonDecoder) {
                val element = decoder.decodeJsonElement().jsonPrimitive
                return if (element.isString) {
                    element.toString()
                } else {
                    element.int
                }
            }
            return decoder.decodeString()
        }

        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("item")

        override fun serialize(encoder: Encoder, value: Any) {
            if (value is Int) {
                encoder.encodeInt(value)
            } else if(value is String) {
                encoder.encodeString(value)
            }
        }

    }

    private val indices = mapOf(
        19 to "items"
    )

    private val archives = mapOf<Int, String>(
    )

    private val files = mapOf<Int, String>(
    )

    private val items = mapOf<Int, String>(
        1704 to "amulet_of_glory"
    )

    @JvmStatic
    fun main(args: Array<String>) {
        val path = "./data/cache/"
        val cache = CacheLibrary(path)

        val decoder = ItemDecoder(CacheDelegate(path, "1", "1"))

        val dir = File(path, "flat/")
        if (dir.exists()) {
//            val old = File(path, "flat.old/")
//            old.delete()
//            dir.renameTo(old)
//            dir.delete()
        }
        val format = Json {
            prettyPrint = true
            serializersModule = SerializersModule {
                contextual(String.serializer())
                contextual(Int.serializer())
                contextual(Any::class, ParameterSerializer)
            }
        }
        dir.mkdir()
        for (index in cache.indices()) {
            val indexName = indices[index.id] ?: continue
            val indexDir = File(dir, "$indexName/")
            indexDir.mkdir()
            println("Index ${index.id} ${index.archives().size}")
            for (archive in index.archives()) {
                println("Archive ${archive.id} ${archive.files.size}")
                for (file in archive.files()) {
                    val itemId = ((archive.id shl 8) + file.id)
                    val def = decoder.getOrNull(itemId) ?: continue
                    val itemName = items.getOrDefault(itemId, itemId.toString())
                    val fileDir = File(indexDir, "$itemName.json")
                    fileDir.writeText(format.encodeToString(def))
                }
            }
        }
    }
}