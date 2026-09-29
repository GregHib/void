package world.gregs.voidps.tools.render

/** Class308 **/
class LruHashTable(private var anInt3891: Int) {
    private val aSecondaryNode_3887 = SecondaryNode()
    private val aIterableHashTable_3888: IterableHashTable
    private var aQueue_3889: Queue? = Queue()

    fun method2302(l: Long): SecondaryNode? {
        val secondaryNode = aIterableHashTable_3888.method3480(l) as SecondaryNode?
        if (secondaryNode != null) aQueue_3889!!.add(secondaryNode)
        return secondaryNode
    }

    fun method2305(l: Long, secondaryNode: SecondaryNode?) {
        if (anInt3891 == 0) {
            var class348_sub42_0_ = aQueue_3889!!.method1008()
            class348_sub42_0_!!.unlink()
            class348_sub42_0_.unlink2()
            if (class348_sub42_0_ === aSecondaryNode_3887) {
                class348_sub42_0_ = aQueue_3889!!.method1008()
                class348_sub42_0_!!.unlink()
                class348_sub42_0_.unlink2()
            }
        } else anInt3891--
        aIterableHashTable_3888.put(l, secondaryNode)
        aQueue_3889!!.add(secondaryNode!!)
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
