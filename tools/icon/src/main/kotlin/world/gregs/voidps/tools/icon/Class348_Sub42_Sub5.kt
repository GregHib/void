package world.gregs.voidps.tools.icon

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index

/* Class348_Sub42_Sub5 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub42_Sub5(packet: Packet) : Class348_Sub42() {
    private val aClass348_Sub40Array9520: Array<Class348_Sub40?>
    private val aClass348_Sub40_9521: Class348_Sub40
    private val anIntArray9523: IntArray
    private val anIntArray9524: IntArray
    private val aClass348_Sub40_9527: Class348_Sub40
    private val aClass348_Sub40_9528: Class348_Sub40?
    fun method3183(var_textureSource: TextureSource?, i: Int, i_0_: Int, bool: Boolean, d: Double, cache: Cache?, i_1_: Byte): IntArray {
        try {
            anInt9522++
            Class286_Sub5.aTextureSource6247 = var_textureSource
            Class348.Companion.aCache_4286 = cache
            var i_2_ = 0
            while (aClass348_Sub40Array9520.size > i_2_) {
                aClass348_Sub40Array9520[i_2_]!!.method3045(i, i_0_, -256)
                i_2_++
            }
            Class348_Sub42_Sub13.method3232(d, (-122).toByte())
            Class79.method797(i_0_, i, 114.toByte())
            val `is` = IntArray(i * i_0_)
            var i_3_ = 0
            var i_4_ = 0
            while (i_0_ > i_4_) {
                val is_5_: IntArray
                val is_6_: IntArray
                val is_7_: IntArray
                if (aClass348_Sub40_9521.aBoolean7045) {
                    val is_8_ = aClass348_Sub40_9521.method3042(i_4_, 255)
                    is_7_ = is_8_!!
                    is_5_ = is_8_
                    is_6_ = is_8_
                } else {
                    val is_9_ = aClass348_Sub40_9521.method3047(i_4_, -1564599039)
                    is_5_ = is_9_!![2]
                    is_6_ = is_9_[1]
                    is_7_ = is_9_[0]
                }
                if (bool) i_3_ = i_4_
                val is_10_: IntArray
                if (aClass348_Sub40_9527.aBoolean7045) is_10_ = aClass348_Sub40_9527.method3042(i_4_, i_1_ + 244)!!
                else is_10_ = (aClass348_Sub40_9527.method3047(i_4_, -1564599039)!![0])
                for (i_11_ in i - 1 downTo 0) {
                    var i_12_ = is_7_[i_11_] shr 4
                    if (i_12_ > 255) i_12_ = 255
                    if (i_12_ < 0) i_12_ = 0
                    var i_13_ = is_6_[i_11_] shr 4
                    if (i_13_ > 255) i_13_ = 255
                    if (i_13_ < 0) i_13_ = 0
                    var i_14_ = is_5_[i_11_] shr 4
                    if (i_14_ > 255) i_14_ = 255
                    i_13_ = Class318_Sub1_Sub3_Sub3.anIntArray10266[i_13_]
                    if (i_14_ < 0) i_14_ = 0
                    i_12_ = Class318_Sub1_Sub3_Sub3.anIntArray10266[i_12_]
                    i_14_ = Class318_Sub1_Sub3_Sub3.anIntArray10266[i_14_]
                    var i_15_: Int
                    if (i_12_ == 0 && i_13_ == 0 && i_14_ == 0) i_15_ = 0
                    else {
                        i_15_ = is_10_[i_11_] shr 4
                        if (i_15_ > 255) i_15_ = 255
                        if (i_15_ < 0) i_15_ = 0
                    }
                    `is`[i_3_++] = (i_14_ + (i_15_ shl 24) + ((i_12_ shl 16) + (i_13_ shl 8)))
                    if (bool) i_3_ += -1 + i
                }
                i_4_++
            }
            var i_16_ = 0
            while ((i_16_ < aClass348_Sub40Array9520.size)) {
                aClass348_Sub40Array9520[i_16_]!!.method3046((-116).toByte())
                i_16_++
            }
            if (i_1_.toInt() != 11) anInt9532 = 97
            return `is`
        } catch (runtimeexception: RuntimeException) {
            throw Class348_Sub17.method2929(runtimeexception, ("lr.D(" + (if (var_textureSource != null) "{...}" else "null") + ',' + i + ',' + i_0_ + ',' + bool + ',' + d + ',' + (if (cache != null) "{...}" else "null") + ',' + i_1_ + ')'))
        }
    }

    fun method3184(var_textureSource: TextureSource?, cache: Cache?, i: Int): Boolean {
        try {
            anInt9529++
            if (Matrix_Sub2.anInt5713 < 0) {
                var i_17_ = 0
                while ((anIntArray9524.size > i_17_)) {
                    if (!cache!!.exists(Index.SPRITES, anIntArray9524[i_17_])) return false
                    i_17_++
                }
            } else {
                var i_18_ = 0
                while ((i_18_ < anIntArray9524.size)) {
                    if (!cache!!.exists(Index.SPRITES, Matrix_Sub2.anInt5713, anIntArray9524[i_18_])) return false
                    i_18_++
                }
            }
            var i_19_ = 0
            val i_20_ = -109 / ((10 - i) / 60)
            while ( /**/i_19_ < anIntArray9523.size) {
                if (!var_textureSource!!.method4(-7953, anIntArray9523[i_19_])) return false
                i_19_++
            }
            return true
        } catch (runtimeexception: RuntimeException) {
            throw Class348_Sub17.method2929(runtimeexception, ("lr.B(" + (if (var_textureSource != null) "{...}" else "null") + ',' + (if (cache != null) "{...}" else "null") + ',' + i + ')'))
        }
    }

    fun method3185(i: Int, var_textureSource: TextureSource?, i_21_: Int, bool: Boolean, d: Double, bool_22_: Boolean, cache: Cache?, i_23_: Int): IntArray {
        try {
            Class286_Sub5.aTextureSource6247 = var_textureSource
            Class348.Companion.aCache_4286 = cache
            anInt9526++
            var i_24_ = 0
            while (aClass348_Sub40Array9520.size > i_24_) {
                aClass348_Sub40Array9520[i_24_]!!.method3045(i, i_23_, i_21_ + -256)
                i_24_++
            }
            Class348_Sub42_Sub13.method3232(d, (-89).toByte())
            Class79.method797(i_23_, i, 122.toByte())
            val `is` = IntArray(i_23_ * i)
            val i_25_: Int
            val i_26_: Int
            val i_27_: Int
            if (bool_22_) {
                i_25_ = i - 1
                i_26_ = -1
                i_27_ = -1
            } else {
                i_25_ = 0
                i_26_ = 1
                i_27_ = i
            }
            var i_28_ = 0
            var i_29_ = 0
            while (i_23_ > i_29_) {
                val is_30_: IntArray
                val is_31_: IntArray
                val is_32_: IntArray
                if (aClass348_Sub40_9521.aBoolean7045) {
                    val is_33_ = aClass348_Sub40_9521.method3042(i_29_, 255)
                    is_30_ = is_33_!!
                    is_31_ = is_33_
                    is_32_ = is_33_
                } else {
                    val is_34_ = aClass348_Sub40_9521.method3047(i_29_, -1564599039)
                    is_30_ = is_34_!![1]
                    is_31_ = is_34_[0]
                    is_32_ = is_34_[2]
                }
                if (bool) i_28_ = i_29_
                var i_35_ = i_25_
                while (i_35_ != i_27_) {
                    var i_36_ = is_31_[i_35_] shr 4
                    if (i_36_ > 255) i_36_ = 255
                    if (i_36_ < 0) i_36_ = 0
                    var i_37_ = is_30_[i_35_] shr 4
                    if (i_37_ > 255) i_37_ = 255
                    if (i_37_ < 0) i_37_ = 0
                    var i_38_ = is_32_[i_35_] shr 4
                    if (i_38_ > 255) i_38_ = 255
                    i_37_ = Class318_Sub1_Sub3_Sub3.anIntArray10266[i_37_]
                    i_36_ = Class318_Sub1_Sub3_Sub3.anIntArray10266[i_36_]
                    if (i_38_ < 0) i_38_ = 0
                    i_38_ = Class318_Sub1_Sub3_Sub3.anIntArray10266[i_38_]
                    var i_39_ = (i_37_ shl 8) + (i_36_ shl 16) + i_38_
                    if (i_39_ != 0) i_39_ = i_39_ or 0xffffff.inv()
                    `is`[i_28_++] = i_39_
                    if (bool) i_28_ += -1 + i
                    i_35_ += i_26_
                }
                i_29_++
            }
            var i_40_ = i_21_
            while ((aClass348_Sub40Array9520.size > i_40_)) {
                aClass348_Sub40Array9520[i_40_]!!.method3046((-106).toByte())
                i_40_++
            }
            return `is`
        } catch (runtimeexception: RuntimeException) {
            throw Class348_Sub17.method2929(runtimeexception, ("lr.C(" + i + ',' + (if (var_textureSource != null) "{...}" else "null") + ',' + i_21_ + ',' + bool + ',' + d + ',' + bool_22_ + ',' + (if (cache != null) "{...}" else "null") + ',' + i_23_ + ')'))
        }
    }

    init {
        val i = packet.readUnsignedByte(255)
        var i_56_ = 0
        var i_57_ = 0
        val `is` = arrayOfNulls<IntArray>(i)
        aClass348_Sub40Array9520 = arrayOfNulls<Class348_Sub40>(i)
        for (i_58_ in 0..<i) {
            val class348_sub40 = Class348_Sub37.method3031(125, packet)
            if (class348_sub40!!.method3037(-121) >= 0) i_56_++
            if (class348_sub40.method3043(-1) >= 0) i_57_++
            val i_59_ = (class348_sub40.aClass348_Sub40Array7031).size
            `is`[i_58_] = IntArray(i_59_)
            for (i_60_ in 0..<i_59_) `is`[i_58_]!![i_60_] = packet.readUnsignedByte(255)
            aClass348_Sub40Array9520[i_58_] = class348_sub40
        }
        anIntArray9524 = IntArray(i_56_)
        anIntArray9523 = IntArray(i_57_)
        i_56_ = 0
        i_57_ = 0
        var i_61_ = 0
        while (i > i_61_) {
            val class348_sub40 = aClass348_Sub40Array9520[i_61_]
            val i_62_ = (class348_sub40!!.aClass348_Sub40Array7031).size
            var i_63_ = 0
            while (i_62_ > i_63_) {
                class348_sub40.aClass348_Sub40Array7031[i_63_] = aClass348_Sub40Array9520[`is`[i_61_]!![i_63_]]
                i_63_++
            }
            val i_64_ = class348_sub40.method3037(-119)
            val i_65_ = class348_sub40.method3043(-1)
            if (i_64_ > 0) anIntArray9524[i_56_++] = i_64_
            if (i_65_ > 0) anIntArray9523[i_57_++] = i_65_
            `is`[i_61_] = null
            i_61_++
        }
        aClass348_Sub40_9521 = aClass348_Sub40Array9520[packet.readUnsignedByte(255)]!!
        aClass348_Sub40_9527 = aClass348_Sub40Array9520[packet.readUnsignedByte(255)]!!
        aClass348_Sub40_9528 = aClass348_Sub40Array9520[packet.readUnsignedByte(255)]
        val `object`: Any? = null
    }

    companion object {
        var anInt9522: Int = 0
        var anInt9526: Int = 0
        var anInt9529: Int = 0
        var anInt9532: Int = 0
    }
}
