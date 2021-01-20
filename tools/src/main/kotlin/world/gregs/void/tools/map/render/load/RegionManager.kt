package world.gregs.void.tools.map.render.load

import world.gregs.void.cache.definition.data.MapDefinition
import world.gregs.void.cache.definition.data.MapObject
import world.gregs.void.cache.definition.decoder.MapDecoder
import world.gregs.void.engine.map.region.Region
import world.gregs.void.tools.map.render.raster.Raster
import java.awt.image.BufferedImage

class RegionManager(
    private val mapDecoder: MapDecoder,
    private val regionRenderSize: Int = 3
) {
    val tiles = mutableMapOf<Int, MapDefinition?>()
    val width = regionRenderSize * 64
    val height = regionRenderSize * 64
    val scale = 4

    fun renderRegion(settings: MapTileSettings, currentPlane: Int): BufferedImage {
        val img = BufferedImage(width * scale, height * scale, BufferedImage.TYPE_INT_ARGB)
        val raster = Raster(img)
        val planes = settings.load()
        for (index in currentPlane until planes.size) {
            val plane = planes[index]
            plane.drawTiles(0, 0, width, height, currentPlane, settings, raster)
        }
        return img
    }

    fun loadTiles(regionX: Int, regionY: Int) {
        for (rX in regionX until regionX + regionRenderSize) {
            for (rY in regionY until regionY + regionRenderSize) {
                setOrLoadTiles(Region.getId(rX, rY))
            }
        }
    }

    private fun setOrLoadTiles(region: Region): MapDefinition? {
        return setOrLoadTiles(region.id)
    }

    private fun setOrLoadTiles(regionId: Int): MapDefinition? {
        if (tiles.containsKey(regionId)) {
            return tiles[regionId]
        }
        val def = mapDecoder.getOrNull(regionId)
        if(def == null || def.objects.isEmpty()) {
            return null
        }
        tiles[regionId] = def
        return def
    }

    fun loadObjects(region: Region): MutableList<MapObject>? {
        val def = setOrLoadTiles(region) ?: return null
        return def.objects
    }

}