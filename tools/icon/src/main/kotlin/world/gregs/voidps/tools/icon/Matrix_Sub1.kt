package world.gregs.voidps.tools.icon

/* Class101_Sub1 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Matrix_Sub1 : Matrix() {
    var aFloat5655: Float = 0f
    var aFloat5662: Float = 0f
    var aFloat5664: Float = 0f
    var aFloat5666: Float = 0f
    var aFloat5669: Float = 0f
    var aFloat5672: Float = 0f
    var aFloat5673: Float = 0f
    var aFloat5678: Float = 0f
    var aFloat5680: Float = 0f
    var aFloat5681: Float = 0f
    var aFloat5685: Float = 0f
    var aFloat5686: Float = 0f
    override fun makeIdentity() {
        anInt5688++
        this.aFloat5664 = 1.0f
        this.aFloat5678 = this.aFloat5664
        this.aFloat5672 = this.aFloat5678
        this.aFloat5681 = 0.0f
        this.aFloat5685 = this.aFloat5681
        this.aFloat5686 = this.aFloat5685
        this.aFloat5666 = this.aFloat5686
        this.aFloat5669 = this.aFloat5666
        this.aFloat5680 = this.aFloat5669
        this.aFloat5673 = this.aFloat5680
        this.aFloat5662 = this.aFloat5673
        this.aFloat5655 = this.aFloat5662
    }

    override fun translate(i: Int, i_17_: Int, i_18_: Int) {
        this.aFloat5685 += i_17_.toFloat()
        this.aFloat5681 += i_18_.toFloat()
        this.aFloat5686 += i.toFloat()
        anInt5661++
    }

    override fun rotateAxisX(i: Int) {
        anInt5658++
        val f = Class239_Sub4.aFloatArray5876[0x3fff and i]
        val f_24_ = Class239_Sub4.aFloatArray5874[0x3fff and i]
        val f_25_ = this.aFloat5655
        val f_26_ = this.aFloat5678
        val f_27_ = this.aFloat5666
        this.aFloat5655 = f_25_ * f - this.aFloat5662 * f_24_
        val f_28_ = this.aFloat5685
        this.aFloat5662 = f_24_ * f_25_ + this.aFloat5662 * f
        this.aFloat5678 = f * f_26_ - this.aFloat5680 * f_24_
        this.aFloat5666 = -(f_24_ * this.aFloat5664) + f * f_27_
        this.aFloat5680 = this.aFloat5680 * f + f_24_ * f_26_
        this.aFloat5685 = f * f_28_ - f_24_ * this.aFloat5681
        this.aFloat5664 = f * this.aFloat5664 + f_27_ * f_24_
        this.aFloat5681 = f_28_ * f_24_ + f * this.aFloat5681
    }

    override fun makeRotationZ(i: Int) {
        anInt5667++
        this.aFloat5664 = 1.0f
        this.aFloat5678 = Class239_Sub4.aFloatArray5876[0x3fff and i]
        this.aFloat5672 = this.aFloat5678
        this.aFloat5655 = Class239_Sub4.aFloatArray5874[0x3fff and i]
        this.aFloat5681 = 0.0f
        this.aFloat5680 = this.aFloat5681
        this.aFloat5662 = this.aFloat5680
        this.aFloat5685 = this.aFloat5662
        this.aFloat5666 = this.aFloat5685
        this.aFloat5686 = this.aFloat5666
        this.aFloat5669 = this.aFloat5686
        this.aFloat5673 = -this.aFloat5655
    }

    override fun makeAxisY(i: Int) {
        anInt5659++
        val f = Class239_Sub4.aFloatArray5876[0x3fff and i]
        val f_32_ = Class239_Sub4.aFloatArray5874[i and 0x3fff]
        val f_33_ = this.aFloat5672
        val f_34_ = this.aFloat5673
        val f_35_ = this.aFloat5669
        this.aFloat5672 = f_33_ * f + f_32_ * this.aFloat5662
        val f_36_ = this.aFloat5686
        this.aFloat5673 = f * f_34_ + f_32_ * this.aFloat5680
        this.aFloat5662 = -(f_32_ * f_33_) + this.aFloat5662 * f
        this.aFloat5669 = f * f_35_ + f_32_ * this.aFloat5664
        this.aFloat5680 = -(f_32_ * f_34_) + f * this.aFloat5680
        this.aFloat5664 = -(f_32_ * f_35_) + f * this.aFloat5664
        this.aFloat5686 = f_36_ * f + f_32_ * this.aFloat5681
        this.aFloat5681 = f * this.aFloat5681 - f_32_ * f_36_
    }

    init {
        makeIdentity()
    }

    companion object {
        var anInt5658: Int = 0
        var aLong5663: Long = 0
        var anInt5659: Int = 0
        var anInt5661: Int = 0
        var anInt5667: Int = 0
        var anInt5688: Int = 0
    }
}
