package world.gregs.voidps.cache.compress

/**
 * A class representing the BZIP2 (de)compressor.
 * @author Jagex
 * @author Displee
 */
@Deprecated("Decompression is slow, use gzip for better performance. Kept for backwards compatability.")
internal class BZIP2Compressor {
    private var anInt3070 = 0
    private var anInt3071 = 0
    private var aBooleanArray3072: BooleanArray = BooleanArray(16)
    private var startOffset = 0
    private var anIntArray3075: IntArray = IntArray(256)
    private var aByteArray3076: ByteArray = ByteArray(18002)
    private var anInt3077 = 0
    private var anInt3078 = 0
    private var decompressed: ByteArray = ByteArray(0)
    private var anInt3080 = 0
    private var anIntArrayArray3082 = Array(6) { IntArray(258) }
    private var anInt3083 = 0
    private var anInt3085 = 0
    private var anInt3088 = 0
    private var anIntArray3090: IntArray = IntArray(6)
    private var anIntArray3091: IntArray = IntArray(257)
    private var anIntArray3092: IntArray = IntArray(16)
    private var decompressedLength = 0
    private var aByteArray3094 = ByteArray(18002)
    private var anIntArrayArray3095: Array<IntArray> = Array(6) { IntArray(258) }
    private var anInt3097 = 0
    private var aByteArrayArray3098: Array<ByteArray> = Array(6) { ByteArray(258) }
    private var anIntArrayArray3099: Array<IntArray> = Array(6) { IntArray(258) }
    private var anInt3100 = 0
    private var aByteArray3101: ByteArray = ByteArray(4096)
    private var aBooleanArray3103: BooleanArray = BooleanArray(256)
    private var anInt3106 = 0
    private var aByteArray3107: ByteArray = ByteArray(256)
    private var aByte3108: Byte = 0
    private var compressed: ByteArray = ByteArray(0)
    private var anIntArray5786: IntArray = IntArray(100000)

    /**
     * Decompress a compressed BZIP2 file.
     * @param decompressed       An empty byte array where we put the decompressed data in.
     * @param decompressedLength The length to decompress.
     * @param archiveData        The compressed BZIP2 file.
     * @param startOffset        The start offset.
     * @return The decompressed length.
     */
    fun decompress(decompressed: ByteArray, decompressedLength: Int, archiveData: ByteArray, startOffset: Int): Int {
        compressed = archiveData
        this.startOffset = startOffset
        this.decompressed = decompressed
        anInt3100 = 0
        this.decompressedLength = decompressedLength
        anInt3088 = 0
        anInt3078 = 0
        anInt3085 = 0
        anInt3097 = 0
        decompress()
        compressed = byteArrayOf()
        this.decompressed = byteArrayOf()
        return decompressedLength - this.decompressedLength
    }

    /**
     * Decompress a BZIP2 block entry.
     */
    private fun decompress() {
        var i = 0
        var intArray1 = IntArray(0)
        var intArray2 = IntArray(0)
        var intArray3 = IntArray(0)
        var bool_28_ = true
        while (bool_28_) {
            var i_29_ = method152(8)
            if (i_29_ == 23) {
                break
            }
            method152(8)
            method152(8)
            method152(8)
            method152(8)
            method152(8)
            method152(8)
            method152(8)
            method152(8)
            method152(8)
            method152(1)
            anInt3083 = 0
            var i_30_ = method152(8)
            anInt3083 = anInt3083 shl 8 or (i_30_ and 0xff)
            i_30_ = method152(8)
            anInt3083 = anInt3083 shl 8 or (i_30_ and 0xff)
            i_30_ = method152(8)
            anInt3083 = anInt3083 shl 8 or (i_30_ and 0xff)
            for (i_31_ in 0..15) {
                i_29_ = method152(1)
                if (i_29_ == 1) {
                    aBooleanArray3072[i_31_] = true
                } else {
                    aBooleanArray3072[i_31_] = false
                }
            }
            for (i_32_ in 0..255) {
                aBooleanArray3103[i_32_] = false
            }
            for (i_33_ in 0..15) {
                if (aBooleanArray3072[i_33_]) {
                    for (i_34_ in 0..15) {
                        i_29_ = method152(1)
                        if (i_29_ == 1) {
                            aBooleanArray3103[i_33_ * 16 + i_34_] = true
                        }
                    }
                }
            }
            var anInt3073 = 0
            for (i in 0..255) {
                if (aBooleanArray3103[i]) {
                    aByteArray3107[anInt3073] = i.toByte()
                    anInt3073++
                }
            }
            val i_35_ = anInt3073 + 2
            val i_36_ = method152(3)
            val i_37_ = method152(15)
            for (i_38_ in 0 until i_37_) {
                var i_39_ = 0
                while (true) {
                    i_29_ = method152(1)
                    if (i_29_ == 0) {
                        break
                    }
                    i_39_++
                }
                aByteArray3094[i_38_] = i_39_.toByte()
            }
            val is_40_ = ByteArray(6)
            for (i_41_ in 0 until i_36_) {
                is_40_[i_41_] = i_41_.toByte()
            }
            for (i_42_ in 0 until i_37_) {
                var i_43_ = aByteArray3094[i_42_]
                val i_44_ = is_40_[i_43_.toInt()]
                while (i_43_ > 0) {
                    is_40_[i_43_.toInt()] = is_40_[i_43_ - 1]
                    i_43_--
                }
                is_40_[0] = i_44_
                aByteArray3076[i_42_] = i_44_
            }
            for (i_45_ in 0 until i_36_) {
                var i_46_ = method152(5)
                for (i_47_ in 0 until i_35_) {
                    while (true) {
                        i_29_ = method152(1)
                        if (i_29_ == 0) {
                            break
                        }
                        i_29_ = method152(1)
                        if (i_29_ == 0) {
                            i_46_++
                        } else {
                            i_46_--
                        }
                    }
                    aByteArrayArray3098[i_45_][i_47_] = i_46_.toByte()
                }
            }
            for (i_48_ in 0 until i_36_) {
                var i_49_ = 32
                var i_50_: Byte = 0
                for (i_51_ in 0 until i_35_) {
                    if (aByteArrayArray3098[i_48_][i_51_] > i_50_) {
                        i_50_ = aByteArrayArray3098[i_48_][i_51_]
                    }
                    if (aByteArrayArray3098[i_48_][i_51_] < i_49_) {
                        i_49_ = aByteArrayArray3098[i_48_][i_51_].toInt()
                    }
                }
                method145(anIntArrayArray3095[i_48_],
                    anIntArrayArray3082[i_48_],
                    anIntArrayArray3099[i_48_],
                    aByteArrayArray3098[i_48_],
                    i_49_,
                    i_50_.toInt(),
                    i_35_)
                anIntArray3090[i_48_] = i_49_
            }
            val i_52_ = anInt3073 + 1
            var i_53_ = -1
            var i_54_ = 0
            for (i_55_ in 0..255) {
                anIntArray3075[i_55_] = 0
            }
            var i_56_ = 4095
            for (i_57_ in 15 downTo 0) {
                for (i_58_ in 15 downTo 0) {
                    aByteArray3101[i_56_] = (i_57_ * 16 + i_58_).toByte()
                    i_56_--
                }
                anIntArray3092[i_57_] = i_56_ + 1
            }
            var i_59_ = 0
            if (i_54_ == 0) {
                i_53_++
                i_54_ = 50
                val i_60_ = aByteArray3076[i_53_]
                i = anIntArray3090[i_60_.toInt()]
                intArray1 = anIntArrayArray3095[i_60_.toInt()]
                intArray3 = anIntArrayArray3099[i_60_.toInt()]
                intArray2 = anIntArrayArray3082[i_60_.toInt()]
            }
            i_54_--
            var i_61_ = i
            var i_62_: Int
            var i_63_: Int
            i_63_ = method152(i_61_)
            while (i_63_ > intArray1[i_61_]) {
                i_61_++
                i_62_ = method152(1)
                i_63_ = i_63_ shl 1 or i_62_
            }
            var i_64_ = intArray3[i_63_ - intArray2[i_61_]]
            while (i_64_ != i_52_) {
                if (i_64_ == 0 || i_64_ == 1) {
                    var i_65_ = -1
                    var i_66_ = 1
                    do {
                        if (i_64_ == 0) {
                            i_65_ += i_66_
                        } else if (i_64_ == 1) {
                            i_65_ += 2 * i_66_
                        }
                        i_66_ *= 2
                        if (i_54_ == 0) {
                            i_53_++
                            i_54_ = 50
                            val i_67_ = aByteArray3076[i_53_]
                            i = anIntArray3090[i_67_.toInt()]
                            intArray1 = anIntArrayArray3095[i_67_.toInt()]
                            intArray3 = anIntArrayArray3099[i_67_.toInt()]
                            intArray2 = anIntArrayArray3082[i_67_.toInt()]
                        }
                        i_54_--
                        i_61_ = i
                        i_63_ = method152(i_61_)
                        while (i_63_ > intArray1[i_61_]) {
                            i_61_++
                            i_62_ = method152(1)
                            i_63_ = i_63_ shl 1 or i_62_
                        }
                        i_64_ = intArray3[i_63_ - intArray2[i_61_]]
                    } while (i_64_ == 0 || i_64_ == 1)
                    i_65_++
                    i_30_ = aByteArray3107[aByteArray3101[anIntArray3092[0]].toInt() and 0xff].toInt()
                    anIntArray3075[i_30_ and 0xff] += i_65_
                    while ( /**/i_65_ > 0) {
                        anIntArray5786[i_59_] = i_30_ and 0xff
                        i_59_++
                        i_65_--
                    }
                } else {
                    var i_68_ = i_64_ - 1
                    if (i_68_ < 16) {
                        val i_69_ = anIntArray3092[0]
                        i_29_ = aByteArray3101[i_69_ + i_68_].toInt()
                        while ( /**/i_68_ > 3) {
                            val i_70_ = i_69_ + i_68_
                            aByteArray3101[i_70_] = aByteArray3101[i_70_ - 1]
                            aByteArray3101[i_70_ - 1] = aByteArray3101[i_70_ - 2]
                            aByteArray3101[i_70_ - 2] = aByteArray3101[i_70_ - 3]
                            aByteArray3101[i_70_ - 3] = aByteArray3101[i_70_ - 4]
                            i_68_ -= 4
                        }
                        while ( /**/i_68_ > 0) {
                            aByteArray3101[i_69_ + i_68_] = aByteArray3101[i_69_ + i_68_ - 1]
                            i_68_--
                        }
                        aByteArray3101[i_69_] = i_29_.toByte()
                    } else {
                        var i_71_ = i_68_ / 16
                        val i_72_ = i_68_ % 16
                        var i_73_ = anIntArray3092[i_71_] + i_72_
                        i_29_ = aByteArray3101[i_73_].toInt()
                        while (i_73_ > anIntArray3092[i_71_]) {
                            aByteArray3101[i_73_] = aByteArray3101[i_73_ - 1]
                            i_73_--
                        }
                        anIntArray3092[i_71_]++
                        while (i_71_ > 0) {
                            anIntArray3092[i_71_]--
                            aByteArray3101[anIntArray3092[i_71_]] = aByteArray3101[anIntArray3092[i_71_ - 1] + 16 - 1]
                            i_71_--
                        }
                        anIntArray3092[0]--
                        aByteArray3101[anIntArray3092[0]] = i_29_.toByte()
                        if (anIntArray3092[0] == 0) {
                            var i_74_ = 4095
                            for (i_75_ in 15 downTo 0) {
                                for (i_76_ in 15 downTo 0) {
                                    aByteArray3101[i_74_] = aByteArray3101[anIntArray3092[i_75_] + i_76_]
                                    i_74_--
                                }
                                anIntArray3092[i_75_] = i_74_ + 1
                            }
                        }
                    }
                    anIntArray3075[aByteArray3107[i_29_ and 0xff].toInt() and 0xff]++
                    anIntArray5786[i_59_] = aByteArray3107[i_29_ and 0xff].toInt() and 0xff
                    i_59_++
                    if (i_54_ == 0) {
                        i_53_++
                        i_54_ = 50
                        val i_77_ = aByteArray3076[i_53_]
                        i = anIntArray3090[i_77_.toInt()]
                        intArray1 = anIntArrayArray3095[i_77_.toInt()]
                        intArray3 = anIntArrayArray3099[i_77_.toInt()]
                        intArray2 = anIntArrayArray3082[i_77_.toInt()]
                    }
                    i_54_--
                    i_61_ = i
                    i_63_ = method152(i_61_)
                    while (i_63_ > intArray1[i_61_]) {
                        i_61_++
                        i_62_ = method152(1)
                        i_63_ = i_63_ shl 1 or i_62_
                    }
                    i_64_ = intArray3[i_63_ - intArray2[i_61_]]
                }
            }
            anInt3080 = 0
            aByte3108 = 0.toByte()
            anIntArray3091[0] = 0
            for (i_78_ in 1..256) {
                anIntArray3091[i_78_] = anIntArray3075[i_78_ - 1]
            }
            for (i_79_ in 1..256) {
                anIntArray3091[i_79_] += anIntArray3091[i_79_ - 1]
            }
            for (i_80_ in 0 until i_59_) {
                i_30_ = (anIntArray5786[i_80_] and 0xff).toByte().toInt()
                anIntArray5786[anIntArray3091[i_30_ and 0xff]] = anIntArray5786[anIntArray3091[i_30_ and 0xff]] or (i_80_ shl 8)
                anIntArray3091[i_30_ and 0xff]++
            }
            anInt3106 = anIntArray5786[anInt3083] shr 8
            anInt3071 = 0
            anInt3106 = anIntArray5786[anInt3106]
            anInt3070 = (anInt3106 and 0xff).toByte().toInt()
            anInt3106 = anInt3106 shr 8
            anInt3071++
            anInt3077 = i_59_
            method151()
            bool_28_ = anInt3071 == anInt3077 + 1 && anInt3080 == 0
        }
    }

    private fun method152(arg0: Int): Int {
        while (true) {
            if (anInt3088 >= arg0) {
                val i_93_ = anInt3078 shr anInt3088 - arg0 and (1 shl arg0) - 1
                anInt3088 -= arg0
                return i_93_
            }
            anInt3078 = anInt3078 shl 8 or (compressed[startOffset].toInt() and 0xff)
            anInt3088 += 8
            startOffset++
            anInt3085++
        }
    }

    private fun method151() {
        var i = aByte3108
        var i_81_ = anInt3080
        var i_82_ = anInt3071
        var i_83_ = anInt3070
        val data = anIntArray5786
        var i_84_ = anInt3106
        val is_85_ = decompressed
        var i_86_ = anInt3100
        var i_87_ = decompressedLength
        val i_88_ = i_87_
        val i_89_ = anInt3077 + 1
        while_68_@ while (true) {
            if (i_81_ > 0) {
                while (true) {
                    if (i_87_ == 0) {
                        break@while_68_
                    }
                    if (i_81_ == 1) {
                        break
                    }
                    is_85_[i_86_] = i
                    i_81_--
                    i_86_++
                    i_87_--
                }
                if (i_87_ == 0) {
                    i_81_ = 1
                    break
                }
                is_85_[i_86_] = i
                i_86_++
                i_87_--
            }
            var bool = true
            while (bool) {
                bool = false
                if (i_82_ == i_89_) {
                    i_81_ = 0
                    break@while_68_
                }
                i = i_83_.toByte()
                i_84_ = data[i_84_]
                val i_90_ = (i_84_ and 0xff).toByte().toInt()
                i_84_ = i_84_ shr 8
                i_82_++
                if (i_90_ != i_83_) {
                    i_83_ = i_90_
                    if (i_87_ == 0) {
                        i_81_ = 1
                        break@while_68_
                    }
                    is_85_[i_86_] = i
                    i_86_++
                    i_87_--
                    bool = true
                } else if (i_82_ == i_89_) {
                    if (i_87_ == 0) {
                        i_81_ = 1
                        break@while_68_
                    }
                    is_85_[i_86_] = i
                    i_86_++
                    i_87_--
                    bool = true
                }
            }
            i_81_ = 2
            i_84_ = data[i_84_]
            var i_91_ = (i_84_ and 0xff).toByte().toInt()
            i_84_ = i_84_ shr 8
            if (++i_82_ != i_89_) {
                if (i_91_ != i_83_) {
                    i_83_ = i_91_
                } else {
                    i_81_ = 3
                    i_84_ = data[i_84_]
                    i_91_ = (i_84_ and 0xff).toByte().toInt()
                    i_84_ = i_84_ shr 8
                    if (++i_82_ != i_89_) {
                        if (i_91_ != i_83_) {
                            i_83_ = i_91_
                        } else {
                            i_84_ = data[i_84_]
                            i_91_ = (i_84_ and 0xff).toByte().toInt()
                            i_84_ = i_84_ shr 8
                            i_82_++
                            i_81_ = (i_91_ and 0xff) + 4
                            i_84_ = data[i_84_]
                            i_83_ = (i_84_ and 0xff).toByte().toInt()
                            i_84_ = i_84_ shr 8
                            i_82_++
                        }
                    }
                }
            }
        }
        anInt3097 += i_88_ - i_87_
        aByte3108 = i
        anInt3080 = i_81_
        anInt3071 = i_82_
        anInt3070 = i_83_
        anIntArray5786 = data
        anInt3106 = i_84_
        decompressed = is_85_
        anInt3100 = i_86_
        decompressedLength = i_87_
    }

    private fun method145(arg0: IntArray, arg1: IntArray, arg2: IntArray, arg3: ByteArray, arg4: Int, arg5: Int, arg6: Int) {
        var i = 0
        for (i_0_ in arg4..arg5) {
            for (i_1_ in 0 until arg6) {
                if (arg3[i_1_].toInt() == i_0_) {
                    arg2[i] = i_1_
                    i++
                }
            }
        }
        for (i_2_ in 0..22) {
            arg1[i_2_] = 0
        }
        for (i_3_ in 0 until arg6) {
            arg1[arg3[i_3_] + 1]++
        }
        for (i_4_ in 1..22) {
            arg1[i_4_] += arg1[i_4_ - 1]
        }
        for (i_5_ in 0..22) {
            arg0[i_5_] = 0
        }
        var i_6_ = 0
        for (i_7_ in arg4..arg5) {
            i_6_ += arg1[i_7_ + 1] - arg1[i_7_]
            arg0[i_7_] = i_6_ - 1
            i_6_ = i_6_ shl 1
        }
        for (i_8_ in arg4 + 1..arg5) {
            arg1[i_8_] = (arg0[i_8_ - 1] + 1 shl 1) - arg1[i_8_]
        }
    }
}