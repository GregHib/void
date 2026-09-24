package world.gregs.voidps.tools.icon

/* Class348_Sub40_Sub32 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub32 : Class348_Sub40(1, false) {
    override fun method3047(i: Int, i_9_: Int): Array<IntArray> {
        if (i_9_ != -1564599039) method3133(4.toByte())
        anInt9417++
        val `is` = this.aClass322_7033!!.method2557(-78, i)
        if (this.aClass322_7033!!.aBoolean4035) {
            val is_10_ = this.method3039((-104).toByte(), i, 0)
            val is_11_ = is_10_!![0]
            val is_12_ = is_10_[1]
            val is_13_ = is_10_[2]
            val is_14_ = `is`!![0]
            val is_15_ = `is`[1]
            val is_16_ = `is`[2]
            var i_17_ = 0
            while (Class348_Sub40_Sub6.Companion.anInt9139 > i_17_) {
                is_14_[i_17_] = -is_11_[i_17_] + 4096
                is_15_[i_17_] = 4096 + -is_12_[i_17_]
                is_16_[i_17_] = -is_13_[i_17_] + 4096
                i_17_++
            }
        }
        return `is`!!
    }

    companion object {
        var anInt9417: Int = 0
        private var aShortArray9421: ShortArray? = shortArrayOf(-10304, 9104, 25485, 4620, 4540)
        private var aShortArray9422: ShortArray? = shortArrayOf(-1, -1, -1, -1, -1)
        private var aShortArray9423: ShortArray? = shortArrayOf(6798, 8741, 25238, 4626, 4550)
        var aShortArrayArray9424: Array<ShortArray?>? = arrayOf<ShortArray?>(aShortArray9423, aShortArray9421, aShortArray9422)

        fun method3133(i: Byte) {
            aShortArray9423 = null
            aShortArray9421 = null
            aShortArrayArray9424 = null
            aShortArray9422 = null
        }
    }
}
