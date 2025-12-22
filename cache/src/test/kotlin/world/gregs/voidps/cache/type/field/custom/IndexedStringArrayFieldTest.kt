package world.gregs.voidps.cache.type.field.custom

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter

class IndexedStringArrayFieldTest {

    @Test
    fun `Read direct` () {
        val arrays = IndexedStringArrayField(1, arrayOf(null, "test", null), 2)
        arrays.set(0, arrayOf("value", null, null))
        val writer = ArrayWriter()
        arrays.writeDirect(writer)

        val newArrays = IndexedStringArrayField(1, arrayOf(null, "test", null), 2)
        val reader = ArrayReader(writer.toArray())
        newArrays.readDirect(reader)

        println(arrays.get(0).toList())
        println(newArrays.get(0).toList())

        assertArrayEquals(arrays.data, newArrays.data)
    }

}