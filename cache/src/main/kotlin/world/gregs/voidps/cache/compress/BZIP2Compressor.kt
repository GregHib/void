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
    @Deprecated("Use GZIP")
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
        var i: Int
        var intArray1: IntArray
        var intArray2: IntArray
        var intArray3: IntArray
        var bool28 = true
        while (bool28) {
            var i29 = method152(8)
            if (i29 == 23) {
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
            var i30 = method152(8)
            anInt3083 = anInt3083 shl 8 or (i30 and 0xff)
            i30 = method152(8)
            anInt3083 = anInt3083 shl 8 or (i30 and 0xff)
            i30 = method152(8)
            anInt3083 = anInt3083 shl 8 or (i30 and 0xff)
            for (i31 in 0..15) {
                i29 = method152(1)
                aBooleanArray3072[i31] = i29 == 1
            }
            for (i32 in 0..255) {
                aBooleanArray3103[i32] = false
            }
            for (i33 in 0..15) {
                if (aBooleanArray3072[i33]) {
                    for (i34 in 0..15) {
                        i29 = method152(1)
                        if (i29 == 1) {
                            aBooleanArray3103[i33 * 16 + i34] = true
                        }
                    }
                }
            }
            var anInt3073 = 0
            for (j in 0..255) {
                if (aBooleanArray3103[j]) {
                    aByteArray3107[anInt3073] = j.toByte()
                    anInt3073++
                }
            }
            val i35 = anInt3073 + 2
            val i36 = method152(3)
            val i37 = method152(15)
            for (i38 in 0 until i37) {
                var i39 = 0
                while (true) {
                    i29 = method152(1)
                    if (i29 == 0) {
                        break
                    }
                    i39++
                }
                aByteArray3094[i38] = i39.toByte()
            }
            val is40 = ByteArray(6)
            for (i41 in 0 until i36) {
                is40[i41] = i41.toByte()
            }
            for (i42 in 0 until i37) {
                var i43 = aByteArray3094[i42]
                val i44 = is40[i43.toInt()]
                while (i43 > 0) {
                    is40[i43.toInt()] = is40[i43 - 1]
                    i43--
                }
                is40[0] = i44
                aByteArray3076[i42] = i44
            }
            for (i45 in 0 until i36) {
                var i46 = method152(5)
                for (i47 in 0 until i35) {
                    while (true) {
                        i29 = method152(1)
                        if (i29 == 0) {
                            break
                        }
                        i29 = method152(1)
                        if (i29 == 0) {
                            i46++
                        } else {
                            i46--
                        }
                    }
                    aByteArrayArray3098[i45][i47] = i46.toByte()
                }
            }
            for (i48 in 0 until i36) {
                var i49 = 32
                var i50: Byte = 0
                for (i51 in 0 until i35) {
                    if (aByteArrayArray3098[i48][i51] > i50) {
                        i50 = aByteArrayArray3098[i48][i51]
                    }
                    if (aByteArrayArray3098[i48][i51] < i49) {
                        i49 = aByteArrayArray3098[i48][i51].toInt()
                    }
                }
                method145(
                    anIntArrayArray3095[i48],
                    anIntArrayArray3082[i48],
                    anIntArrayArray3099[i48],
                    aByteArrayArray3098[i48],
                    i49,
                    i50.toInt(),
                    i35,
                )
                anIntArray3090[i48] = i49
            }
            val i52 = anInt3073 + 1
            var i53 = 0
            var i54: Int
            for (i55 in 0..255) {
                anIntArray3075[i55] = 0
            }
            var i56 = 4095
            for (i57 in 15 downTo 0) {
                for (i58 in 15 downTo 0) {
                    aByteArray3101[i56] = (i57 * 16 + i58).toByte()
                    i56--
                }
                anIntArray3092[i57] = i56 + 1
            }
            var i59 = 0
            i54 = 50
            val index = aByteArray3076[i53]
            i = anIntArray3090[index.toInt()]
            intArray1 = anIntArrayArray3095[index.toInt()]
            intArray3 = anIntArrayArray3099[index.toInt()]
            intArray2 = anIntArrayArray3082[index.toInt()]
            i54--
            var i61 = i
            var i62: Int
            var i63: Int
            i63 = method152(i61)
            while (i63 > intArray1[i61]) {
                i61++
                i62 = method152(1)
                i63 = i63 shl 1 or i62
            }
            var i64 = intArray3[i63 - intArray2[i61]]
            while (i64 != i52) {
                if (i64 == 0 || i64 == 1) {
                    var i65 = -1
                    var i66 = 1
                    do {
                        i65 += if (i64 == 0) {
                            i66
                        } else {
                            2 * i66
                        }
                        i66 *= 2
                        if (i54 == 0) {
                            i53++
                            i54 = 50
                            val index2 = aByteArray3076[i53]
                            i = anIntArray3090[index2.toInt()]
                            intArray1 = anIntArrayArray3095[index2.toInt()]
                            intArray3 = anIntArrayArray3099[index2.toInt()]
                            intArray2 = anIntArrayArray3082[index2.toInt()]
                        }
                        i54--
                        i61 = i
                        i63 = method152(i61)
                        while (i63 > intArray1[i61]) {
                            i61++
                            i62 = method152(1)
                            i63 = i63 shl 1 or i62
                        }
                        i64 = intArray3[i63 - intArray2[i61]]
                    } while (i64 == 0 || i64 == 1)
                    i65++
                    i30 = aByteArray3107[aByteArray3101[anIntArray3092[0]].toInt() and 0xff].toInt()
                    anIntArray3075[i30 and 0xff] += i65
                    while (i65 > 0) {
                        anIntArray5786[i59] = i30 and 0xff
                        i59++
                        i65--
                    }
                } else {
                    var i68 = i64 - 1
                    if (i68 < 16) {
                        val i69 = anIntArray3092[0]
                        i29 = aByteArray3101[i69 + i68].toInt()
                        while (i68 > 3) {
                            val i70 = i69 + i68
                            aByteArray3101[i70] = aByteArray3101[i70 - 1]
                            aByteArray3101[i70 - 1] = aByteArray3101[i70 - 2]
                            aByteArray3101[i70 - 2] = aByteArray3101[i70 - 3]
                            aByteArray3101[i70 - 3] = aByteArray3101[i70 - 4]
                            i68 -= 4
                        }
                        while (i68 > 0) {
                            aByteArray3101[i69 + i68] = aByteArray3101[i69 + i68 - 1]
                            i68--
                        }
                        aByteArray3101[i69] = i29.toByte()
                    } else {
                        var i71 = i68 / 16
                        val i72 = i68 % 16
                        var i73 = anIntArray3092[i71] + i72
                        i29 = aByteArray3101[i73].toInt()
                        while (i73 > anIntArray3092[i71]) {
                            aByteArray3101[i73] = aByteArray3101[i73 - 1]
                            i73--
                        }
                        anIntArray3092[i71]++
                        while (i71 > 0) {
                            anIntArray3092[i71]--
                            aByteArray3101[anIntArray3092[i71]] = aByteArray3101[anIntArray3092[i71 - 1] + 16 - 1]
                            i71--
                        }
                        anIntArray3092[0]--
                        aByteArray3101[anIntArray3092[0]] = i29.toByte()
                        if (anIntArray3092[0] == 0) {
                            var i74 = 4095
                            for (i75 in 15 downTo 0) {
                                for (i76 in 15 downTo 0) {
                                    aByteArray3101[i74] = aByteArray3101[anIntArray3092[i75] + i76]
                                    i74--
                                }
                                anIntArray3092[i75] = i74 + 1
                            }
                        }
                    }
                    anIntArray3075[aByteArray3107[i29 and 0xff].toInt() and 0xff]++
                    anIntArray5786[i59] = aByteArray3107[i29 and 0xff].toInt() and 0xff
                    i59++
                    if (i54 == 0) {
                        i53++
                        i54 = 50
                        val i77 = aByteArray3076[i53]
                        i = anIntArray3090[i77.toInt()]
                        intArray1 = anIntArrayArray3095[i77.toInt()]
                        intArray3 = anIntArrayArray3099[i77.toInt()]
                        intArray2 = anIntArrayArray3082[i77.toInt()]
                    }
                    i54--
                    i61 = i
                    i63 = method152(i61)
                    while (i63 > intArray1[i61]) {
                        i61++
                        i62 = method152(1)
                        i63 = i63 shl 1 or i62
                    }
                    i64 = intArray3[i63 - intArray2[i61]]
                }
            }
            anInt3080 = 0
            aByte3108 = 0.toByte()
            anIntArray3091[0] = 0
            for (i78 in 1..256) {
                anIntArray3091[i78] = anIntArray3075[i78 - 1]
            }
            for (i79 in 1..256) {
                anIntArray3091[i79] += anIntArray3091[i79 - 1]
            }
            for (i80 in 0 until i59) {
                i30 = (anIntArray5786[i80] and 0xff).toByte().toInt()
                anIntArray5786[anIntArray3091[i30 and 0xff]] = anIntArray5786[anIntArray3091[i30 and 0xff]] or (i80 shl 8)
                anIntArray3091[i30 and 0xff]++
            }
            anInt3106 = anIntArray5786[anInt3083] shr 8
            anInt3071 = 0
            anInt3106 = anIntArray5786[anInt3106]
            anInt3070 = (anInt3106 and 0xff).toByte().toInt()
            anInt3106 = anInt3106 shr 8
            anInt3071++
            anInt3077 = i59
            method151()
            bool28 = anInt3071 == anInt3077 + 1 && anInt3080 == 0
        }
    }

    private fun method152(arg0: Int): Int {
        while (true) {
            if (anInt3088 >= arg0) {
                val i93 = anInt3078 shr anInt3088 - arg0 and (1 shl arg0) - 1
                anInt3088 -= arg0
                return i93
            }
            anInt3078 = anInt3078 shl 8 or (compressed[startOffset].toInt() and 0xff)
            anInt3088 += 8
            startOffset++
            anInt3085++
        }
    }

    private fun method151() {
        var i = aByte3108
        var i81 = anInt3080
        var i82 = anInt3071
        var i83 = anInt3070
        val data = anIntArray5786
        var i84 = anInt3106
        val decompressed = decompressed
        var i86 = anInt3100
        var length = decompressedLength
        val i88 = length
        val i89 = anInt3077 + 1
        while_68_@ while (true) {
            if (i81 > 0) {
                while (true) {
                    if (length == 0) {
                        break@while_68_
                    }
                    if (i81 == 1) {
                        break
                    }
                    decompressed[i86] = i
                    i81--
                    i86++
                    length--
                }
                if (length == 0) {
                    i81 = 1
                    break
                }
                decompressed[i86] = i
                i86++
                length--
            }
            var bool = true
            while (bool) {
                bool = false
                if (i82 == i89) {
                    i81 = 0
                    break@while_68_
                }
                i = i83.toByte()
                i84 = data[i84]
                val i90 = (i84 and 0xff).toByte().toInt()
                i84 = i84 shr 8
                i82++
                if (i90 != i83) {
                    i83 = i90
                    if (length == 0) {
                        i81 = 1
                        break@while_68_
                    }
                    decompressed[i86] = i
                    i86++
                    length--
                    bool = true
                } else if (i82 == i89) {
                    if (length == 0) {
                        i81 = 1
                        break@while_68_
                    }
                    decompressed[i86] = i
                    i86++
                    length--
                    bool = true
                }
            }
            i81 = 2
            i84 = data[i84]
            var i91 = (i84 and 0xff).toByte().toInt()
            i84 = i84 shr 8
            if (++i82 != i89) {
                if (i91 != i83) {
                    i83 = i91
                } else {
                    i81 = 3
                    i84 = data[i84]
                    i91 = (i84 and 0xff).toByte().toInt()
                    i84 = i84 shr 8
                    if (++i82 != i89) {
                        if (i91 != i83) {
                            i83 = i91
                        } else {
                            i84 = data[i84]
                            i91 = (i84 and 0xff).toByte().toInt()
                            i84 = i84 shr 8
                            i82++
                            i81 = (i91 and 0xff) + 4
                            i84 = data[i84]
                            i83 = (i84 and 0xff).toByte().toInt()
                            i84 = i84 shr 8
                            i82++
                        }
                    }
                }
            }
        }
        anInt3097 += i88 - length
        aByte3108 = i
        anInt3080 = i81
        anInt3071 = i82
        anInt3070 = i83
        anIntArray5786 = data
        anInt3106 = i84
        this.decompressed = decompressed
        anInt3100 = i86
        decompressedLength = length
    }

    private fun method145(arg0: IntArray, arg1: IntArray, arg2: IntArray, arg3: ByteArray, arg4: Int, arg5: Int, arg6: Int) {
        var i = 0
        for (i0 in arg4..arg5) {
            for (i1 in 0 until arg6) {
                if (arg3[i1].toInt() == i0) {
                    arg2[i] = i1
                    i++
                }
            }
        }
        for (index in 0..22) {
            arg1[index] = 0
        }
        for (index in 0 until arg6) {
            arg1[arg3[index] + 1]++
        }
        for (index in 1..22) {
            arg1[index] += arg1[index - 1]
        }
        for (index in 0..22) {
            arg0[index] = 0
        }
        var i6 = 0
        for (index in arg4..arg5) {
            i6 += arg1[index + 1] - arg1[index]
            arg0[index] = i6 - 1
            i6 = i6 shl 1
        }
        for (index in arg4 + 1..arg5) {
            arg1[index] = (arg0[index - 1] + 1 shl 1) - arg1[index]
        }
    }
}
