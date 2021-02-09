package world.gregs.voidps.engine.data.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import world.gregs.voidps.engine.map.Tile

internal class TileSerializer : StdSerializer<Tile>(Tile::class.java) {
    override fun serialize(value: Tile, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeNumberField("x", value.x)
        gen.writeNumberField("y", value.y)
        gen.writeNumberField("plane", value.plane)
        gen.writeEndObject()
    }
}