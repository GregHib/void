package world.gregs.voidps.tools.icon

import java.awt.Canvas
import java.awt.Graphics
import java.awt.Image
import java.awt.image.ColorModel
import java.awt.image.DirectColorModel
import java.awt.image.ImageConsumer
import java.awt.image.ImageProducer

/* Class348_Sub31_Sub2 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*
* Trimmed: method3016 (static cleanup, pulls in Class357) dropped - not
* needed by Class110.method1035 (genuine), which only ever constructs this
* class in a Throwable fallback path (AWT peer creation failure) and calls
* method3008 on it.
*/

internal class Class348_Sub31_Sub2 : Class348_Sub31(), ImageProducer {
    private var aCanvas9073: Canvas? = null
    private var anImage9075: Image? = null
    private var aColorModel9078: ColorModel? = null
    private var anImageConsumer9083: ImageConsumer? = null

    @Synchronized
    private fun method3015(i: Int, i_0_: Int, i_1_: Int, i_2_: Int, i_3_: Int) {
        anInt9068++
        if (anImageConsumer9083 != null) {
            anImageConsumer9083!!.setPixels(i_1_, i_2_, i_3_, i, aColorModel9078, (this.anIntArray6916), i_2_ * (this.anInt6917) + i_1_, this.anInt6917)
            anImageConsumer9083!!.imageComplete(2)
        }
    }

    @Synchronized
    override fun removeConsumer(imageconsumer: ImageConsumer?) {
        if (imageconsumer === anImageConsumer9083) anImageConsumer9083 = null
        anInt9081++
    }

    override fun startProduction(imageconsumer: ImageConsumer) {
        addConsumer(imageconsumer)
        anInt9076++
    }

    @Synchronized
    private fun method3017(i: Byte) {
        anInt9071++
        if (anImageConsumer9083 != null) {
            anImageConsumer9083!!.setPixels(0, 0, this.anInt6917, this.anInt6920, aColorModel9078, (this.anIntArray6916), 0, this.anInt6917)
            anImageConsumer9083!!.imageComplete(2)
        }
    }

    @Synchronized
    override fun addConsumer(imageconsumer: ImageConsumer) {
        anInt9072++
        anImageConsumer9083 = imageconsumer
        imageconsumer.setDimensions(this.anInt6917, this.anInt6920)
        imageconsumer.setProperties(null)
        imageconsumer.setColorModel(aColorModel9078)
        imageconsumer.setHints(14)
    }

    @Synchronized
    override fun isConsumer(imageconsumer: ImageConsumer?): Boolean {
        anInt9080++
        return anImageConsumer9083 === imageconsumer
    }

    fun method3011(i: Int, i_5_: Int, i_6_: Int, graphics: Graphics, i_7_: Int, i_8_: Int, i_9_: Int, i_10_: Int) {
        anInt9074++
        method3015(i_6_, 25786, i_5_, i_10_, i_9_)
        val shape = graphics.getClip()
        graphics.clipRect(i_8_, i, i_9_, i_6_)
        graphics.drawImage(anImage9075, i_8_ - i_5_, -i_10_ + i, aCanvas9073)
        graphics.setClip(shape)
    }

    override fun method3008(canvas: Canvas?, i: Int, i_11_: Int, i_12_: Int) {
        this.anInt6920 = i_12_
        anInt9079++
        this.anInt6917 = i
        aCanvas9073 = canvas
        this.anIntArray6916 = IntArray((this.anInt6917 * this.anInt6920))
        aColorModel9078 = DirectColorModel(32, 16711680, 65280, 255)
        if (i_11_ <= -42) {
            anImage9075 = aCanvas9073!!.createImage(this)
            method3017((-117).toByte())
            aCanvas9073!!.prepareImage(anImage9075, aCanvas9073)
            method3017(86.toByte())
            aCanvas9073!!.prepareImage(anImage9075, aCanvas9073)
            method3017(51.toByte())
            aCanvas9073!!.prepareImage(anImage9075, aCanvas9073)
        }
    }

    override fun requestTopDownLeftRightResend(imageconsumer: ImageConsumer?) {
        anInt9070++
    }

    companion object {
        var anInt9068: Int = 0
        var anInt9070: Int = 0
        var anInt9071: Int = 0
        var anInt9072: Int = 0
        var anInt9074: Int = 0
        var anInt9076: Int = 0
        var anInt9077: Int = 0
        var anInt9079: Int = 0
        var anInt9080: Int = 0
        var anInt9081: Int = 0
    }
}
