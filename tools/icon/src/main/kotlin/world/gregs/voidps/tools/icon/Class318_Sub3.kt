package world.gregs.voidps.tools.icon

/* Class318_Sub3 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class318_Sub3 : Class318() {
    var aBoolean6401: Boolean = false
    var anInt6402: Int = 0
    var anInt6403: Int = 0
    var anInt6404: Int = 0
    var anInt6405: Int = 0
    var anInt6406: Int = 0

    fun method2500(i: Int, i_0_: Int): Boolean {
        if (!this.aBoolean6401) return false
        val i_1_ = (this.anInt6406 - this.anInt6405)
        val i_2_ = (this.anInt6404 - this.anInt6402)
        val i_3_ = i_1_ * i_1_ + i_2_ * i_2_
        var i_4_ = (i * i_1_ + i_0_ * i_2_ - (this.anInt6405 * i_1_ + this.anInt6402 * i_2_))
        if (i_4_ <= 0) {
            val i_5_ = this.anInt6405 - i
            val i_6_ = this.anInt6402 - i_0_
            return i_5_ * i_5_ + i_6_ * i_6_ < (this.anInt6403 * this.anInt6403)
        }
        if (i_4_ > i_3_) {
            val i_7_ = this.anInt6406 - i
            val i_8_ = this.anInt6404 - i_0_
            return i_7_ * i_7_ + i_8_ * i_8_ < (this.anInt6403 * this.anInt6403)
        }
        i_4_ = (i_4_ shl 10) / i_3_
        val i_9_ = this.anInt6405 + (i_1_ * i_4_ shr 10) - i
        val i_10_ = this.anInt6402 + (i_2_ * i_4_ shr 10) - i_0_
        return i_9_ * i_9_ + i_10_ * i_10_ < (this.anInt6403 * this.anInt6403)
    }
}
