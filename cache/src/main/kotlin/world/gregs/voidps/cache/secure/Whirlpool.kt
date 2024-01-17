package world.gregs.voidps.cache.secure

class Whirlpool {
    private val block = LongArray(8)
    private val hash = LongArray(8)
    private val aLongArray6637 = LongArray(8)
    private val aLongArray6638 = LongArray(8)
    private val state = LongArray(8)
    private val bitLength = ByteArray(32)
    private val buffer = ByteArray(64)
    private var bufferBits = 0
    private var bufferPosition = 0
    private val aLongArrayArray6630 = Array(8) { LongArray(256) }
    private val aLongArray6631 = LongArray(11)

    init {
        val chars =
            "\u1823\uc6e8\u87b8\u014f\u36a6\ud2f5\u796f\u9152\u60bc\u9b8e\ua30c\u7b35\u1de0\ud7c2\u2e4b\ufe57\u1577\u37e5\u9ff0\u4ada\u58c9\u290a\ub1a0\u6b85\ubd5d\u10f4\ucb3e\u0567\ue427\u418b\ua77d\u95d8\ufbee\u7c66\udd17\u479e\uca2d\ubf07\uad5a\u8333\u6302\uaa71\uc819\u49d9\uf2e3\u5b88\u9a26\u32b0\ue90f\ud580\ubecd\u3448\uff7a\u905f\u2068\u1aae\ub454\u9322\u64f1\u7312\u4008\uc3ec\udba1\u8d3d\u9700\ucf2b\u7682\ud61b\ub5af\u6a50\u45f3\u30ef\u3f55\ua2ea\u65ba\u2fc0\ude1c\ufd4d\u9275\u068a\ub2e6\u0e1f\u62d4\ua896\uf9c5\u2559\u8472\u394c\u5e78\u388c\ud1a5\ue261\ub321\u9c1e\u43c7\ufc04\u5199\u6d0d\ufadf\u7e24\u3bab\uce11\u8f4e\ub7eb\u3c81\u94f7\ub913\u2cd3\ue76e\uc403\u5644\u7fa9\u2abb\uc153\udc0b\u9d6c\u3174\uf646\uac89\u14e1\u163a\u6909\u70b6\ud0ed\ucc42\u98a4\u285c\uf886"
        for (i in 0..255) {
            val code = chars[i / 2].code
            val l = if (i and 0x1 == 0) (code ushr 8).toLong() else (code and 0xff).toLong()
            var l_38_ = l shl 1
            if (l_38_ >= 256L) {
                l_38_ = l_38_ xor 0x11dL
            }
            var l_39_ = l_38_ shl 1
            if (l_39_ >= 256L) {
                l_39_ = l_39_ xor 0x11dL
            }
            val l_40_ = l_39_ xor l
            var l_41_ = l_39_ shl 1
            if (l_41_ >= 256L) {
                l_41_ = l_41_ xor 0x11dL
            }
            val l_42_ = l_41_ xor l
            aLongArrayArray6630[0][i] = l shl 56 or (l shl 48) or (l_39_ shl 40) or (l shl 32) or (l_41_ shl 24) or (l_40_ shl 16) or (l_38_ shl 8) or l_42_
            for (i_43_ in 1..7) {
                aLongArrayArray6630[i_43_][i] = aLongArrayArray6630[i_43_ - 1][i] ushr 8 or (aLongArrayArray6630[i_43_ - 1][i] shl 56)
            }
        }
        aLongArray6631[0] = 0L
        for (i in 1..10) {
            val i_44_ = 8 * (i - 1)
            aLongArray6631[i] = aLongArrayArray6630[0][i_44_] and 0xffffffffffffffL.inv() xor
                    (aLongArrayArray6630[1][i_44_ + 1] and 0xff000000000000L) xor
                    (aLongArrayArray6630[2][i_44_ + 2] and 0xff0000000000L) xor
                    (aLongArrayArray6630[3][i_44_ + 3] and 0xff00000000L) xor
                    (aLongArrayArray6630[4][4 + i_44_] and 0xff000000L) xor
                    (aLongArrayArray6630[5][5 + i_44_] and 0xff0000L) xor
                    (aLongArrayArray6630[6][6 + i_44_] and 0xff00L) xor
                    (aLongArrayArray6630[7][7 + i_44_] and 0xffL)
        }
        reset()
    }

    fun reset() {
        for (i in 0..31) {
            bitLength[i] = 0.toByte()
        }
        bufferPosition = 0
        bufferBits = 0
        buffer[0] = 0.toByte()
        for (i_3_ in 0..7) {
            hash[i_3_] = 0L
        }
    }

    private fun processBuffer() {
        var i_4_ = 0
        var i_5_ = 0
        while (i_4_ < 8) {
            block[i_4_] = buffer[i_5_].toLong() shl 56 xor
                    (buffer[i_5_ + 1].toLong() and 0xffL shl 48) xor
                    (buffer[2 + i_5_].toLong() and 0xffL shl 40) xor
                    (buffer[i_5_ + 3].toLong() and 0xffL shl 32) xor
                    (buffer[i_5_ + 4].toLong() and 0xffL shl 24) xor
                    (buffer[5 + i_5_].toLong() and 0xffL shl 16) xor
                    (buffer[i_5_ + 6].toLong() and 0xffL shl 8) xor
                    (buffer[7 + i_5_].toLong() and 0xffL)
            i_4_++
            i_5_ += 8
        }
        i_4_ = 0
        while (i_4_ < 8) {
            state[i_4_] = block[i_4_] xor hash[i_4_].also { aLongArray6637[i_4_] = it }
            i_4_++
        }
        i_4_ = 1
        while (i_4_ <= 10) {
            i_5_ = 0
            while (i_5_ < 8) {
                aLongArray6638[i_5_] = 0L
                var i_6_ = 0
                var i_7_ = 56
                while (i_6_ < 8) {
                    aLongArray6638[i_5_] = aLongArray6638[i_5_] xor aLongArrayArray6630[i_6_][(aLongArray6637[i_5_ - i_6_ and 0x7] ushr i_7_).toInt() and 0xff]
                    i_6_++
                    i_7_ -= 8
                }
                i_5_++
            }
            i_5_ = 0
            while (i_5_ < 8) {
                aLongArray6637[i_5_] = aLongArray6638[i_5_]
                i_5_++
            }
            aLongArray6637[0] = aLongArray6637[0] xor aLongArray6631[i_4_]
            i_5_ = 0
            while (i_5_ < 8) {
                aLongArray6638[i_5_] = aLongArray6637[i_5_]
                var i_8_ = 0
                var i_9_ = 56
                while (i_8_ < 8) {
                    aLongArray6638[i_5_] = aLongArray6638[i_5_] xor aLongArrayArray6630[i_8_][(state[i_5_ - i_8_ and 0x7] ushr i_9_).toInt() and 0xff]
                    i_8_++
                    i_9_ -= 8
                }
                i_5_++
            }
            i_5_ = 0
            while (i_5_ < 8) {
                state[i_5_] = aLongArray6638[i_5_]
                i_5_++
            }
            i_4_++
        }
        i_4_ = 0
        while (i_4_ < 8) {
            hash[i_4_] = hash[i_4_] xor (state[i_4_] xor block[i_4_])
            i_4_++
        }
    }

    fun finalize(digest: ByteArray, offset: Int = 0) {
        buffer[bufferPosition] = (buffer[bufferPosition].toInt() or (128 ushr (bufferBits and 0x7))).toByte()
        ++bufferPosition
        if (bufferPosition > 32) {
            while (bufferPosition < 64) {
                buffer[bufferPosition++] = 0.toByte()
            }
            processBuffer()
            bufferPosition = 0
        }
        while (bufferPosition < 32) {
            buffer[bufferPosition++] = 0.toByte()
        }
        System.arraycopy(bitLength, 0, buffer, 32, 32)
        processBuffer()
        var readIndex = 0
        var writeIndex = offset
        while (readIndex < 8) {
            val l = hash[readIndex]
            digest[writeIndex] = (l ushr 56).toInt().toByte()
            digest[writeIndex + 1] = (l ushr 48).toInt().toByte()
            digest[writeIndex + 2] = (l ushr 40).toInt().toByte()
            digest[writeIndex + 3] = (l ushr 32).toInt().toByte()
            digest[writeIndex + 4] = (l ushr 24).toInt().toByte()
            digest[writeIndex + 5] = (l ushr 16).toInt().toByte()
            digest[writeIndex + 6] = (l ushr 8).toInt().toByte()
            digest[writeIndex + 7] = l.toInt().toByte()
            readIndex++
            writeIndex += 8
        }
    }

    fun add(source: ByteArray, offset: Int = 0, length: Int = source.size) {
        var sourceBits = length * 8L
        var i = offset
        val i_23_ = 8 - (sourceBits.toInt() and 0x7) and 0x7
        val i_24_ = bufferBits and 0x7
        var l_25_ = sourceBits
        var i_26_ = 31
        var i_27_ = 0
        while ( /**/i_26_ >= 0) {
            i_27_ += (bitLength[i_26_].toInt() and 0xff) + (l_25_.toInt() and 0xff)
            bitLength[i_26_] = i_27_.toByte()
            i_27_ = i_27_ ushr 8
            l_25_ = l_25_ ushr 8
            i_26_--
        }
        while (sourceBits > 8L) {
            val i_28_ = source[i].toInt() shl i_23_ and 0xff or (source[i + 1].toInt() and 0xff ushr 8 - i_23_)
            if (i_28_ < 0 || i_28_ >= 256) {
                throw RuntimeException()
            }
            buffer[bufferPosition] = (buffer[bufferPosition].toInt() or (i_28_ ushr i_24_)).toByte()
            ++bufferPosition
            bufferBits += 8 - i_24_
            if (bufferBits == 512) {
                processBuffer()
                bufferPosition = 0
                bufferBits = 0
            }
            buffer[bufferPosition] = (i_28_ shl 8 - i_24_ and 0xff).toByte()
            bufferBits += i_24_
            sourceBits -= 8L
            i++
        }
        val i_29_: Int
        if (sourceBits > 0L) {
            i_29_ = source[i].toInt() shl i_23_ and 0xff
            buffer[bufferPosition] = (buffer[bufferPosition].toInt() or (i_29_ ushr i_24_)).toByte()
        } else {
            i_29_ = 0
        }
        if (i_24_.toLong() + sourceBits < 8L) {
            bufferBits += sourceBits.toInt()
        } else {
            ++bufferPosition
            bufferBits += 8 - i_24_
            sourceBits -= (8 - i_24_).toLong()
            if (bufferBits == 512) {
                processBuffer()
                bufferPosition = 0
                bufferBits = 0
            }
            buffer[bufferPosition] = (i_29_ shl 8 - i_24_ and 0xff).toByte()
            bufferBits += sourceBits.toInt()
        }
    }
}