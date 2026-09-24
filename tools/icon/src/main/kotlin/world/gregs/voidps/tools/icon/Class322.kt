package world.gregs.voidps.tools.icon

/* Class322 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class322(i: Int, i_9_: Int, i_10_: Int) {
    private val anInt4020: Int
    private var anInt4022: Int
    private val anInt4024: Int
    private var anInt4025 = 0
    private var anIntArrayArrayArray4029: Array<Array<IntArray>>?
    private var aClass348_Sub24Array4033: Array<Class348_Sub24?>?
    var aBoolean4035: Boolean
    private var aClass262_4021: Class262?

    fun method2557(i: Int, i_6_: Int): Array<IntArray>? {
        anInt4034++
        if (i >= -75) Companion.method2554((-61).toByte())
        if (anInt4020 != anInt4024) {
            if (anInt4020 == 1) {
                this.aBoolean4035 = i_6_ != anInt4022
                anInt4022 = i_6_
                return anIntArrayArrayArray4029!![0]
            }
            var class348_sub24 = aClass348_Sub24Array4033!![i_6_]
            if (class348_sub24 == null) {
                this.aBoolean4035 = true
                if (anInt4020 <= anInt4025) {
                    val class348_sub24_7_ = aClass262_4021!!.method1993(-126) as Class348_Sub24?
                    class348_sub24 = Class348_Sub24(i_6_, class348_sub24_7_!!.anInt6875)
                    aClass348_Sub24Array4033!![class348_sub24_7_.anInt6872] = null
                    class348_sub24_7_.unlink(56.toByte())
                } else {
                    class348_sub24 = Class348_Sub24(i_6_, anInt4025)
                    anInt4025++
                }
                aClass348_Sub24Array4033!![i_6_] = class348_sub24
            } else this.aBoolean4035 = false
            aClass262_4021!!.method2001(class348_sub24, -110)
            return (anIntArrayArrayArray4029!![class348_sub24.anInt6875])
        }
        this.aBoolean4035 = aClass348_Sub24Array4033!![i_6_] == null
        aClass348_Sub24Array4033!![i_6_] = Class341.aClass348_Sub24_4226
        return anIntArrayArrayArray4029!![i_6_]
    }

    fun method2558(i: Int) {
        anInt4019++
        if (i != 6144) anIntArrayArrayArray4029 = null
        aClass348_Sub24Array4033 = null
        anIntArrayArrayArray4029 = null
        aClass262_4021!!.method1996(99)
        aClass262_4021 = null
    }

    init {
        anInt4022 = -1
        aClass262_4021 = Class262()
        this.aBoolean4035 = false
        anInt4020 = i
        anInt4024 = i_9_
        aClass348_Sub24Array4033 = arrayOfNulls<Class348_Sub24>(anInt4024)
        anIntArrayArrayArray4029 = Array(anInt4020) { Array(3) { IntArray(i_10_) } }
    }

    companion object {
        var anInt4019: Int = 0
        var anInt4023: Int = 0
        var anInt4030: Int = 0
        var anInt4032: Int = -1
        var anInt4034: Int = 0
        fun method2554(i: Byte) {
            if (i.toInt() != -45) anInt4032 = 61
            anInt4030++
            if (Class312.anInt3931 == 1 || Class312.anInt3931 == 3 || (Class312.anInt3931 != Class83.anInt1447 && (Class312.anInt3931 == 0 || Class83.anInt1447 == 0))) {
                Class348_Sub32.anInt6930 = 0
                Class150.anInt2057 = 0
                Class282.aIterableHashTable_3654.method3481(0)
            }
            Class83.anInt1447 = Class312.anInt3931
        }
    }
}
