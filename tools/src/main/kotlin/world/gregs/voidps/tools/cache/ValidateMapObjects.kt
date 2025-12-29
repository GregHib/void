package world.gregs.voidps.tools.cache

import com.displee.cache.CacheLibrary
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.cache.definition.encoder.MapObjectEncoder
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.tools.map.MapDecoder
import world.gregs.voidps.type.Region

/**
 * Checks that all map objects are within the [ObjectDecoder]'s value
 * Any invalid objects are removed and the map re-encoded.
 */
object ValidateMapObjects {

    fun validateAll(cache: CacheLibrary, xteas: Xteas = Xteas()) {
        val mapDecoder = MapDecoder(xteas)
        mapDecoder.modified = false
        val delegate = CacheDelegate(cache)
        val definitions = mapDecoder.load(delegate)
        val max = ObjectDecoder(member = true, lowDetail = false).size(delegate)
        val mapEncoder = MapObjectEncoder()
        val writer = ArrayWriter(45_000)
        var removed = 0
        var rewrote = 0
        for (definition in definitions) {
            val region = Region(definition.id)

            var write = false
            val it = definition.objects.iterator()
            while (it.hasNext()) {
                val obj = it.next()
                if (obj.id >= max) {
//                    println("Removing invalid object $obj in $region")
                    it.remove()
                    write = true
                    removed++
                }
            }
            if (write) {
                writer.clear()
                with(mapEncoder) {
                    writer.encode(definition)
                }
                val data = writer.toArray()
                cache.put(Index.MAPS, "l${region.x}_${region.y}", data)
                rewrote++
            }
        }
        cache.update()
        println("Removed $removed ${"object".plural(removed)} from $rewrote ${"region".plural(rewrote)}.")
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val cache = CacheLibrary("./data/cache/test/")
        validateAll(cache)
    }
}
