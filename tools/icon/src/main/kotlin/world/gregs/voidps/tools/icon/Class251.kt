package world.gregs.voidps.tools.icon

/* Class251 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class251 {
    var anInt3231: Int = 0
    var anInt3235: Int = 0

    fun method1913(bool: Boolean, i: Int, class46: Class46?) {
        anInt3235++
        val i_0_ = -40 % ((-35 - i) / 51)
        val i_1_ = (if (class46!!.anInt698 == 0) class46.anInt709 else class46.anInt698)
        val i_2_ = (if (class46.anInt791 != 0) class46.anInt791 else class46.anInt789)
        Class367_Sub1.method3534(false, class46.anInt830, i_1_, bool, i_2_, (Class348_Sub40_Sub33.aClass46ArrayArray9427!![(class46.anInt830 shr 16)]))
        if (class46.aClass46Array798 != null) Class367_Sub1.method3534(false, class46.anInt830, i_1_, bool, i_2_, class46.aClass46Array798)
        val class348_sub41 = (Class125.aIterableHashTable_4915.method3480(class46.anInt830.toLong(), -6008) as Class348_Sub41?)
        if (class348_sub41 != null) Class239_Sub3.method1728(i_2_, -1, (class348_sub41.anInt7050), bool, i_1_)
    }

    fun method1914(i: Int, i_3_: Int): Int {
        anInt3231++
        if (i != -23590) Class251.method1913(false, -115, null)
        return i_3_ and 0xff
    }
}
