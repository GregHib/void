package world.gregs.voidps.tools.render

import java.awt.Canvas
import java.awt.Graphics
import java.awt.Image
import java.awt.image.ColorModel
import java.awt.image.DirectColorModel
import java.awt.image.ImageConsumer
import java.awt.image.ImageProducer

internal class Class348_Sub31_Sub2 : Class348_Sub31(), ImageProducer {
    private var aCanvas9073: Canvas? = null
    private var anImage9075: Image? = null
    private var aColorModel9078: ColorModel? = null
    private var anImageConsumer9083: ImageConsumer? = null

    @Synchronized
    override fun removeConsumer(imageconsumer: ImageConsumer?) {
        if (imageconsumer === anImageConsumer9083) anImageConsumer9083 = null
    }

    override fun startProduction(imageconsumer: ImageConsumer) {
        addConsumer(imageconsumer)
    }

    @Synchronized
    private fun method3017() {
        if (anImageConsumer9083 != null) {
            anImageConsumer9083!!.setPixels(0, 0, this.anInt6917, this.anInt6920, aColorModel9078, (this.anIntArray6916), 0, this.anInt6917)
            anImageConsumer9083!!.imageComplete(2)
        }
    }

    @Synchronized
    override fun addConsumer(imageconsumer: ImageConsumer) {
        anImageConsumer9083 = imageconsumer
        imageconsumer.setDimensions(this.anInt6917, this.anInt6920)
        imageconsumer.setProperties(null)
        imageconsumer.setColorModel(aColorModel9078)
        imageconsumer.setHints(14)
    }

    @Synchronized
    override fun isConsumer(imageconsumer: ImageConsumer?): Boolean {
        return anImageConsumer9083 === imageconsumer
    }

    override fun method3008(canvas: Canvas?, i: Int, i_12_: Int) {
        this.anInt6920 = i_12_
        this.anInt6917 = i
        aCanvas9073 = canvas
        this.anIntArray6916 = IntArray((this.anInt6917 * this.anInt6920))
        aColorModel9078 = DirectColorModel(32, 16711680, 65280, 255)
        anImage9075 = aCanvas9073!!.createImage(this)
        method3017()
        aCanvas9073!!.prepareImage(anImage9075, aCanvas9073)
        method3017()
        aCanvas9073!!.prepareImage(anImage9075, aCanvas9073)
        method3017()
        aCanvas9073!!.prepareImage(anImage9075, aCanvas9073)
    }

    override fun requestTopDownLeftRightResend(imageconsumer: ImageConsumer?) {
    }

}
