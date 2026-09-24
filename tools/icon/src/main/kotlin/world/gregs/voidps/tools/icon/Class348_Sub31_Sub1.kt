package world.gregs.voidps.tools.icon

import java.awt.Canvas
import java.awt.Image
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.awt.image.DirectColorModel
import java.awt.image.Raster
import java.util.*

/* Class348_Sub31_Sub1 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub31_Sub1 : Class348_Sub31() {
    private var aCanvas9065: Canvas? = null
    private var anImage9066: Image? = null
    private var aRectangle9067: Rectangle? = null

    override fun method3008(canvas: Canvas?, i: Int, i_6_: Int, i_7_: Int) {
        aCanvas9065 = canvas
        aRectangle9067 = Rectangle()
        this.anInt6917 = i
        this.anInt6920 = i_7_
        this.anIntArray6916 = IntArray((this.anInt6920 * this.anInt6917))
        val databufferint = DataBufferInt(this.anIntArray6916, (this.anIntArray6916).size)
        if (i_6_ > -42) method3008(null, 6, -14, 63)
        val directcolormodel = DirectColorModel(32, 16711680, 65280, 255)
        val writableraster = Raster.createWritableRaster((directcolormodel.createCompatibleSampleModel((this.anInt6917), (this.anInt6920))), databufferint, null)
        anImage9066 = BufferedImage(directcolormodel, writableraster, false, Hashtable<Any?, Any?>())
    }
}
