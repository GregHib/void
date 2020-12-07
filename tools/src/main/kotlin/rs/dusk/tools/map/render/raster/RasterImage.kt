package rs.dusk.tools.map.render.raster

interface RasterImage {

    val biWidth: Int
    val biHeight: Int

    fun get(x: Int, y: Int): Int

    fun set(x: Int, y: Int, value: Int)
}