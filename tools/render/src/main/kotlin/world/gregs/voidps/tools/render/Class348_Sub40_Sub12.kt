package world.gregs.voidps.tools.render

/* Class348_Sub40_Sub12 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

class Class348_Sub40_Sub12 : Class348_Sub40(1, false) {
    private var anIntArrayArray9194: Array<IntArray>? = null
    private val anIntArray9199 = IntArray(257)
    override fun method3049(packet: Packet?, i: Int) {
        if (i == 0) {
            val i_1_ = packet!!.readUnsignedByte()
            if (i_1_ == 0) {
                anIntArrayArray9194 = Array(packet.readUnsignedByte()) { IntArray(4) }
                var i_2_ = 0
                while (anIntArrayArray9194!!.size > i_2_) {
                    anIntArrayArray9194!![i_2_][0] = packet.readUnsignedShort()
                    anIntArrayArray9194!![i_2_][1] = packet.readUnsignedByte() shl 4
                    anIntArrayArray9194!![i_2_][2] = packet.readUnsignedByte() shl 4
                    anIntArrayArray9194!![i_2_][3] = ((packet.readUnsignedByte()) shl 4)
                    i_2_++
                }
            } else method3078(i_1_)
        }
    }

    override fun method3047(i: Int): Array<IntArray>? {
        val `is` = this.aClass322_7033!!.method2557(i)
        if (this.aClass322_7033!!.aBoolean4035) {
            val is_4_ = this.method3048(i, 0)
            val is_5_ = `is`!![0]
            val is_6_ = `is`[1]
            val is_7_ = `is`[2]
            var i_8_ = 0
            while ((Class348_Sub40_Sub6.anInt9139 > i_8_)) {
                var i_9_ = is_4_!![i_8_] shr 4
                if (i_9_ < 0) i_9_ = 0
                if (i_9_ > 256) i_9_ = 256
                i_9_ = anIntArray9199[i_9_]
                is_5_[i_8_] = Class348_Sub40_Sub17.method1166(i_9_, 16711680) shr 12
                is_6_[i_8_] = Class348_Sub40_Sub17.method1166(65280, i_9_) shr 4
                is_7_[i_8_] = Class348_Sub40_Sub17.method1166(4080, i_9_ shl 4)
                i_8_++
            }
        }
        return `is`
    }

    override fun method3044() {
        if (anIntArrayArray9194 == null) method3078(1)
        method3079()
    }

    private fun method3078(i: Int) {
        while_157_@ do {
            if (i != 0) {
                val i_11_ = i
                while_156_@ do {
                    while_155_@ do {
                        while_154_@ do {
                            while_153_@ do {
                                do {
                                    if (i_11_ == 1) {
                                        anIntArrayArray9194 = Array(2) { IntArray(4) }
                                        anIntArrayArray9194!![0][1] = 0
                                        anIntArrayArray9194!![0][2] = 0
                                        anIntArrayArray9194!![0][0] = 0
                                        anIntArrayArray9194!![0][3] = 0
                                        anIntArrayArray9194!![1][3] = 4096
                                        anIntArrayArray9194!![1][2] = 4096
                                        anIntArrayArray9194!![1][1] = 4096
                                        anIntArrayArray9194!![1][0] = 4096
                                        break@while_157_
                                    } else if (i_11_ != 2) {
                                        if (i_11_ != 3) {
                                            if (i_11_ != 4) {
                                                if (i_11_ != 5) {
                                                    if (i_11_ != 6) break@while_156_
                                                } else break@while_154_
                                                break@while_155_
                                            }
                                        } else break
                                        break@while_153_
                                    }
                                    anIntArrayArray9194 = Array(8) { IntArray(4) }
                                    anIntArrayArray9194!![0][3] = 2361
                                    anIntArrayArray9194!![0][1] = 2650
                                    anIntArrayArray9194!![0][0] = 0
                                    anIntArrayArray9194!![0][2] = 2602
                                    anIntArrayArray9194!![1][2] = 1799
                                    anIntArrayArray9194!![1][1] = 2313
                                    anIntArrayArray9194!![1][3] = 1558
                                    anIntArrayArray9194!![1][0] = 2867
                                    anIntArrayArray9194!![2][2] = 1734
                                    anIntArrayArray9194!![2][1] = 2618
                                    anIntArrayArray9194!![2][3] = 1413
                                    anIntArrayArray9194!![2][0] = 3072
                                    anIntArrayArray9194!![3][2] = 1220
                                    anIntArrayArray9194!![3][0] = 3276
                                    anIntArrayArray9194!![3][3] = 947
                                    anIntArrayArray9194!![3][1] = 2296
                                    anIntArrayArray9194!![4][2] = 963
                                    anIntArrayArray9194!![4][3] = 722
                                    anIntArrayArray9194!![4][0] = 3481
                                    anIntArrayArray9194!![4][1] = 2072
                                    anIntArrayArray9194!![5][3] = 1766
                                    anIntArrayArray9194!![5][2] = 2152
                                    anIntArrayArray9194!![5][0] = 3686
                                    anIntArrayArray9194!![5][1] = 2730
                                    anIntArrayArray9194!![6][2] = 1060
                                    anIntArrayArray9194!![6][0] = 3891
                                    anIntArrayArray9194!![6][1] = 2232
                                    anIntArrayArray9194!![6][3] = 915
                                    anIntArrayArray9194!![7][2] = 1413
                                    anIntArrayArray9194!![7][3] = 1140
                                    anIntArrayArray9194!![7][1] = 1686
                                    anIntArrayArray9194!![7][0] = 4096
                                    break@while_157_
                                } while (false)
                                anIntArrayArray9194 = Array(7) { IntArray(4) }
                                anIntArrayArray9194!![0][0] = 0
                                anIntArrayArray9194!![0][1] = 0
                                anIntArrayArray9194!![0][2] = 0
                                anIntArrayArray9194!![0][3] = 4096
                                anIntArrayArray9194!![1][3] = 4096
                                anIntArrayArray9194!![1][1] = 0
                                anIntArrayArray9194!![1][0] = 663
                                anIntArrayArray9194!![1][2] = 4096
                                anIntArrayArray9194!![2][0] = 1363
                                anIntArrayArray9194!![2][2] = 4096
                                anIntArrayArray9194!![2][1] = 0
                                anIntArrayArray9194!![2][3] = 0
                                anIntArrayArray9194!![3][0] = 2048
                                anIntArrayArray9194!![3][2] = 4096
                                anIntArrayArray9194!![3][3] = 0
                                anIntArrayArray9194!![3][1] = 4096
                                anIntArrayArray9194!![4][0] = 2727
                                anIntArrayArray9194!![4][1] = 4096
                                anIntArrayArray9194!![4][2] = 0
                                anIntArrayArray9194!![4][3] = 0
                                anIntArrayArray9194!![5][0] = 3411
                                anIntArrayArray9194!![5][3] = 4096
                                anIntArrayArray9194!![5][2] = 0
                                anIntArrayArray9194!![5][1] = 4096
                                anIntArrayArray9194!![6][2] = 0
                                anIntArrayArray9194!![6][3] = 4096
                                anIntArrayArray9194!![6][0] = 4096
                                anIntArrayArray9194!![6][1] = 0
                                break@while_157_
                            } while (false)
                            anIntArrayArray9194 = Array(6) { IntArray(4) }
                            anIntArrayArray9194!![0][2] = 0
                            anIntArrayArray9194!![0][1] = 0
                            anIntArrayArray9194!![0][3] = 0
                            anIntArrayArray9194!![0][0] = 0
                            anIntArrayArray9194!![1][3] = 1493
                            anIntArrayArray9194!![1][1] = 0
                            anIntArrayArray9194!![1][2] = 0
                            anIntArrayArray9194!![1][0] = 1843
                            anIntArrayArray9194!![2][3] = 2939
                            anIntArrayArray9194!![2][2] = 0
                            anIntArrayArray9194!![2][1] = 0
                            anIntArrayArray9194!![2][0] = 2457
                            anIntArrayArray9194!![3][3] = 3565
                            anIntArrayArray9194!![3][1] = 0
                            anIntArrayArray9194!![3][2] = 1124
                            anIntArrayArray9194!![3][0] = 2781
                            anIntArrayArray9194!![4][2] = 3084
                            anIntArrayArray9194!![4][0] = 3481
                            anIntArrayArray9194!![4][1] = 546
                            anIntArrayArray9194!![4][3] = 4031
                            anIntArrayArray9194!![5][0] = 4096
                            anIntArrayArray9194!![5][3] = 4096
                            anIntArrayArray9194!![5][2] = 4096
                            anIntArrayArray9194!![5][1] = 4096
                            break@while_157_
                        } while (false)
                        anIntArrayArray9194 = Array(16) { IntArray(4) }
                        anIntArrayArray9194!![0][0] = 0
                        anIntArrayArray9194!![0][3] = 321
                        anIntArrayArray9194!![0][2] = 192
                        anIntArrayArray9194!![0][1] = 80
                        anIntArrayArray9194!![1][0] = 155
                        anIntArrayArray9194!![1][2] = 449
                        anIntArrayArray9194!![1][3] = 562
                        anIntArrayArray9194!![1][1] = 321
                        anIntArrayArray9194!![2][0] = 389
                        anIntArrayArray9194!![2][3] = 803
                        anIntArrayArray9194!![2][1] = 578
                        anIntArrayArray9194!![2][2] = 690
                        anIntArrayArray9194!![3][1] = 947
                        anIntArrayArray9194!![3][3] = 1140
                        anIntArrayArray9194!![3][0] = 671
                        anIntArrayArray9194!![3][2] = 995
                        anIntArrayArray9194!![4][1] = 1285
                        anIntArrayArray9194!![4][3] = 1509
                        anIntArrayArray9194!![4][2] = 1397
                        anIntArrayArray9194!![4][0] = 897
                        anIntArrayArray9194!![5][2] = 1429
                        anIntArrayArray9194!![5][3] = 1413
                        anIntArrayArray9194!![5][0] = 1175
                        anIntArrayArray9194!![5][1] = 1525
                        anIntArrayArray9194!![6][0] = 1368
                        anIntArrayArray9194!![6][2] = 1461
                        anIntArrayArray9194!![6][1] = 1734
                        anIntArrayArray9194!![6][3] = 1333
                        anIntArrayArray9194!![7][2] = 1525
                        anIntArrayArray9194!![7][1] = 1413
                        anIntArrayArray9194!![7][0] = 1507
                        anIntArrayArray9194!![7][3] = 1702
                        anIntArrayArray9194!![8][3] = 2056
                        anIntArrayArray9194!![8][1] = 1108
                        anIntArrayArray9194!![8][2] = 1590
                        anIntArrayArray9194!![8][0] = 1736
                        anIntArrayArray9194!![9][2] = 2056
                        anIntArrayArray9194!![9][3] = 2666
                        anIntArrayArray9194!![9][0] = 2088
                        anIntArrayArray9194!![9][1] = 1766
                        anIntArrayArray9194!![10][0] = 2355
                        anIntArrayArray9194!![10][2] = 2586
                        anIntArrayArray9194!![10][1] = 2409
                        anIntArrayArray9194!![10][3] = 3276
                        anIntArrayArray9194!![11][2] = 3148
                        anIntArrayArray9194!![11][3] = 3228
                        anIntArrayArray9194!![11][1] = 3116
                        anIntArrayArray9194!![11][0] = 2691
                        anIntArrayArray9194!![12][3] = 3196
                        anIntArrayArray9194!![12][0] = 3031
                        anIntArrayArray9194!![12][1] = 3806
                        anIntArrayArray9194!![12][2] = 3710
                        anIntArrayArray9194!![13][3] = 3019
                        anIntArrayArray9194!![13][1] = 3437
                        anIntArrayArray9194!![13][2] = 3421
                        anIntArrayArray9194!![13][0] = 3522
                        anIntArrayArray9194!![14][1] = 3116
                        anIntArrayArray9194!![14][2] = 3148
                        anIntArrayArray9194!![14][3] = 3228
                        anIntArrayArray9194!![14][0] = 3727
                        anIntArrayArray9194!![15][2] = 2505
                        anIntArrayArray9194!![15][3] = 2746
                        anIntArrayArray9194!![15][1] = 2377
                        anIntArrayArray9194!![15][0] = 4096
                        break@while_157_
                    } while (false)
                    anIntArrayArray9194 = Array(4) { IntArray(4) }
                    anIntArrayArray9194!![0][1] = 0
                    anIntArrayArray9194!![0][2] = 4096
                    anIntArrayArray9194!![0][3] = 0
                    anIntArrayArray9194!![0][0] = 2048
                    anIntArrayArray9194!![1][0] = 2867
                    anIntArrayArray9194!![1][3] = 0
                    anIntArrayArray9194!![1][1] = 4096
                    anIntArrayArray9194!![1][2] = 4096
                    anIntArrayArray9194!![2][0] = 3276
                    anIntArrayArray9194!![2][1] = 4096
                    anIntArrayArray9194!![2][3] = 0
                    anIntArrayArray9194!![2][2] = 4096
                    anIntArrayArray9194!![3][0] = 4096
                    anIntArrayArray9194!![3][2] = 0
                    anIntArrayArray9194!![3][1] = 4096
                    anIntArrayArray9194!![3][3] = 0
                    break@while_157_
                } while (false)
                throw RuntimeException("Invalid gradient preset")
            }
        } while (false)
    }

    private fun method3079() {
        val i_12_ = anIntArrayArray9194!!.size
        if (i_12_ > 0) {
            for (i_13_ in 0..256) {
                var i_14_ = 0
                val i_15_ = i_13_ shl 4
                for (i_16_ in 0..<i_12_) {
                    if (anIntArrayArray9194!![i_16_][0] > i_15_) break
                    i_14_++
                }
                var i_17_: Int
                var i_18_: Int
                var i_19_: Int
                if (i_14_ >= i_12_) {
                    val `is` = anIntArrayArray9194!![i_12_ + -1]
                    i_17_ = `is`[1]
                    i_19_ = `is`[2]
                    i_18_ = `is`[3]
                } else {
                    val `is` = anIntArrayArray9194!![i_14_]
                    if (i_14_ <= 0) {
                        i_17_ = `is`[1]
                        i_18_ = `is`[3]
                        i_19_ = `is`[2]
                    } else {
                        val is_20_ = anIntArrayArray9194!![-1 + i_14_]
                        val i_21_ = ((i_15_ - is_20_[0] shl 12) / (-is_20_[0] + `is`[0]))
                        val i_22_ = -i_21_ + 4096
                        i_17_ = is_20_[1] * i_22_ + `is`[1] * i_21_ shr 12
                        i_18_ = (i_21_ * `is`[3] - -(i_22_ * is_20_[3]) shr 12)
                        i_19_ = is_20_[2] * i_22_ + `is`[2] * i_21_ shr 12
                    }
                }
                i_19_ = i_19_ shr 4
                i_17_ = i_17_ shr 4
                i_18_ = i_18_ shr 4
                if (i_17_ < 0) i_17_ = 0
                else if (i_17_ > 255) i_17_ = 255
                if (i_18_ >= 0) {
                    if (i_18_ > 255) i_18_ = 255
                } else i_18_ = 0
                if (i_19_ >= 0) {
                    if (i_19_ > 255) i_19_ = 255
                } else i_19_ = 0
                anIntArray9199[i_13_] = (or(i_18_, or(i_17_ shl 16, i_19_ shl 8)))
            }
        }
    }

    companion object {
        fun or(i: Int, i_2_: Int): Int {
            return i or i_2_
        }
    }
}
