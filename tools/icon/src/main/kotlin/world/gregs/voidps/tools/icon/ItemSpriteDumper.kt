package world.gregs.voidps.tools.icon

import world.gregs.voidps.tools.render.TextureOpVerticalGradient
import world.gregs.voidps.tools.render.TextureSource
import world.gregs.voidps.tools.render.Toolkit
import java.awt.Canvas
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

/**
 * Debug utility: renders every item's inventory icon (the same 36x32 raster
 * produced for bank/inventory/shop interfaces) and writes each one out as a
 * standalone PNG. Intended to be invoked once, after item definitions and
 * the model archive are loaded.
 */
internal object ItemSpriteDumper {
    fun method958(var_textureSource: TextureSource?, canvas: Canvas?): Toolkit {
        var i_62_ = 0
        var i_63_ = 0
        if (canvas != null) {
            val dimension = canvas.size
            i_63_ = dimension.height
            i_62_ = dimension.width
        }
        return Toolkit.method3692(i_63_, i_62_, var_textureSource, canvas)
    }

    var icons: ItemIconRenderer? = null

    private const val WIDTH = ItemIconRenderer.WIDTH
    private const val HEIGHT = ItemIconRenderer.HEIGHT

    /** Scale of the additional high resolution `{id}_hd.png` icons (72x64).  */
    private const val HD_SCALE = 2

    /**
     * Class348_Sub8.aHa6654 is constructed once very early (during the loading
     * progress bar, before Class348_Sub40_Sub4.aD9113 is set) and never rebuilt,
     * so it permanently carries a null material/texture provider - any item
     * icon whose model references a material (e.g. item 799) NPEs inside
     * Class64_Sub1 when drawn with it. Build our own small off-screen renderer
     * instead, the same way Class22.method294's icon-queue does, so it picks up
     * the real (by-then-initialised) texture provider.
     */
    private val renderers = mutableMapOf<Int, Toolkit>()

    /** One renderer per scale as the toolkit's pixel buffer is sized to its canvas. */
    private fun renderer(scale: Int): Toolkit = renderers.getOrPut(scale) {
        val canvas = Canvas()
        canvas.setSize(WIDTH * scale, HEIGHT * scale)
        method958(TextureOpVerticalGradient.aTextureSource9113, canvas)
    }

    @JvmOverloads
    fun dump(dir: File = File("item_sprites")) {
        dir.mkdirs()
        val count = icons!!.size
        var dumped = 0
        for (id in 0..<count) {
            if (dumpItem(dir, id, outline = true)) dumped++
            dumpItem(dir, id, HD_SCALE, outline = false)
        }
        println("ItemSpriteDumper: wrote " + dumped + " item icons to " + dir.absolutePath)
    }

    private fun dumpItem(dir: File?, id: Int, scale: Int = 1, outline: Boolean): Boolean {
        try {
            val def = icons!!.definition(id) ?: return false
            val pixels = icons!!.pixels(def, 1, false, 0, renderer(scale), if (outline) 1 else 0, scale) ?: return false
            val width = WIDTH * scale
            val height = HEIGHT * scale
            val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            image.setRGB(0, 0, width, height, pixels, 0, width)
            ImageIO.write(image, "png", File(dir, if (scale == 1) "$id.png" else "${id}_hd.png"))
            return true
        } catch (ioexception: IOException) {
            return false
        } catch (runtimeexception: RuntimeException) {
            return false
        }
    }
}
