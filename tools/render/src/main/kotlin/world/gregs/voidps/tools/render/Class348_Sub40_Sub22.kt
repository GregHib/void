package world.gregs.voidps.tools.render

import java.util.Random

/* Class348_Sub40_Sub22 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class348_Sub40_Sub22 : Class348_Sub40(0, true) {
    private var anInt9284 = 1024
    private var anIntArrayArray9286: Array<IntArray>? = null
    private var anIntArrayArray9287: Array<IntArray>? = null
    private var anInt9288 = 1024
    private var anInt9291 = 0
    private var anInt9293 = 0
    private var anInt9294 = 81
    private var anIntArray9297: IntArray? = null
    private var anInt9298 = 0
    private var anInt9299 = 4
    private var anInt9300 = 0
    private var anInt9301 = 8
    private var anInt9302 = 409
    private var anInt9305 = 204

    private fun method3109() {
        val random = Random(anInt9301.toLong())
        anInt9298 = anInt9294 / 2
        anInt9291 = 4096 / anInt9299
        anInt9300 = 4096 / anInt9301
        val i_0_ = anInt9291 / 2
        val anIntArray9297 = IntArray(anInt9301 + 1)
        this.anIntArray9297 = anIntArray9297
        val i_1_ = anInt9300 / 2
        val anIntArrayArray9287 = Array(anInt9301) { IntArray(1 + anInt9299) }
        this.anIntArrayArray9287 = anIntArrayArray9287
        val anIntArrayArray9286 = Array(anInt9301) { IntArray(anInt9299) }
        this.anIntArrayArray9286 = anIntArrayArray9286
        anIntArray9297[0] = 0
        for (i_2_ in 0..<anInt9301) {
            if (i_2_ > 0) {
                var i_3_ = anInt9300
                val i_4_ = ((Mesh.method1097(4096, random) - 2048) * anInt9305 shr 12)
                i_3_ += i_4_ * i_1_ shr 12
                anIntArray9297[i_2_] = i_3_ + anIntArray9297[i_2_ - 1]
            }
            anIntArrayArray9287[i_2_][0] = 0
            for (i_5_ in 0..<anInt9299) {
                if (i_5_ > 0) {
                    var i_6_ = anInt9291
                    val i_7_ = ((Mesh.method1097(4096, random) - 2048) * anInt9302 shr 12)
                    i_6_ += i_0_ * i_7_ shr 12
                    anIntArrayArray9287[i_2_][i_5_] = anIntArrayArray9287[i_2_][i_5_ - 1] + i_6_
                }
                anIntArrayArray9286[i_2_][i_5_] = if (anInt9284 <= 0) 4096 else 4096 - Mesh.method1097(anInt9284, random)
            }
            anIntArrayArray9287[i_2_][anInt9299] = 4096
        }
        anIntArray9297[anInt9301] = 4096
    }

    override fun method3049(packet: Packet?, i: Int) {
        when (i) {
            0 -> anInt9299 = packet!!.readUnsignedByte()
            1 -> anInt9301 = packet!!.readUnsignedByte()
            2 -> anInt9302 = packet!!.readUnsignedShort()
            3 -> anInt9305 = packet!!.readUnsignedShort()
            4 -> anInt9288 = packet!!.readUnsignedShort()
            5 -> anInt9293 = packet!!.readUnsignedShort()
            6 -> anInt9294 = packet!!.readUnsignedShort()
            7 -> anInt9284 = packet!!.readUnsignedShort()
        }
    }

    override fun method3042(i: Int): IntArray? {
        val `is` = this.aClass191_7032!!.method1433(i)!!
        if (this.aClass191_7032!!.aBoolean2570) {
            val anIntArray9297 = anIntArray9297!!
            val anIntArrayArray9287 = anIntArrayArray9287!!
            var i_11_ = 0
            var i_12_ = anInt9293 + Class79.anIntArray6035!![i]
            while (i_12_ < 0) i_12_ += 4096
            while (i_12_ > 4096) i_12_ -= 4096
            while (i_11_ < anInt9301) {
                if (i_12_ < anIntArray9297[i_11_]) break
                i_11_++
            }
            val i_13_ = i_11_ - 1
            val bool = (0x1 and i_11_) == 0
            val i_14_ = anIntArray9297[i_11_]
            val i_15_ = anIntArray9297[i_11_ - 1]
            if (anInt9298 + i_15_ < i_12_ && i_12_ < i_14_ - anInt9298) {
                for (i_16_ in 0..<Class348_Sub40_Sub6.anInt9139) {
                    var i_17_ = 0
                    val i_18_ = if (!bool) -anInt9288 else anInt9288
                    var i_19_ = Class348_Sub40_Sub8.anIntArray6432!![i_16_] + (i_18_ * anInt9291 shr 12)
                    while (i_19_ < 0) i_19_ += 4096
                    while (i_19_ > 4096) i_19_ -= 4096
                    while (i_17_ < anInt9299) {
                        if (anIntArrayArray9287[i_13_][i_17_] > i_19_) break
                        i_17_++
                    }
                    val i_20_ = i_17_ - 1
                    val i_21_ = anIntArrayArray9287[i_13_][i_20_]
                    val i_22_ = anIntArrayArray9287[i_13_][i_17_]
                    if (anInt9298 + i_21_ >= i_19_ || i_19_ >= -anInt9298 + i_22_) {
                        `is`[i_16_] = 0
                    } else {
                        `is`[i_16_] = anIntArrayArray9286!![i_13_][i_20_]
                    }
                }
            } else {
                Class214.method1579(`is`, 0, Class348_Sub40_Sub6.anInt9139, 0)
            }
        }
        return `is`
    }

    override fun method3044() {
        method3109()
    }
}
