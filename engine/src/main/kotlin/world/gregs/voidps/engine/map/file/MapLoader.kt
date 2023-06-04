package world.gregs.voidps.engine.map.file

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.definition.decoder.MapDecoder
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.data.definition.extra.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.map.collision.CollisionReader
import world.gregs.voidps.engine.map.region.Region
import kotlin.system.measureTimeMillis

/**
 * Loads all maps from the cache into memory via [MapDecoder]
 * Note: using a compressed file and [MapExtract] is preferred
 */
class MapLoader(
    private val decoder: MapDecoder,
    private val reader: CollisionReader,
    private val definitions: ObjectDefinitions,
    private val objects: GameObjects
) : Runnable {

    private val logger = InlineLogger()

    override fun run() {
        var count = 0
        val took = measureTimeMillis {
            for (x in 0 until 256) {
                for (y in 0 until 256) {
                    val region = Region(x, y)
                    if (load(region)) {
                        count++
                    }
                }
            }
        }
        logger.info { "Loaded $count ${"region".plural(count)} from cache in ${took}ms" }
    }

    fun load(region: Region): Boolean {
        val def = decoder.getOrNull(region.id) ?: return false
        reader.read(region, def)
        for (location in def.objects) {
            val definition = definitions.get(location.id)
            objects.set(location.id, region.tile.x + location.x, region.tile.y + location.y, location.plane, location.type, location.rotation, definition)
        }
        return true
    }
}