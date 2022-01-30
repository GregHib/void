package world.gregs.voidps.engine.data

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

object MapSerializer : StdSerializer<Map<String, Any>>(emptyMap<String, Any>()::class.java as Class<Map<String, Any>>) {
    override fun serialize(map: Map<String, Any>, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        for ((key, value) in map) {
            if (value is Double) {
                gen.writeNumberField(key, value.toBigDecimal())
            } else {
                gen.writePOJOField(key, value)
            }
        }
        gen.writeEndObject()
    }
}