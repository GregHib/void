package world.gregs.voidps.tools.icon

/* Class60 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class60 @JvmOverloads constructor(private var anInt1086: Int, i_10_: Int = anInt1086) {
    private val anInt1084: Int
    private var aQueue_1089: Queue? = Queue()
    private val aIterableHashTable_1100: IterableHashTable

    fun method580(i: Int, `object`: Any?, l: Long, i_5_: Int) {
        try {
            anInt1092++
            check(i_5_ <= anInt1084) { "s>cs" }
            method586(l, 0)
            anInt1086 -= i_5_
            while (anInt1086 < 0) {
                val class348_sub42_sub8 = (aQueue_1089!!.method1008(i xor 0x7c8a) as Class348_Sub42_Sub8?)
                method585(class348_sub42_sub8, i xor 0x7cfa.inv())
            }
            val class348_sub42_sub8_sub2 = Class348_Sub42_Sub8_Sub2(`object`, i_5_)
            aIterableHashTable_1100.put(54.toByte(), l, class348_sub42_sub8_sub2)
            if (i != 31902) anInt1086 = -106
            aQueue_1089!!.add(true, class348_sub42_sub8_sub2)
            class348_sub42_sub8_sub2.key2 = 0L
        } catch (runtimeexception: RuntimeException) {
            throw Class348_Sub17.method2929(runtimeexception, ("jr.E(" + i + ',' + (if (`object` != null) "{...}" else "null") + ',' + l + ',' + i_5_ + ')'))
        }
    }

    fun method582(`object`: Any?, l: Long, i: Byte) {
        try {
            if (i >= -92) method589(null, -7)
            anInt1095++
            method580(31902, `object`, l, 1)
        } catch (runtimeexception: RuntimeException) {
            throw Class348_Sub17.method2929(runtimeexception, ("jr.B(" + (if (`object` != null) "{...}" else "null") + ',' + l + ',' + i + ')'))
        }
    }

    fun method583(l: Long, i: Int): Any? {
        try {
            val i_6_ = -59 % ((i - 2) / 47)
            anInt1085++
            val class348_sub42_sub8 = aIterableHashTable_1100.method3480(l, -6008) as Class348_Sub42_Sub8?
            if (class348_sub42_sub8 == null) return null
            val `object` = class348_sub42_sub8.method3193(86)
            if (`object` == null) {
                class348_sub42_sub8.unlink(102.toByte())
                class348_sub42_sub8.unlink2(true)
                anInt1086 += class348_sub42_sub8.anInt9545
                return null
            }
            if (class348_sub42_sub8.method3195(-4)) {
                val class348_sub42_sub8_sub2 = Class348_Sub42_Sub8_Sub2(`object`, (class348_sub42_sub8.anInt9545))
                aIterableHashTable_1100.put(90.toByte(), (class348_sub42_sub8.aLong4291), class348_sub42_sub8_sub2)
                aQueue_1089!!.add(true, class348_sub42_sub8_sub2)
                class348_sub42_sub8_sub2.key2 = 0L
                class348_sub42_sub8.unlink(112.toByte())
                class348_sub42_sub8.unlink2(true)
            } else {
                aQueue_1089!!.add(true, class348_sub42_sub8)
                class348_sub42_sub8.key2 = 0L
            }
            return `object`
        } catch (runtimeexception: RuntimeException) {
            throw Class348_Sub17.method2929(runtimeexception, "jr.K(" + l + ',' + i + ')')
        }
    }

    private fun method585(class348_sub42_sub8: Class348_Sub42_Sub8?, i: Int) {
        val i_8_ = 80 / ((i - 6) / 36)
        anInt1102++
        if (class348_sub42_sub8 != null) {
            class348_sub42_sub8.unlink(117.toByte())
            class348_sub42_sub8.unlink2(true)
            anInt1086 += class348_sub42_sub8.anInt9545
        }
    }

    private fun method586(l: Long, i: Int) {
        try {
            if (i != 0) aQueue_1089 = null
            anInt1090++
            val class348_sub42_sub8 = aIterableHashTable_1100.method3480(l, -6008) as Class348_Sub42_Sub8?
            method585(class348_sub42_sub8, -57)
        } catch (runtimeexception: RuntimeException) {
            throw Class348_Sub17.method2929(runtimeexception, "jr.J(" + l + ',' + i + ')')
        }
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

    companion object {
        var anInt1085: Int = 0
        var anInt1090: Int = 0
        var anInt1092: Int = 0
        var anInt1095: Int = 0
        var anInt1102: Int = 0
        var anInt1103: Int = 0
        fun method589(class42: Class42?, i: Int): Boolean {
            anInt1103++
            if (class42 == null) return false
            if (i != -4) return false
            if (!class42.aBoolean574) return false
            if (!class42.method373(Class75.anInterface17_1244!!, i xor 0x2d.inv())) return false
            if (Class158.aIterableHashTable_4934.method3480(class42.anInt581.toLong(), i xor 0x1774) != null) return false
            return KeyedHardReferenceNode.Companion.aIterableHashTable_10442.method3480(class42.anInt596.toLong(), i + -6004) == null
        }
    }
}
