package world.gregs.voidps.engine.data.snapshot

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.area.Cuboid
import world.gregs.voidps.type.area.Polygon
import world.gregs.voidps.type.area.Rectangle

/**
 * The shapes areas are configured as.
 */
object AreaSerializer : KSerializer<Area> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("world.gregs.voidps.type.Area") {
        element("type", Byte.serializer().descriptor)
        element("coordinates", Int.serializer().descriptor)
    }

    override fun serialize(encoder: Encoder, value: Area) {
        when {
            value is Rectangle -> {
                encoder.encodeByte(0)
                encoder.encodeInt(value.minX)
                encoder.encodeInt(value.minY)
                encoder.encodeInt(value.maxX)
                encoder.encodeInt(value.maxY)
            }
            value is Cuboid -> {
                encoder.encodeByte(1)
                encoder.encodeInt(value.minX)
                encoder.encodeInt(value.minY)
                encoder.encodeInt(value.maxX)
                encoder.encodeInt(value.maxY)
                encoder.encodeInt(value.minLevel)
                encoder.encodeInt(value.maxLevel)
            }
            value.javaClass == Polygon::class.java -> {
                value as Polygon
                encoder.encodeByte(2)
                ints(encoder, value.xPoints)
                ints(encoder, value.yPoints)
                encoder.encodeInt(value.level)
                encoder.encodeInt(value.bounds.maxLevel)
            }
            else -> throw IllegalArgumentException("Unsupported area type ${value.javaClass}")
        }
    }

    override fun deserialize(decoder: Decoder): Area = when (val type = decoder.decodeByte().toInt()) {
        0 -> Rectangle(decoder.decodeInt(), decoder.decodeInt(), decoder.decodeInt(), decoder.decodeInt())
        1 -> Cuboid(decoder.decodeInt(), decoder.decodeInt(), decoder.decodeInt(), decoder.decodeInt(), decoder.decodeInt(), decoder.decodeInt())
        2 -> Polygon(ints(decoder), ints(decoder), decoder.decodeInt(), decoder.decodeInt())
        else -> throw IllegalStateException("Unknown area type $type")
    }

    private fun ints(encoder: Encoder, values: IntArray) {
        encoder.encodeInt(values.size)
        for (value in values) {
            encoder.encodeInt(value)
        }
    }

    private fun ints(decoder: Decoder) = IntArray(decoder.decodeInt()) { decoder.decodeInt() }
}
