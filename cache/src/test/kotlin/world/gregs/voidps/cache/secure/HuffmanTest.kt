package world.gregs.voidps.cache.secure

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.buffer.read.BufferReader
import java.io.File

internal class HuffmanTest {

    private lateinit var huffman: Huffman
    private lateinit var result: ByteArray
    private lateinit var message: String
    private val data = File("./src/test/resources/huffman.csv").readText()
        .split(", ").map { it.toByte() }.toByteArray()

    @BeforeEach
    fun setup() {
        huffman = Huffman().load(data)
        message = ""
    }

    @Test
    fun `A short string`() {
        // Given
        build("Message")
        // When
        result = huffman.compress(message)
        // Then
        assertUncompressedSize(7)
        assertCompressedSize(5)
        assertDecompressed()
    }

    @Test
    fun `A long string`() {
        // Given
        build("This is a string of substantial size, perhaps enough character overlap for some decent compression")
        // When
        result = huffman.compress(message)
        // Then
        assertUncompressedSize(98)
        assertCompressedSize(54)
        assertDecompressed()
    }

    @Test
    fun `Full Alphabet`() {
        // Given
        build("abcdefghijklmnopqrstuvwxyz")
        // When
        result = huffman.compress(message)
        // Then
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
