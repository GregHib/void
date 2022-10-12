package world.gregs.voidps.engine.data.serial

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

object DoubleArraySerializer : StdSerializer<DoubleArray>(DoubleArray::class.java) {
    override fun serialize(value: DoubleArray, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartArray()
        for (double in value) {
            gen.writeNumber(DoubleSerializer.toBigDecimal(double))
        }
        gen.writeEndArray()
    }
}