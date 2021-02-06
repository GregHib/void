package world.gregs.voidps.cache.format.definition

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import org.koin.core.context.startKoin
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Indices
import world.gregs.voidps.cache.definition.data.ItemDefinition2
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.cache.format.definition.Test.putIfAbsent
import kotlin.system.measureNanoTime

@OptIn(ExperimentalSerializationApi::class)
object Test {
    @JvmStatic
    fun main(args: Array<String>) {
        val cache = CacheDelegate("./data/667", "1", "1")
        val decoder = ItemDecoder(cache)

        println(measureNanoTime { decoder.get(4151) })
        repeat(10) {
            decoder.clear()
            println(measureNanoTime { decoder.get(4151) })
        }
        println()
        println(measureNanoTime { cache.getItemDef(4151) })
        repeat(10) {
            println(measureNanoTime { cache.getItemDef(4151) })
        }
        val def = cache.getItemDef(4151)
        println(def)
        var data = cache.getFile(Indices.ITEMS, 4151 ushr 8, 4151 and 0xff)
        println("Original data ${data.contentToString()}")
        data = Definition.encodeToByteArray(def)
        println("Back to data ${data.contentToString()}")
        println("Back again ${Definition.decodeFromByteArray<ItemDefinition2>(data)}")
    }

    fun Cache.getItemDef(id: Int): ItemDefinition2 {
        val data = getFile(Indices.ITEMS, id ushr 8, id and 0xff) ?: return ItemDefinition2()
        val def: ItemDefinition2 = Definition.decodeFromByteArray(data)
        def.id = id
        def.floorOptions.putIfAbsent(2, "Take")
        def.floorOptions.putIfAbsent(5, "Examine")
        def.options.putIfAbsent(4, "Drop")
        return def
    }

    fun <T> Array<T?>.putIfAbsent(index: Int, value: T) {
        if (this[index] == null) {
            this[index] = value
        }
    }
}