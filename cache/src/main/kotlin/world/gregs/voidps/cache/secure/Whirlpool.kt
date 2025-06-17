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
            var l38 = l shl 1
            if (l38 >= 256L) {
                l38 = l38 xor 0x11dL
            }
            var l39 = l38 shl 1
            if (l39 >= 256L) {
                l39 = l39 xor 0x11dL
            }
            val l40 = l39 xor l
            var l41 = l39 shl 1
            if (l41 >= 256L) {
                l41 = l41 xor 0x11dL
            }
            val l42 = l41 xor l
            aLongArrayArray6630[0][i] = l shl 56 or (l shl 48) or (l39 shl 40) or (l shl 32) or (l41 shl 24) or (l40 shl 16) or (l38 shl 8) or l42
            for (index in 1..7) {
                aLongArrayArray6630[index][i] = aLongArrayArray6630[index - 1][i] ushr 8 or (aLongArrayArray6630[index - 1][i] shl 56)
            }
        }
        aLongArray6631[0] = 0L
        for (i in 1..10) {
            val index = 8 * (i - 1)
            aLongArray6631[i] = aLongArrayArray6630[0][index] and 0xffffffffffffffL.inv() xor
                (aLongArrayArray6630[1][index + 1] and 0xff000000000000L) xor
                (aLongArrayArray6630[2][index + 2] and 0xff0000000000L) xor
                (aLongArrayArray6630[3][index + 3] and 0xff00000000L) xor
                (aLongArrayArray6630[4][index + 4] and 0xff000000L) xor
                (aLongArrayArray6630[5][index + 5] and 0xff0000L) xor
                (aLongArrayArray6630[6][index + 6] and 0xff00L) xor
                (aLongArrayArray6630[7][index + 7] and 0xffL)
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
        for (i in 0..7) {
            hash[i] = 0L
        }
    }

    private fun processBuffer() {
        var index = 0
        var offset = 0
        while (index < 8) {
            block[index] = buffer[offset].toLong() shl 56 xor
                (buffer[offset + 1].toLong() and 0xffL shl 48) xor
                (buffer[offset + 2].toLong() and 0xffL shl 40) xor
                (buffer[offset + 3].toLong() and 0xffL shl 32) xor
                (buffer[offset + 4].toLong() and 0xffL shl 24) xor
                (buffer[offset + 5].toLong() and 0xffL shl 16) xor
                (buffer[offset + 6].toLong() and 0xffL shl 8) xor
                (buffer[offset + 7].toLong() and 0xffL)
            index++
            offset += 8
        }
        index = 0
        while (index < 8) {
            state[index] = block[index] xor hash[index].also { aLongArray6637[index] = it }
            index++
        }
        index = 1
        while (index <= 10) {
            offset = 0
            while (offset < 8) {
                aLongArray6638[offset] = 0L
                var start = 0
                var offset2 = 56
                while (start < 8) {
                    aLongArray6638[offset] = aLongArray6638[offset] xor aLongArrayArray6630[start][(aLongArray6637[offset - start and 0x7] ushr offset2).toInt() and 0xff]
                    start++
                    offset2 -= 8
                }
                offset++
            }
            offset = 0
            while (offset < 8) {
                aLongArray6637[offset] = aLongArray6638[offset]
                offset++
            }
            aLongArray6637[0] = aLongArray6637[0] xor aLongArray6631[index]
            offset = 0
            while (offset < 8) {
                aLongArray6638[offset] = aLongArray6637[offset]
                var offset2 = 0
                var start = 56
                while (offset2 < 8) {
                    aLongArray6638[offset] = aLongArray6638[offset] xor aLongArrayArray6630[offset2][(state[offset - offset2 and 0x7] ushr start).toInt() and 0xff]
                    offset2++
                    start -= 8
                }
                offset++
            }
            offset = 0
            while (offset < 8) {
                state[offset] = aLongArray6638[offset]
                offset++
            }
            index++
        }
        index = 0
        while (index < 8) {
            hash[index] = hash[index] xor (state[index] xor block[index])
            index++
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
        val diff = 8 - (sourceBits.toInt() and 0x7) and 0x7
        val bits = bufferBits and 0x7
        var previous = sourceBits
        var index = 31
        var total = 0
        while (index >= 0) {
            total += (bitLength[index].toInt() and 0xff) + (previous.toInt() and 0xff)
            bitLength[index] = total.toByte()
            total = total ushr 8
            previous = previous ushr 8
            index--
        }
        while (sourceBits > 8L) {
            val value = source[i].toInt() shl diff and 0xff or (source[i + 1].toInt() and 0xff ushr 8 - diff)
            buffer[bufferPosition] = (buffer[bufferPosition].toInt() or (value ushr bits)).toByte()
            ++bufferPosition
            bufferBits += 8 - bits
            if (bufferBits == 512) {
                processBuffer()
                bufferPosition = 0
                bufferBits = 0
            }
            buffer[bufferPosition] = (value shl 8 - bits and 0xff).toByte()
            bufferBits += bits
            sourceBits -= 8L
            i++
        }
        val last: Int
        if (sourceBits > 0L) {
            last = source[i].toInt() shl diff and 0xff
            buffer[bufferPosition] = (buffer[bufferPosition].toInt() or (last ushr bits)).toByte()
        } else {
            last = 0
        }
        if (bits.toLong() + sourceBits < 8L) {
            bufferBits += sourceBits.toInt()
        } else {
            ++bufferPosition
            bufferBits += 8 - bits
            sourceBits -= (8 - bits).toLong()
            if (bufferBits == 512) {
                processBuffer()
                bufferPosition = 0
                bufferBits = 0
            }
            buffer[bufferPosition] = (last shl 8 - bits and 0xff).toByte()
            bufferBits += sourceBits.toInt()
        }
    }
}
