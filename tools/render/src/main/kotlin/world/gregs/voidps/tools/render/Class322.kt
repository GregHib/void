package world.gregs.voidps.tools.render

/* Class322 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

class Class322(i: Int, i_9_: Int, i_10_: Int) {
    private val anInt4020: Int
    private var anInt4022: Int
    private val anInt4024: Int
    private var anInt4025 = 0
    private var anIntArrayArrayArray4029: Array<Array<IntArray>>?
    private var aClass348_Sub24Array4033: Array<Class348_Sub24?>?
    var aBoolean4035: Boolean
    private var aClass262_4021: Class262?

    fun method2557(i_6_: Int): Array<IntArray>? {
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
                    val class348_sub24_7_ = aClass262_4021!!.method1993() as Class348_Sub24?
                    class348_sub24 = Class348_Sub24(i_6_, class348_sub24_7_!!.anInt6875)
                    aClass348_Sub24Array4033!![class348_sub24_7_.anInt6872] = null
                    class348_sub24_7_.unlink()
                } else {
                    class348_sub24 = Class348_Sub24(i_6_, anInt4025)
                    anInt4025++
                }
                aClass348_Sub24Array4033!![i_6_] = class348_sub24
            } else this.aBoolean4035 = false
            aClass262_4021!!.method2001(class348_sub24)
            return (anIntArrayArrayArray4029!![class348_sub24.anInt6875])
        }
        this.aBoolean4035 = aClass348_Sub24Array4033!![i_6_] == null
        aClass348_Sub24Array4033!![i_6_] = aClass348_Sub24_4226
        return anIntArrayArrayArray4029!![i_6_]
    }

    fun method2558() {
        aClass348_Sub24Array4033 = null
        anIntArrayArrayArray4029 = null
        aClass262_4021!!.method1996()
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
        var aClass348_Sub24_4226: Class348_Sub24? = null
    }
}
