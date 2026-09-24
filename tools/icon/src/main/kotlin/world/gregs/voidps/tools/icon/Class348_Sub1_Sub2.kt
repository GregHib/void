package world.gregs.voidps.tools.icon

/* Class348_Sub1_Sub2 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*
* Trimmed for item_renderer_standalone: only the static Class308 cache field
* (aClass308_8815) used by ha.method3664/method3692's byte-shuffle cache is kept.
*/

internal object Class348_Sub1_Sub2 : Class348_Sub1() {
    var aClass308_8815: Class308 = Class308(16)
    var anInt8811: Int = 0

    /* Needed by Class291's constructor (genuine: Class291(byte[], int, byte[]))
     * to verify a checksum against reference-table data. */
    fun method2730(i: Int, i_4_: Int, `is`: ByteArray, i_5_: Int): ByteArray {
        anInt8811++
        val is_6_: ByteArray?
        if (i_4_ > 0) {
            is_6_ = ByteArray(i_5_)
            var i_7_ = 0
            while (i_5_ > i_7_) {
                is_6_[i_7_] = `is`[i_4_ + i_7_]
                i_7_++
            }
        } else is_6_ = `is`
        val class85 = Class85()
        class85.method829(i + -4682)
        class85.method832((i_5_ * 8).toLong(), is_6_, -69)
        val is_8_ = ByteArray(64)
        class85.method833(true, 0, is_8_)
        return is_8_
    }
}
