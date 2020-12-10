package rs.dusk.tools.map.render.load

import rs.dusk.cache.Cache
import rs.dusk.engine.map.region.Region
import rs.dusk.engine.map.region.obj.GameObjectLoc
import rs.dusk.engine.map.region.obj.GameObjectMapDecoder
import rs.dusk.engine.map.region.obj.Xteas
import rs.dusk.engine.map.region.tile.TileData
import rs.dusk.engine.map.region.tile.TileDecoder
import rs.dusk.tools.map.render.raster.Raster
import java.awt.image.BufferedImage

class RegionManager(
    private val cache: Cache,
    private val tileDecoder: TileDecoder,
    private val xteas: Xteas,
    private val mapObjDecoder: GameObjectMapDecoder,
    private val regionRenderSize: Int = 3
) {
    val tiles = mutableMapOf<Int, Array<Array<Array<TileData?>>>>()
    val width = regionRenderSize * 64
    val height = regionRenderSize * 64
    val scale = 4

    fun renderRegion(settings: MapTileSettings): BufferedImage {
        val img = BufferedImage(width * scale, height * scale, BufferedImage.TYPE_INT_ARGB)
        val raster = Raster(img)
        val currentPlane = 0
        val planes = settings.load()
        planes.forEach {
            it.drawTiles(0, 0, width, height, currentPlane, settings, raster)
        }
        return img
    }

    fun loadTiles(regionX: Int, regionY: Int) {
        for (rX in regionX until regionX + regionRenderSize) {
            for (rY in regionY until regionY + regionRenderSize) {
                val regionId = Region.getId(rX, rY)
                setOrLoadTiles(regionId, rX, rY)
            }
        }
    }

    private fun setOrLoadTiles(region: Region): Array<Array<Array<TileData?>>>? {
        return setOrLoadTiles(region.id, region.x, region.y)
    }

    private fun setOrLoadTiles(regionId: Int, regionX: Int, regionY: Int): Array<Array<Array<TileData?>>>? {
        if (tiles.containsKey(regionId)) {
            return tiles[regionId]
        }
        val mapData = cache.getFile(5, "m${regionX}_${regionY}") ?: return null
        val tileData = tileDecoder.read(mapData)
        tiles[regionId] = tileData
        return tileData
    }

    fun loadObjects(region: Region): List<GameObjectLoc>? {
        val tiles = setOrLoadTiles(region) ?: return null
        val xtea = xteas[region.id]
        val locationData = cache.getFile(5, "l${region.x}_${region.y}", xtea)

        if (locationData == null) {
            println("Missing xteas for region ${region.id} [${xtea?.toList()}].")
            return null
        }

        return mapObjDecoder.read(region.x, region.y, locationData, tiles)
    }

}