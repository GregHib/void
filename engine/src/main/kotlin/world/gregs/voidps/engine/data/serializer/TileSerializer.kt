package world.gregs.voidps.engine.data.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import world.gregs.voidps.engine.map.Tile

object TileSerializer : KSerializer<Tile> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Tile") {
        element<Int>("x")
        element<Int>("y")
        element<Int>("z")
    }

    override fun serialize(encoder: Encoder, value: Tile) {
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, 0, value.x)
            encodeIntElement(descriptor, 1, value.y)
            encodeIntElement(descriptor, 2, value.plane)
        }
    }

    override fun deserialize(decoder: Decoder): Tile = decoder.decodeStructure(descriptor) {
        var x = 0
        var y = 0
        var z = 0
        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                0 -> x = decodeIntElement(descriptor, 0)
                1 -> y = decodeIntElement(descriptor, 1)
                2 -> z = decodeIntElement(descriptor, 2)
                CompositeDecoder.DECODE_DONE -> break
                else -> error("Unexpected index: $index")
            }
        }
        require(x in 0 until 16384 && y in 0 until 16384 && z in 0..3)
        Tile(x, y, z)
    }

}