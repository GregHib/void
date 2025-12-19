package world.gregs.voidps.cache.secure

import com.displee.cache.CacheLibrary
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import world.gregs.voidps.buffer.write.ArrayWriter
import java.io.FileInputStream
import java.math.BigInteger
import java.util.*

class VersionTableBuilderTest {

    @Test
    fun `Build small version table`() {
        val indexCount = 5
        val table = VersionTableBuilder(BigInteger("12345"), BigInteger("54321"), indexCount)

        for (i in 0 until indexCount) {
            table.crc(i, i)
            table.whirlpool(i, ByteArray(64) { it.toByte() })
            table.revision(i, i)
        }

        val rsa = byteArrayOf(73, 58)
        val expected = expected(indexCount, rsa)
        val actual = table.build()
        assertEquals(expected, actual)
    }

    @Test
    fun `Build large version table`() {
        val indexCount = 50
        val table = VersionTableBuilder(BigInteger("1234567891011121314151617181920"), BigInteger("2019181716151413121110987654321"), indexCount)
        for (i in 0 until indexCount) {
            table.crc(i, i)
            table.whirlpool(i, ByteArray(64) { it.toByte() })
            table.revision(i, i)
        }

        val rsa = byteArrayOf(18, -4, -73, 59, -128, -51, -117, 60, 66, 21, 127, -17, -54)
        val expected = expected(indexCount, rsa)
        val actual = table.build()
        assertEquals(expected, actual)
    }

    @Test
    fun `Build version table with large RSA numbers`() {
        val indexCount = 50

        val random = Random(0)
        val exponent = BigInteger(256, random)
        val modulus = BigInteger(256, random)
        val table = VersionTableBuilder(exponent, modulus, indexCount)
        for (i in 0 until indexCount) {
            table.crc(i, i)
            table.whirlpool(i, ByteArray(64) { it.toByte() })
            table.revision(i, i)
        }

        val rsa = byteArrayOf(46, 32, 78, -33, -33, -50, 69, -78, 13, 77, 66, -120, 95, -104, -92, 62, 53, 121, -16, 1, 116, -100, 53, -36, 72, -124, 116, 113, 27, 88, -106, -19)
        val expected = expected(indexCount, rsa)
        val actual = table.build()
        assertEquals(expected, actual)
    }

    @Disabled
    @Test
    fun `Check actual cache`() {
        val library = CacheLibrary("../data/cache/")
        val indexCount = library.indices().size

        val properties = Properties()
        properties.load(FileInputStream("../game/src/main/resources/private.properties"))
        val exponent = BigInteger(properties.getProperty("security.game.private"), 16)
        val modulus = BigInteger(properties.getProperty("security.game.modulus"), 16)
        val table = VersionTableBuilder(exponent, modulus, indexCount)
        for (i in 0 until indexCount) {
            val index = library.index(i)
            table.crc(i, index.crc)
            table.revision(i, index.revision)
            table.whirlpool(i, index.whirlpool ?: ByteArray(64))
        }

        val expected = library.generateUkeys(exponent = exponent, modulus = modulus)
        val actual = table.build()
        assertEquals(expected, actual)
    }

    private fun assertEquals(expected: ByteArray, actual: ByteArray) {
        if (!expected.contentEquals(actual)) {
            println(expected.contentToString())
            println(actual.contentToString())
        }
        assertArrayEquals(expected, actual)
    }

    private fun expected(indexCount: Int, rsa: ByteArray): ByteArray {
        val size = 6 + rsa.size + indexCount * 72
        val writer = ArrayWriter(size)
        writer.writeByte(0)
        writer.writeInt(size - 5)
        writer.writeByte(indexCount)
        for (i in 0 until indexCount) {
            writer.skip(3)
            writer.writeByte(i)
            writer.skip(3)
            writer.writeByte(i)
            for (j in 0 until 64) {
                writer.writeByte(j)
            }
        }
        writer.writeBytes(rsa)
        return writer.toArray()
    }
}
