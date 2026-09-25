package world.gregs.voidps.tools.render

class Class308(private var anInt3891: Int) {
    private val aClass348_Sub42_3887 = Class348_Sub42()
    private val aIterableHashTable_3888: IterableHashTable
    private var aQueue_3889: Queue? = Queue()

    fun method2302(l: Long): Class348_Sub42? {
        val class348_sub42 = aIterableHashTable_3888.method3480(l) as Class348_Sub42?
        if (class348_sub42 != null) aQueue_3889!!.add(class348_sub42)
        return class348_sub42
    }

    fun method2305(l: Long, class348_sub42: Class348_Sub42?) {
        if (anInt3891 == 0) {
            var class348_sub42_0_ = aQueue_3889!!.method1008()
            class348_sub42_0_!!.unlink()
            class348_sub42_0_.unlink2()
            if (class348_sub42_0_ === aClass348_Sub42_3887) {
                class348_sub42_0_ = aQueue_3889!!.method1008()
                class348_sub42_0_!!.unlink()
                class348_sub42_0_.unlink2()
            }
        } else anInt3891--
        aIterableHashTable_3888.put(l, class348_sub42)
        aQueue_3889!!.add(class348_sub42!!)
    }

    init {
        var i_1_: Int
        i_1_ = 1
        while (i_1_ + i_1_ < anInt3891) {
            i_1_ += i_1_
        }
        aIterableHashTable_3888 = IterableHashTable(i_1_)
    }
}
