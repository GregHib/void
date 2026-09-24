package world.gregs.voidps.tools.icon

/* Class37 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class37 {
    var anIntArray8684: IntArray? = null

    var aByteArrayArray8505: Array<ByteArray?> = arrayOfNulls<ByteArray>(50)

    var aByteArrayArray358: Array<ByteArray?> = arrayOfNulls<ByteArray>(1000)

    var anIntArray2552: IntArray? = null

    var aByteArrayArray1918: Array<ByteArray?> = arrayOfNulls<ByteArray>(250)

    var anInt493: Int = 0

    @Synchronized
    fun method359(i: Int, i_9_: Int): ByteArray? {
        anInt493++
        if (i == 100 && Class348_Sub40_Sub31.Companion.anInt9412 > 0) {
            val `is` = aByteArrayArray358[--Class348_Sub40_Sub31.Companion.anInt9412]
            aByteArrayArray358[Class348_Sub40_Sub31.Companion.anInt9412] = null
            return `is`
        }
        if (i == 5000 && Class348_Sub40_Sub21.anInt9280 > 0) {
            val `is` = (aByteArrayArray1918[--Class348_Sub40_Sub21.anInt9280])
            aByteArrayArray1918[Class348_Sub40_Sub21.anInt9280] = null
            return `is`
        }
        if (i_9_ != -1) method359(-88, -45)
        if (i == 30000 && Class348_Sub31.Companion.anInt6913 > 0) {
            val `is` = (aByteArrayArray8505[--Class348_Sub31.Companion.anInt6913])
            aByteArrayArray8505[Class348_Sub31.Companion.anInt6913] = null
            return `is`
        }
        if (Class348_Sub40_Sub6.Companion.aByteArrayArrayArray9134 != null) {
            var i_10_ = 0
            while (anIntArray8684!!.size > i_10_) {
                if ((i == anIntArray8684!![i_10_]) && anIntArray2552!![i_10_] > 0) {
                    val `is`: ByteArray? = (Class348_Sub40_Sub6.Companion.aByteArrayArrayArray9134!![i_10_]!![--anIntArray2552!![i_10_]]!!)
                    Class348_Sub40_Sub6.Companion.aByteArrayArrayArray9134!![i_10_]!![anIntArray2552!![i_10_]] = null
                    return `is`
                }
                i_10_++
            }
        }
        return ByteArray(i)
    }
}
