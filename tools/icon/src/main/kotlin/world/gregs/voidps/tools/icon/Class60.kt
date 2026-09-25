package world.gregs.voidps.tools.icon

/* Class60 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class60 @JvmOverloads constructor(private var anInt1086: Int, i_10_: Int = anInt1086) {
    private val anInt1084: Int
    private var aQueue_1089: Queue? = Queue()
    private val aIterableHashTable_1100: IterableHashTable

    fun method580(`object`: Any?, l: Long, i_5_: Int) {
        check(i_5_ <= anInt1084) { "s>cs" }
        method586(l)
        anInt1086 -= i_5_
        while (anInt1086 < 0) {
            val class348_sub42_sub8 = (aQueue_1089!!.method1008() as Class348_Sub42_Sub8?)
            method585(class348_sub42_sub8)
        }
        val class348_sub42_sub8_sub2 = Class348_Sub42_Sub8_Sub2(`object`, i_5_)
        aIterableHashTable_1100.put(l, class348_sub42_sub8_sub2)
        aQueue_1089!!.add(class348_sub42_sub8_sub2)
    }

    fun method582(`object`: Any?, l: Long) {
        method580(`object`, l, 1)
    }

    fun method583(l: Long): Any? {
        val class348_sub42_sub8 = aIterableHashTable_1100.method3480(l) as Class348_Sub42_Sub8?
        if (class348_sub42_sub8 == null) return null
        val `object` = class348_sub42_sub8.method3193()
        if (`object` == null) {
            class348_sub42_sub8.unlink()
            class348_sub42_sub8.unlink2()
            anInt1086 += class348_sub42_sub8.anInt9545
            return null
        }
        if (class348_sub42_sub8.method3195()) {
            val class348_sub42_sub8_sub2 = Class348_Sub42_Sub8_Sub2(`object`, (class348_sub42_sub8.anInt9545))
            aIterableHashTable_1100.put((class348_sub42_sub8.aLong4291), class348_sub42_sub8_sub2)
            aQueue_1089!!.add(class348_sub42_sub8_sub2)
            class348_sub42_sub8.unlink()
            class348_sub42_sub8.unlink2()
        } else {
            aQueue_1089!!.add(class348_sub42_sub8)
        }
        return `object`
    }

    private fun method585(class348_sub42_sub8: Class348_Sub42_Sub8?) {
        if (class348_sub42_sub8 != null) {
            class348_sub42_sub8.unlink()
            class348_sub42_sub8.unlink2()
            anInt1086 += class348_sub42_sub8.anInt9545
        }
    }

    private fun method586(l: Long) {
        val class348_sub42_sub8 = aIterableHashTable_1100.method3480(l) as Class348_Sub42_Sub8?
        method585(class348_sub42_sub8)
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
