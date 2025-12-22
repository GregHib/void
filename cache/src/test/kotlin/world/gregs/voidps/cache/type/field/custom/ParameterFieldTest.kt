package world.gregs.voidps.cache.type.field.custom

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter

class ParameterFieldTest {

    @Test
    fun `Read binary` () {
        val param = ParameterField(1, mapOf("key" to 4, "str" to 7))
        param.set(0, mapOf(4 to 123, 7 to "string"))
        val writer = ArrayWriter()
        param.writePacked(writer, 0, 0)

        val newParam = ParameterField(1, mapOf("key" to 4, "str" to 7))
        val reader = ArrayReader(writer.toArray())
        newParam.readPacked(reader, 0, 0)

        assertArrayEquals(param.data, newParam.data)

    }

}