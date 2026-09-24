package world.gregs.voidps.tools.icon

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
    private const val WIDTH = 36
    private const val HEIGHT = 32

    /** Toggle the black selection outline that item icons are normally drawn with.  */
    private const val OUTLINE = true

    /**
     * Ids outside the normal item definition range that still need dumping:
     * 799 is the "note" background/overlay, 13009 the "lent" item background.
     */
    private val EXTRA_IDS = intArrayOf(799, 13009)

    /**
     * Class348_Sub8.aHa6654 is constructed once very early (during the loading
     * progress bar, before Class348_Sub40_Sub4.aD9113 is set) and never rebuilt,
     * so it permanently carries a null material/texture provider - any item
     * icon whose model references a material (e.g. item 799) NPEs inside
     * Class64_Sub1 when drawn with it. Build our own small off-screen renderer
     * instead, the same way Class22.method294's icon-queue does, so it picks up
     * the real (by-then-initialised) texture provider.
     */
    private var renderer: Toolkit? = null

    private fun renderer(): Toolkit {
        if (renderer == null) {
            val canvas = Canvas()
            canvas.setSize(WIDTH, HEIGHT)
            renderer = Class104.method958(true, 0, Class348_Sub40_Sub4.Companion.aTextureSource9113, 0, canvas, Class136.aJs5_4796)
        }
        return renderer!!
    }

    @JvmOverloads
    fun dump(dir: File = File("item_sprites")) {
        dir.mkdirs()
        val count = Exception_Sub1.itemTypeList!!.num
        var dumped = 0
        for (id in 0..<count) {
            if (dumpItem(dir, id)) dumped++
        }
        for (id in EXTRA_IDS) {
            if (id >= count && dumpItem(dir, id)) dumped++
        }
        println("ItemSpriteDumper: wrote " + dumped + " item icons to " + dir.getAbsolutePath())
    }

    private fun dumpItem(dir: File?, id: Int): Boolean {
        try {
            val def = Exception_Sub1.itemTypeList!!.list(-104, id)
            val toolkit = renderer()
            val pixels = def.sprite(1, false, 0, toolkit, toolkit, null, null, 0, (-102).toByte(), if (OUTLINE) 1 else 0)
            if (pixels == null) return false
            val image = BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB)
            image.setRGB(0, 0, WIDTH, HEIGHT, pixels, 0, WIDTH)
            ImageIO.write(image, "png", File(dir, id.toString() + "_" + sanitize(def.name) + ".png"))
            return true
        } catch (ioexception: IOException) {
            return false
        } catch (runtimeexception: RuntimeException) {
            return false
        }
    }

    private fun sanitize(name: String?): String {
        if (name == null || name.length == 0) return "unnamed"
        return name.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
    }
}
