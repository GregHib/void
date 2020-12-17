package rs.dusk.tools.map.process

import rs.dusk.cache.Cache
import rs.dusk.engine.map.region.Region
import rs.dusk.engine.map.region.obj.GameObjectLoc
import rs.dusk.engine.map.region.obj.GameObjectMapDecoder
import rs.dusk.engine.map.region.obj.Xteas
import rs.dusk.engine.map.region.tile.TileData
import rs.dusk.engine.map.region.tile.TileDecoder
import rs.dusk.tools.Pipeline

abstract class ObjectProcessor(
    private val tileDecoder: TileDecoder,
    private val mapDecoder: GameObjectMapDecoder,
    private val xteas: Xteas,
    private val cache: Cache
) : Pipeline.Modifier<Region> {
    override fun process(region: Region) {
        val mapData = cache.getFile(5, "m${region.x}_${region.y}") ?: return
        val tiles = tileDecoder.read(mapData)
        val xtea = xteas[region.id]
        val locationData = cache.getFile(5, "l${region.x}_${region.y}", xtea)

        if (locationData == null) {
//            println("Missing xteas for region ${region.id} [${xtea?.toList()}].")
            return
        }

        val objects = mapDecoder.read(region.x, region.y, locationData, tiles)
        if(objects != null)
        process(region, tiles, objects)
    }

    abstract fun process(region: Region, tiles: Array<Array<Array<TileData?>>>, objects: List<GameObjectLoc>)
}