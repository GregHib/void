package world.gregs.voidps.tools.render

/* Class6 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class JavaBillboardFace(var anInt144: Int, i_46_: Int, i_47_: Int, i_48_: Int, i_49_: Int, i_50_: Int, i_51_: Int, i_52_: Int, i_53_: Int, var aBoolean145: Boolean, var anInt154: Int) {
    /* Minimal stub: only the fields/method/constructor that Class64_Sub1's
     * kept code actually reads/calls are included (verbatim from the
     * original), matching the full Class6(...) constructor signature used
     * at the aClass6Array5361[...] = new Class6(...) call site. */
    var aShort143: Short
    var aShort146: Short
    var aByte148: Byte
    var aShort150: Short
    var aByte156: Byte

    init {
        this.aByte156 = i_53_.toByte()
        this.aShort143 = i_50_.toShort()
        this.aByte148 = i_52_.toByte()
        this.aShort146 = i_51_.toShort()
        this.aShort150 = i_49_.toShort()
    }

    companion object {
        fun method206(i: Int, i_31_: Int, i_32_: Int): Int {
            var i_31_ = i_31_
            val i_33_ = i_31_ ushr 24
            val i_34_ = -i_33_ + i_32_
            i_31_ = (0xff0000 and (i_31_ and 0xff00) * i_33_ or ((0xff00ff and i_31_) * i_33_ and 0xff00ff.inv())) ushr 8
            return i_31_ + (((i and 0xff00) * i_34_ and 0xff0000 or (0xff00ff.inv() and (0xff00ff and i) * i_34_)) ushr 8)
        }
    }
}
