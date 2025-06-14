package world.gregs.voidps.tools.map.render.load

import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.cache.definition.data.MapObject
import world.gregs.voidps.tools.map.render.raster.Raster
import world.gregs.voidps.type.Region
import java.awt.image.BufferedImage

class RegionManager(
    val tiles: Map<Int, MapDefinition>,
    private val regionRenderSize: Int = 3,
) {
    val width = regionRenderSize * 64
    val height = regionRenderSize * 64
    val scale = 4

    fun renderRegion(settings: MapTileSettings, currentLevel: Int): BufferedImage {
        val img = BufferedImage(width * scale, height * scale, BufferedImage.TYPE_INT_ARGB)
        val raster = Raster(img)
        val levels = settings.load()
        for (index in currentLevel until levels.size) {
            val level = levels[index]
            level.drawTiles(0, 0, width, height, currentLevel, settings, raster)
        }
        return img
    }

    fun loadTiles(regionX: Int, regionY: Int) {
        for (rX in regionX until regionX + regionRenderSize) {
            for (rY in regionY until regionY + regionRenderSize) {
                setOrLoadTiles(Region.id(rX, rY))
            }
        }
    }

    private fun setOrLoadTiles(region: Region): MapDefinition? = setOrLoadTiles(region.id)

    private fun setOrLoadTiles(regionId: Int): MapDefinition? = tiles[regionId]

    fun loadObjects(region: Region): MutableList<MapObject>? {
        val def = setOrLoadTiles(region) ?: return null
        return def.objects
    }
}
