package world.gregs.voidps.tools.icon

/* Class308 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class308(private var anInt3891: Int) {
    private val aClass348_Sub42_3887 = Class348_Sub42()
    private val aIterableHashTable_3888: IterableHashTable
    private var aQueue_3889: Queue? = Queue()
    private val anInt3890: Int

    fun method2302(l: Long, i: Byte): Class348_Sub42? {
        try {
            if (i > -25) aQueue_3889 = null
            anInt3885++
            val class348_sub42 = aIterableHashTable_3888.method3480(l, -6008) as Class348_Sub42?
            if (class348_sub42 != null) aQueue_3889!!.add(true, class348_sub42)
            return class348_sub42
        } catch (runtimeexception: RuntimeException) {
            throw Class348_Sub17.method2929(runtimeexception, "wu.D(" + l + ',' + i + ')')
        }
    }

    fun method2305(l: Long, class348_sub42: Class348_Sub42?, i: Int) {
        try {
            anInt3881++
            if ((anInt3891.inv()) == i) {
                var class348_sub42_0_ = aQueue_3889!!.method1008(20)
                class348_sub42_0_!!.unlink(113.toByte())
                class348_sub42_0_.unlink2(true)
                if (class348_sub42_0_ === aClass348_Sub42_3887) {
                    class348_sub42_0_ = aQueue_3889!!.method1008(20)
                    class348_sub42_0_!!.unlink(79.toByte())
                    class348_sub42_0_.unlink2(true)
                }
            } else anInt3891--
            aIterableHashTable_3888.put(37.toByte(), l, class348_sub42)
            aQueue_3889!!.add(true, class348_sub42!!)
        } catch (runtimeexception: RuntimeException) {
            throw Class348_Sub17.method2929(runtimeexception, ("wu.E(" + l + ',' + (if (class348_sub42 != null) "{...}" else "null") + ',' + i + ')'))
        }
    }

    init {
        anInt3890 = anInt3891
        var i_1_: Int
        i_1_ = 1
        while (i_1_ + i_1_ < anInt3891) {
            i_1_ += i_1_
        }
        aIterableHashTable_3888 = IterableHashTable(i_1_)
    }

    companion object {
        var anInt3881: Int = 0
        var anInt3885: Int = 0
    }
}
