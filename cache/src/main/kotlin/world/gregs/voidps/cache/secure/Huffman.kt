package world.gregs.voidps.cache.secure

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.ArrayWriter

class Huffman {

    private val logger = InlineLogger()
    private var masks: IntArray? = null
    private lateinit var frequencies: ByteArray
    private lateinit var decryptKeys: IntArray
    private lateinit var decryptedKeys: IntArray
    private var decryptionValues = listOf(0, 0x40, 0x20, 0x10, 0x8, 0x4, 0x2, 0x1)

    /**
     * Load huffman tree from cache for compression
     */
    fun load(huffman: ByteArray): Huffman {
        val start = System.currentTimeMillis()
        frequencies = huffman
        masks = IntArray(huffman.size)
        decryptKeys = IntArray(8)
        val freq = IntArray(33)
        var key = 0
        // For each non-zero frequency
        for ((index, size) in huffman.map { it.toInt() }.filter { it != 0 }.toIntArray().withIndex()) {
            // Calculate maximum frequency
            val maximumFreq = 1 shl 32 - size
            // zero or the previous minimum
            val currentFreq = freq[size]
            // Store the min
            masks!![index] = currentFreq
            // Set the frequency to the
            freq[size] = if (currentFreq and maximumFreq == 0) { // If the min and max are equal ish?
                // Starting from the bottom find the smallest frequency indices
                for (idx in size - 1 downTo 1) {
                    val leftFreq = freq[idx]
                    if (leftFreq != currentFreq) {
                        break
                    }
                    val rightFreq = 1 shl 32 - idx
                    if (rightFreq and leftFreq != 0) {
                        // Move up the tree?
                        freq[idx] = freq[idx - 1]
                        break
                    }
                    // Merge the two smallest trees
                    freq[idx] = leftFreq + rightFreq
                }
                // Sum of their frequencies
                maximumFreq + currentFreq
            } else {
                // Move up the tree?
                freq[size - 1]
            }
            for (idx in size + 1..32) {
                if (currentFreq == freq[idx]) {
                    freq[idx] = freq[size]
                }
            }

            var decryptIndex = 0
            val value: Long = Int.MAX_VALUE + 1L
            for (count in 0 until size) {
                if (currentFreq and value.ushr(count).toInt() == 0) {
                    decryptIndex++
                } else {
                    if (decryptKeys[decryptIndex] == 0) {
                        decryptKeys[decryptIndex] = key
                    }
                    decryptIndex = decryptKeys[decryptIndex]
                }
                if (decryptKeys.size <= decryptIndex) {
                    val keys = IntArray(decryptKeys.size * 2)
                    System.arraycopy(decryptKeys, 0, keys, 0, decryptKeys.size)
                    decryptKeys = keys
                }
            }
            decryptKeys[decryptIndex] = index xor -0x1
            if (key <= decryptIndex) {
                key = 1 + decryptIndex
            }
        }

        decryptedKeys = decryptKeys.map { it xor -0x1 }.toIntArray()
        logger.info { "Huffman tree decoded in ${System.currentTimeMillis() - start}ms" }
        return this
    }

    /**
     * Decompresses string of length [characters] using Huffman coding
     * @param packet The packet containing the compressed data
     * @param characters The number of string characters to decompress
     */
    fun decompress(packet: Reader, characters: Int): String {
        val textBuffer = ByteArray(packet.readableBytes())
        packet.readBytes(textBuffer)
        return decompress(textBuffer, characters) ?: ""
    }

    fun decompress(message: ByteArray, length: Int): String? {
        return try {
            if (masks == null) {
                return null
            }
            var charsDecoded = 0
            var keyIndex = 0
            val sb = StringBuilder()
            chars@ for (character in message) {
                for (value in decryptionValues) {
                    if (if (value == 0) character >= 0 else character.toInt() and value == 0) {
                        keyIndex++
                    } else {
                        keyIndex = decryptKeys[keyIndex]
                    }

                    val char = decryptedKeys[keyIndex]
                    if (char >= 0) {
                        sb.append(char.toChar())
                        if (length <= ++charsDecoded) {
                            break@chars
                        }
                        keyIndex = 0
                    }
                }
            }
            sb.toString()
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Formats, compresses and writes [message] using Huffman coding
     * @param message The message to encode
     */
    fun compress(message: String): ByteArray {
        val writer = ArrayWriter(128)
        try {
            // Format the message
            val messageData = formatMessage(message)
            // Write message length
            writer.writeSmart(messageData.size)
            // Write the compressed message
            compress(messageData, writer)
        } catch (exception: Throwable) {
            exception.printStackTrace()
        }
        return writer.toArray()
    }

    /**
     * Compresses [message] using Huffman coding and writes to [builder]
     * @param message The message to compress, split by symbol into a byte array
     * @param builder The packet to write the compressed data too
     */
    private fun compress(message: ByteArray, builder: ArrayWriter) {
        try {
            if (masks == null) {
                return
            }
            var key = 0
            val startPosition = builder.position()
            var position = startPosition shl 3
            for (char in message) {
                val character = char.toInt() and 0xff
                val min = masks!![character]
                val size = frequencies[character]

                var offset = position shr 3
                var bitOffset = position and 0x7
                key = key and (-bitOffset shr 31)
                position += size
                val byteSize = (bitOffset + size - 1 shr 3) + offset
                bitOffset += 24
                key += min.ushr(bitOffset)
                builder.setByte(offset, key)

                while (offset < byteSize) {
                    bitOffset -= 8
                    key = min.ushr(bitOffset)
                    builder.setByte(++offset, key)
                }
            }

            // Set the packet position to the correct place
            builder.position(7 + position shr 3)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Replaces unknown symbols with question marks
     * @param message The text to format
     * @return message split by character
     */
    private fun formatMessage(message: String): ByteArray {
        val array = ByteArray(message.length)
        for ((index, c) in message.withIndex()) {
            val char = c.code
            array[index] = if (char <= 0 || (char in 128..159) || char > 255) {
                63.toByte()
            } else {
                char.toByte()
            }
        }
        return array
    }
}
