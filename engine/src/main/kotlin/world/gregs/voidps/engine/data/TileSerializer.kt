package world.gregs.voidps.engine.data

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import world.gregs.voidps.engine.map.Tile

object TileSerializer : StdSerializer<Tile>(Tile::class.java) {
    override fun serialize(tile: Tile, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeNumberField("x", tile.x)
        gen.writeNumberField("y", tile.y)
        gen.writeNumberField("plane", tile.plane)
        gen.writeEndObject()
    }
}