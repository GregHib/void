package world.gregs.voidps.tools.icon

/* Class350 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class JavaBillboardAttributes(var anInt4313: Int) {
    var anInt4307: Int = 0
    var anInt4308: Int = 0
    var anInt4309: Int = 0
    var anInt4310: Int = 0
    var anInt4311: Int = 128
    var anInt4312: Int = 0
    var anInt4314: Int = 128
    var anInt4316: Int = 0
    var anInt4317: Int = 0
    var anInt4320: Int = 0

    companion object {
        var anInt4315: Int = 0
        fun method3452(i: Int, i_0_: Byte, i_1_: Int): Int {
            if (i_0_.toInt() != -15) method3452(80, 123.toByte(), -88)
            anInt4315++
            val i_2_ = i_1_ + -1 and (i shr 31)
            return (i + (i ushr 31)) % i_1_ + i_2_
        }
    }
}
