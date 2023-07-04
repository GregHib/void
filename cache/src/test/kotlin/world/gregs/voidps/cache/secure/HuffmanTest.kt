package world.gregs.voidps.cache.secure

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.buffer.read.BufferReader

internal class HuffmanTest {

    private lateinit var huffman: Huffman
    private lateinit var result: ByteArray
    private lateinit var message: String

    @BeforeEach
    fun setup() {
        val data = byteArrayOf(22, 22, 22, 22, 22, 22, 21, 22, 22, 20, 22, 22, 22, 21, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 3, 8, 22, 16, 22, 16, 17, 7, 13, 13, 13, 16, 7, 10, 6, 16, 10, 11, 12, 12, 12, 12, 13, 13, 14, 14, 11, 14, 19, 15, 17, 8, 11, 9, 10, 10, 10, 10, 11, 10, 9, 7, 12, 11, 10, 10, 9, 10, 10, 12, 10, 9, 8, 12, 12, 9, 14, 8, 12, 17, 16, 17, 22, 13, 21, 4, 7, 6, 5, 3, 6, 6, 5, 4, 10, 7, 5, 6, 4, 4, 6, 10, 5, 4, 4, 5, 7, 6, 10, 6, 10, 22, 19, 22, 14, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 21, 22, 21, 22, 22, 22, 21, 22, 22)
        huffman = Huffman().load(data)
        message = ""
    }

    @Test
    fun `A short string`() {
        //Given
        build("Message")
        //When
        result = huffman.compress(message)
        //Then
        assertUncompressedSize(7)
        assertCompressedSize(5)
        assertDecompressed()
    }

    @Test
    fun `A long string`() {
        //Given
        build("This is a string of substantial size, perhaps enough character overlap for some decent compression")
        //When
        result = huffman.compress(message)
        //Then
        assertUncompressedSize(98)
        assertCompressedSize(54)
        assertDecompressed()
    }

    @Test
    fun `Full Alphabet`() {
        //Given
        build("abcdefghijklmnopqrstuvwxyz")
        //When
        result = huffman.compress(message)
        //Then
        assertUncompressedSize(26)
        assertCompressedSize(20)
        assertDecompressed()
    }

    private fun decompress(offset: Int): String? {
        val packet = BufferReader(result)
        packet.skip(offset)
        return huffman.decompress(packet, packet.readSmart())
    }

    private fun build(message: String) {
        this.message = message
    }

    private fun assertUncompressedSize(size: Int) {
        assertEquals(size, message.length)
    }

    private fun assertCompressedSize(size: Int) {
        assertEquals(size, result.size - 1)
    }

    private fun assertDecompressed(offset: Int = 0) {
        assertEquals(message, decompress(offset))
    }
}