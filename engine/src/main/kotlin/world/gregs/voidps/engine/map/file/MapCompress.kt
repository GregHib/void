package world.gregs.voidps.engine.map.file

import com.github.michaelbull.logging.InlineLogger
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.MapDecoder
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.update.batch.ChunkBatchUpdates
import world.gregs.voidps.engine.data.definition.extra.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.collision.CollisionReader
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.GameObjectCollision
import world.gregs.voidps.engine.map.collision.check
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.XteaLoader
import world.gregs.voidps.engine.map.region.Xteas
import java.io.File
import java.lang.ref.WeakReference

/**
 * Writes all map collision and objects into a [file] for faster load times via [MapExtract].
 */
class MapCompress(
    private val file: File,
    private val collisions: Collisions,
    private val decoder: MapDecoder
) : Runnable {

    private val logger = InlineLogger()

    override fun run() {
        val start = System.currentTimeMillis()
        var count = 0L
        val writer = BufferWriter(20_000_000)
        var total = 0
        val objects = mutableMapOf<Chunk, MutableList<ZoneObject>>()
        val chunks = mutableSetOf<Chunk>()
        val full = mutableSetOf<Chunk>()
        val regions = mutableListOf<Region>()
        for (regionX in 0 until 256) {
            for (regionY in 0 until 256) {
                val region = Region(regionX, regionY)
                val def = decoder.getOrNull(region.id) ?: continue
                var empty = true
                for (chunk in region.toRectangle().toChunks()) {
                    val tiles = (0 until 8).sumOf { x -> (0 until 8).count { y -> collisions.check(chunk.tile.x + x, chunk.tile.y + y, chunk.plane, CollisionFlag.FLOOR) } }
                    count += tiles
                    if (tiles == 64) {
                        full.add(chunk)
                        empty = false
                    } else if (tiles > 0) {
                        chunks.add(chunk)
                        empty = false
                    }
                }
                total += def.objects.size
                for (obj in def.objects) {
                    val x = region.tile.x + obj.x
                    val y = region.tile.y + obj.y
                    val tile = Tile(x, y)
                    empty = false
                    val chunkX = tile.chunk.tile.x
                    val chunkY = tile.chunk.tile.y
                    objects.getOrPut(tile.chunk) { mutableListOf() }.add(ZoneObject(obj.id, tile.x - chunkX, tile.y - chunkY, obj.plane, obj.type, obj.rotation))
                }
                if (!empty) {
                    regions.add(region)
                }
            }
        }
        writeObjects(writer, objects)
        writeChunks(writer, chunks)
        writeFilledChunks(writer, full)
        val data = writer.toArray()
        file.writeBytes(data)
        logger.info { "${regions.size} ${"map".plural(regions.size)} ($total objects, $count tiles) compressed to ${data.size / 1000000}mb in ${System.currentTimeMillis() - start}ms" }
    }

    private fun writeObjects(writer: Writer, objects: Map<Chunk, List<ZoneObject>>) {
        writer.writeInt(objects.size)
        objects.forEach { (chunk, objs) ->
            writer.writeInt(chunk.id)
            writer.writeShort(objs.size)
            for (obj in objs) {
                writer.writeInt(obj.value)
            }
        }
    }

    private fun writeChunks(writer: BufferWriter, chunks: MutableSet<Chunk>) {
        writer.writeInt(chunks.size)
        for (chunk in chunks) {
            writer.writeInt(chunk.id)
            val array = collisions.allocateIfAbsent(
                chunk.tile.x,
                chunk.tile.y,
                chunk.plane
            )
            var long = 0L
            for (i in 0 until 64) {
                if (array[i] and CollisionFlag.FLOOR != 0) {
                    long = long or (1L shl i)
                }
            }
            writer.writeLong(long)
        }
    }

    private fun writeFilledChunks(writer: BufferWriter, full: MutableSet<Chunk>) {
        writer.writeInt(full.size)
        for (chunk in full) {
            writer.writeInt(chunk.id)
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val cache = WeakReference(CacheDelegate("./data/cache"))
            val definitions = ObjectDefinitions(ObjectDecoder(cache.get()!!, true, false))
            val xteas = Xteas().apply { XteaLoader().load(this, "./data/xteas.dat") }
            val decoder = MapDecoder(cache.get()!!, xteas)
            val collisions = Collisions()
            val objects = GameObjects(GameObjectCollision(collisions), ChunkBatchUpdates())
            MapLoader(decoder, CollisionReader(collisions), definitions, objects).run()
            MapCompress(File("./data/map-test.dat"), collisions, decoder).run()
        }
    }
}