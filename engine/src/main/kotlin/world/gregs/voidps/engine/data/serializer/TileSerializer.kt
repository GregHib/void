package world.gregs.voidps.engine.data.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
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

internal class TileDeserializer : StdDeserializer<Tile>(Tile::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Tile {
        val node = p.codec.readTree<JsonNode>(p)
        return Tile(node.get("x").numberValue() as Int, node.get("y").numberValue() as Int, node.get("plane")?.numberValue() as? Int ?: 0)
    }
}