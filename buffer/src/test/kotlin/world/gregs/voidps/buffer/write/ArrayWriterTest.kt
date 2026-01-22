package world.gregs.voidps.buffer.write

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

internal class ArrayWriterTest : WriterTest() {
    override fun writer() = ArrayWriter()

    @Test
    fun `Cut removes middle section`() {
        val writer = ArrayWriter(buffer = byteArrayOf(1, 2, 3, 4, 5))
        val result = writer.cut(1, 2)
        assertContentEquals(byteArrayOf(1, 4, 5), result.array())
    }

    @Test
    fun `Cut removes from start`() {
        val writer = ArrayWriter(buffer = byteArrayOf(1, 2, 3, 4, 5))
        val result = writer.cut(0, 2)
        assertContentEquals(byteArrayOf(3, 4, 5), result.array())
    }

    @Test
    fun `Cut throws when start exceeds end`() {
        val writer = ArrayWriter(buffer = byteArrayOf(1, 2, 3, 4, 5))
        assertThrows<IllegalArgumentException> {
            writer.cut(3, 5)
        }
    }

    @Test
    fun `Cut removes single element`() {
        val writer = ArrayWriter(buffer = byteArrayOf(1, 2, 3, 4, 5))
        val result = writer.cut(2, 1)
        assertContentEquals(byteArrayOf(1, 2, 4, 5), result.array())
    }

    @Test
    fun `Cut with same start and end removes nothing`() {
        val writer = ArrayWriter(buffer = byteArrayOf(1, 2, 3, 4, 5))
        val result = writer.cut(2, 0)
        assertContentEquals(byteArrayOf(1, 2, 3, 4, 5), result.array())
    }

    @Test
    fun `Cut entire array`() {
        val writer = ArrayWriter(buffer = byteArrayOf(1, 2, 3, 4, 5))
        val result = writer.cut(0, 5)
        assertContentEquals(byteArrayOf(), result.array())
    }

    @Test
    fun `Insert adds space in middle`() {
        val writer = ArrayWriter(buffer = byteArrayOf(1, 2, 3, 4, 5))
        val result = writer.insert(2, 3)
        assertContentEquals(byteArrayOf(1, 2, 0, 0, 0, 3, 4, 5), result.array())
    }

    @Test
    fun `Insert adds space at start`() {
        val writer = ArrayWriter(buffer = byteArrayOf(1, 2, 3))
        val result = writer.insert(0, 2)
        assertContentEquals(byteArrayOf(0, 0, 1, 2, 3), result.array())
    }

    @Test
    fun `Insert adds space at end`() {
        val writer = ArrayWriter(buffer = byteArrayOf(1, 2, 3))
        val result = writer.insert(3, 2)
        assertContentEquals(byteArrayOf(1, 2, 3, 0, 0), result.array())
    }

    @Test
    fun `Insert single byte`() {
        val writer = ArrayWriter(buffer = byteArrayOf(1, 2, 3))
        val result = writer.insert(1, 1)
        assertContentEquals(byteArrayOf(1, 0, 2, 3), result.array())
    }

    @Test
    fun `Insert zero bytes does nothing`() {
        val writer = ArrayWriter(buffer = byteArrayOf(1, 2, 3))
        val result = writer.insert(1, 0)
        assertContentEquals(byteArrayOf(1, 2, 3), result.array())
    }

    @Test
    fun `Insert into empty array`() {
        val writer = ArrayWriter(buffer = byteArrayOf())
        val result = writer.insert(0, 3)
        assertContentEquals(byteArrayOf(0, 0, 0), result.array())
    }

    @Test
    fun `Cut and insert are complementary operations`() {
        val original = byteArrayOf(1, 2, 3, 4, 5)
        val writer = ArrayWriter(buffer = original)

        // Cut out 2 bytes from position 2
        val afterCut = writer.cut(2, 2)
        assertContentEquals(byteArrayOf(1, 2, 5), afterCut.array())

        // Insert 2 bytes back at position 2
        val afterInsert = afterCut.insert(2, 2)
        assertContentEquals(byteArrayOf(1, 2, 0, 0, 5), afterInsert.array())
    }
}
