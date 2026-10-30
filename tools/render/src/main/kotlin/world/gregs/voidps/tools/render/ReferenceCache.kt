package world.gregs.voidps.tools.render

/** Class60 **/
class ReferenceCache @JvmOverloads constructor(private var anInt1086: Int, i_10_: Int = anInt1086) {
    private val anInt1084: Int
    private var aQueue_1089: Queue? = Queue()
    private val aIterableHashTable_1100: IterableHashTable

    fun method580(`object`: Any?, l: Long, i_5_: Int) {
        check(i_5_ <= anInt1084) { "s>cs" }
        method586(l)
        anInt1086 -= i_5_
        while (anInt1086 < 0) {
            val referenceNode = (aQueue_1089!!.method1008() as ReferenceNode?)
            method585(referenceNode)
        }
        val hardReferenceNode = HardReferenceNode(`object`, i_5_)
        aIterableHashTable_1100.put(l, hardReferenceNode)
        aQueue_1089!!.add(hardReferenceNode)
    }

    fun method582(`object`: Any?, l: Long) {
        method580(`object`, l, 1)
    }

    fun method583(l: Long): Any? {
        val referenceNode = aIterableHashTable_1100.method3480(l) as ReferenceNode?
        if (referenceNode == null) return null
        val `object` = referenceNode.method3193()
        if (`object` == null) {
            referenceNode.unlink()
            referenceNode.unlink2()
            anInt1086 += referenceNode.anInt9545
            return null
        }
        if (referenceNode.method3195()) {
            val hardReferenceNode = HardReferenceNode(`object`, (referenceNode.anInt9545))
            aIterableHashTable_1100.put((referenceNode.aLong4291), hardReferenceNode)
            aQueue_1089!!.add(hardReferenceNode)
            referenceNode.unlink()
            referenceNode.unlink2()
        } else {
            aQueue_1089!!.add(referenceNode)
        }
        return `object`
    }

    private fun method585(referenceNode: ReferenceNode?) {
        if (referenceNode != null) {
            referenceNode.unlink()
            referenceNode.unlink2()
            anInt1086 += referenceNode.anInt9545
        }
    }

    private fun method586(l: Long) {
        val referenceNode = aIterableHashTable_1100.method3480(l) as ReferenceNode?
        method585(referenceNode)
    }

    init {
        anInt1084 = anInt1086
        var i_11_: Int
        i_11_ = 1
        while (anInt1086 > i_11_ + i_11_ && i_10_ > i_11_) {
            i_11_ += i_11_
        }
        aIterableHashTable_1100 = IterableHashTable(i_11_)
    }
}
